package ca.concordia.jdeodorant.eclipse.commandline;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.MalformedTreeException;
import org.slf4j.Logger;

import ca.concordia.jdeodorant.eclipse.commandline.ApplicationRunner.TestReportFileType;
import ca.concordia.jdeodorant.eclipse.commandline.cli.CLIParser;
import ca.concordia.jdeodorant.eclipse.commandline.cli.CLIParser.ApplicationMode;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.TestReportResults;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.TestReportResults.TestReportDifference;
import ca.concordia.jdeodorant.eclipse.commandline.utility.FileLogger;
import ca.concordia.jdeodorant.eclipse.commandline.utility.Mailer;
import gr.uom.java.ast.ASTReader;
import gr.uom.java.ast.CompilationErrorDetectedException;
import gr.uom.java.ast.Standalone;
import gr.uom.java.distance.CandidateRefactoring;
import gr.uom.java.distance.ExtractClassCandidateGroup;
import gr.uom.java.distance.ExtractClassCandidateRefactoring;
import gr.uom.java.distance.MoveMethodCandidateRefactoring;
import gr.uom.java.jdeodorant.refactoring.manipulators.ExtractClassRefactoring;
import gr.uom.java.jdeodorant.refactoring.manipulators.MoveMethodRefactoring;

@SuppressWarnings("restriction")
public class Application implements IApplication {
	
	private static Logger LOGGER = FileLogger.getLogger(Application.class);
	private static CLIParser cliParser;

	@Override
	public Object start(IApplicationContext arg0) throws Exception {

		// Get the commandline parser object
		cliParser = new CLIParser((String[])arg0.getArguments().get(IApplicationContext.APPLICATION_ARGS));
				
		if (cliParser.showHelp()) {
			cliParser.printHelp();
		}
		else { 
			String status = "";
			try {

				ApplicationMode applicationMode = cliParser.getApplicationMode(ApplicationMode.ANALYZE_EXISTING);

				String projectName = "";
				IJavaProject jProject = null;
				if (cliParser.getProjectDescritionFile() != null) {
					IProjectDescription description = ResourcesPlugin.getWorkspace().
						loadProjectDescription(new Path(cliParser.getProjectDescritionFile()));
					projectName = description.getName();
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
					if(!project.exists()) {
						project.create(description, null);
					}
					if (!project.isOpen()) {
						project.open(null);
					}
					if(project.hasNature(JavaCore.NATURE_ID)) {
						jProject = JavaCore.create(project);
					}					
				} else if (cliParser.getProjectName() != null) {
					projectName = cliParser.getProjectName();
					jProject = findJavaProjectInWorkspace(projectName);
				}
				
				if (jProject == null) {
					throw new RuntimeException("The project \"" + projectName + "\" is not opened in the workspace. Cannot continue.");
				}
				// If the application mode is not ApplicationMode.PARSE, we have to parse the project and make AST, otherwise, we don't need it. 
				if (applicationMode != ApplicationMode.PARSE) {
					parseJavaProject(jProject);
				}
				if (!cliParser.isDebuggingEnabled())
					handleScheduledJobsByEclipse();
				IProject project = jProject.getProject();
				project.setDescription(project.getDescription(), ~IProject.KEEP_HISTORY, new NullProgressMonitor());

				File excelFile = new File(cliParser.getExcelFilePath());

				if (cliParser.hasLogToFile()) {
					FileLogger.addFileAppender(excelFile.getParentFile().getAbsolutePath() + "/log.log", false);
				}
				
				String excelFileParentDirectory = replaceBackSlasheshWithSlashes(excelFile.getParent()) + "/";
				
				if (cliParser.runTests()) {
					ApplicationRunner runner;
					try {
						runner = new ApplicationRunner(jProject, cliParser.getClassFolder(), excelFileParentDirectory);
						LOGGER.info("Started running tests and getting test coverage");
						runner.launchApplication();
						LOGGER.info("Finished running tests and getting test coverage");
						//List<LineCoverage> lineCoverageInfo = ApplicationRunner.readCoverageFile(excelFileParentDirectory);
					} catch (IOException | CoreException e) {
						e.printStackTrace();
					}
				}

				testRefactorings(jProject, excelFile);

				status = "OK";
			} catch (Throwable throwable) {
				throwable.printStackTrace();
				status = "ERROR";
			}

			if (cliParser.getNotificationEmailAddresses().length > 0) {
				Mailer mailer = new Mailer(cliParser.getSMTPServerAddress(),
						cliParser.getSMTPServerPort(),
						cliParser.isMailServerAuthenticated(),
						cliParser.getMailServerSecurtyType(),
						cliParser.getMailServerUserName(),
						cliParser.getMailServerPassword());
				String message = String.format("Finished analysing project %s (%s) in %s [%s]",
						cliParser.getProjectName(),
						cliParser.getExcelFilePath(),
						getComputerName(),
						status);
				String subject = String.format("Analysis finished [%s]", status);
				mailer.sendMail(subject, message, cliParser.getMailServerUserName(), cliParser.getNotificationEmailAddresses());
			}
		}

		return IApplication.EXIT_OK;
	}
	
	private String replaceBackSlasheshWithSlashes(String string) {
		return string.replace("\\", "/");
	}
	
	private void testRefactorings(IJavaProject iJavaProject, File originalExcelFile) {
		/*
		 * Keeping this is important, as we check for these options
		 * in different places in the code, and we might go inconsistent if we don't do it.
		 * Please pass -rt command line argument if you want to run tests.
		 * Also pass java compilation output folder using -cf (e.g., -cf "bin")
		 */
		boolean shouldRunTests = cliParser.runTests();
		LOGGER.info("Testing refactorability in " + originalExcelFile.getAbsolutePath());

		TestReportResults originalTestReport = null;
		if (shouldRunTests)
			originalTestReport = ApplicationRunner.readTestFile(originalExcelFile.getParent(), TestReportFileType.ORIGINAL);
		
		LOGGER.info("Started detecting refactoring opportunities");
		//List<MoveMethodCandidateRefactoring> refactorings = Standalone.getMoveMethodRefactoringOpportunities(iJavaProject);
		List<ExtractClassCandidateRefactoring> refactorings = new ArrayList<ExtractClassCandidateRefactoring>();
		Set<ExtractClassCandidateGroup> candidateGroups = Standalone.getExtractClassRefactoringOpportunities(iJavaProject);
		for(ExtractClassCandidateGroup candidateGroup : candidateGroups) {
			refactorings.addAll(candidateGroup.getCandidates());
		}
		LOGGER.info("Finished detecting refactoring opportunities");
		LOGGER.info("Number of detected refactoring opportunities: " + refactorings.size());
		
		int i=1;
		for(CandidateRefactoring candidate : refactorings) {
			LOGGER.info("Refactoring opportunity: " + i + " out of " + refactorings.size());
			Refactoring refactoring = generateRefactoring(candidate);
			testRefactoring(iJavaProject, originalExcelFile, shouldRunTests, originalTestReport, refactoring);
			i++;
		}
		try {
			iJavaProject.getProject().getWorkspace().save(true, new NullProgressMonitor());
			LOGGER.info("Finished testing refactorability in " + originalExcelFile.getAbsolutePath());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private Refactoring generateRefactoring(CandidateRefactoring candidate) {
		if(candidate instanceof MoveMethodCandidateRefactoring) {
			return generateMoveMethodRefactoring((MoveMethodCandidateRefactoring)candidate);
		}
		else if(candidate instanceof ExtractClassCandidateRefactoring) {
			return generateExtractClassRefactoring((ExtractClassCandidateRefactoring)candidate);
		}
		return null;
	}

	private ExtractClassRefactoring generateExtractClassRefactoring(ExtractClassCandidateRefactoring candidate) {
		String[] tokens = candidate.getTargetClassName().split("\\.");
		String extractedClassName = tokens[tokens.length-1];
		return new ExtractClassRefactoring(candidate.getSourceIFile(), (CompilationUnit)candidate.getSourceClassTypeDeclaration().getRoot(),
				candidate.getSourceClassTypeDeclaration(), candidate.getExtractedFieldFragments(), candidate.getExtractedMethods(),
				candidate.getDelegateMethods(), extractedClassName);
	}

	private MoveMethodRefactoring generateMoveMethodRefactoring(MoveMethodCandidateRefactoring candidate) {
		return new MoveMethodRefactoring((CompilationUnit)candidate.getSourceClassTypeDeclaration().getRoot(),
				(CompilationUnit)candidate.getTargetClassTypeDeclaration().getRoot(),
				candidate.getSourceClassTypeDeclaration(), candidate.getTargetClassTypeDeclaration(), candidate.getSourceMethodDeclaration(),
				candidate.getAdditionalMethodsToBeMoved(), candidate.leaveDelegate(), candidate.getMovedMethodName());
	}

	private void testRefactoring(IJavaProject iJavaProject, File originalExcelFile, boolean shouldRunTests, TestReportResults originalTestReport, Refactoring refactoring) {
		IProgressMonitor npm = new NullProgressMonitor();
		try {
			RefactoringStatus refStatus = refactoring.checkFinalConditions(npm);

			if (refStatus.isOK()) {
				LOGGER.info("Started refactoring");
				Change change = refactoring.createChange(npm);
				Change undoChange = change.perform(npm);
				LOGGER.info("Finished Refactoring");
				List<IMarker> markers = buildProject(iJavaProject, npm);
				// Check for compile errors
				if (markers.size() > 0) {
					for (IMarker marker : markers) {
						//String message = marker.getAttributes().get("message").toString();
						LOGGER.warn("Compilation error: " + marker.getResource().getFullPath().toOSString());
					}
					LOGGER.warn("Compile errors occured during refactoring");
				} else { 
					if (shouldRunTests) {
						// Run tests here and see if they pass
						LOGGER.info("Started running unit tests");
						new ApplicationRunner(iJavaProject, cliParser.getClassFolder(), new File(cliParser.getExcelFilePath()).getParent().toString()).launchTest();
						LOGGER.info("Finished running unit tests");
						LOGGER.info("Reading unit tests reports file");
						TestReportResults newTestReport = ApplicationRunner.readTestFile(originalExcelFile.getParent(), TestReportFileType.AFTER_REFACTORING);
						LOGGER.info("Comparing test results");
						List<TestReportDifference> compareTestResults = newTestReport.compareTestResults(originalTestReport);
						if (compareTestResults.size() != 0) {
							LOGGER.warn("Tests failed after refactoring");
							for(TestReportDifference diff : compareTestResults) {
								LOGGER.warn(diff.toString());
							}
						} else {
							LOGGER.info("Tests passed after refactoring");
						}
					}
				}

				LOGGER.info("Started undoing refactoring");
				boolean shouldRetry = true;
				do {
					try {
						undoChange.perform(npm);
						shouldRetry = false;
					} catch (ResourceException rex) {
						LOGGER.warn("Exception while deleting resources, retrying...");
						Thread.sleep(500);
					}
				} while (shouldRetry);
				LOGGER.info("Finished undoing refactoring");
				markers = buildProject(iJavaProject, npm);
				if (markers.size() > 0) {
					// Is it possible to have compile errors after undoing?
					LOGGER.error("Compiler errors after undoing refactorings");
				}
				iJavaProject.getProject().deleteMarkers(null, true, IResource.DEPTH_INFINITE);
				iJavaProject.getProject().clearHistory(new NullProgressMonitor());
			} else {
				LOGGER.warn("Refactoring was not applied due to precondition violations");
			}
		} catch (MalformedTreeException mte) {
			// Overlapping text edits
			LOGGER.warn("Overlapping text edits");
		} catch(CoreException ce) {
			ce.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	private String getComputerName() {
		String hostname = "Unknown";
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException ex) {
			LOGGER.warn("Hostname can not be resolved");
		}
		return hostname;
	}

	/**
	 * This method will cancel following jobs to prevent memory leak by IndexManager and increasing execution performance
	 * Debug Event Dispatch
	 * Updating encoding settings.
	 * Periodic workspace save.
	 * Building workspace
	 * Java indexing..
	 * These jobs run automatically by Eclipse
	 */
	private void handleScheduledJobsByEclipse() {
		IJobManager jobManager = Job.getJobManager();
		jobManager.addJobChangeListener(new IJobChangeListener() {

			@Override
			public void sleeping(IJobChangeEvent jobChangeEvent) {

			}

			@Override
			public void scheduled(IJobChangeEvent jobChangeEvent) {
				jobChangeEvent.getJob().cancel();
			}

			@Override
			public void running(IJobChangeEvent jobChangeEvent) {
			}

			@Override
			public void done(IJobChangeEvent jobChangeEvent) {

			}

			@Override
			public void awake(IJobChangeEvent jobChangeEvent) {

			}

			@Override
			public void aboutToRun(IJobChangeEvent jobChangeEvent) {
			}
		});
	}

	private List<IMarker> buildProject(IJavaProject iJavaProject, IProgressMonitor npm)
			throws CoreException {
		IProject project = iJavaProject.getProject();
		project.refreshLocal(IResource.DEPTH_INFINITE, npm);
		LOGGER.info("Started building");	
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, npm);
		LOGGER.info("Finished building");

		ArrayList<IMarker> result = new ArrayList<>();
		IMarker[] markers = null;
		markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		for (IMarker marker: markers)
		{
			Integer severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
			if (severityType.intValue() == IMarker.SEVERITY_ERROR)
				result.add(marker);
		}
		return result;
	}

	/**
	 * Gets the IJavaProject from projectName
	 * @param projectName
	 * @return
	 * @throws CoreException
	 */
	private IJavaProject findJavaProjectInWorkspace(String projectName) throws CoreException {
		IJavaProject jProject = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		for(IProject project : projects) {
			if(project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.getName().equals(projectName)) {
				jProject = JavaCore.create(project);
				LOGGER.info("Project " + projectName + " was found in the workspace");
				break;
			}
		}
		return jProject;
	}
	
	private void parseJavaProject(IJavaProject jProject) {
		LOGGER.info("Now parsing the project");
		try {
			if(ASTReader.getSystemObject() != null && jProject.equals(ASTReader.getExaminedProject())) {
				new ASTReader(jProject, ASTReader.getSystemObject(), null);
			}
			else {
				new ASTReader(jProject, null);
			}
		} catch(CompilationErrorDetectedException e) {
			e.printStackTrace(System.err);
			LOGGER.info("Project contains compilation errors");
		}
		LOGGER.info("Finished parsing");
	}

	@Override
	public void stop() {

	}

}
