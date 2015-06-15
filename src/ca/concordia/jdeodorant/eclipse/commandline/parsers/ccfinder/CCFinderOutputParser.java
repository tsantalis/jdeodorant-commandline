package ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.contribution.cedar.elements.clones.CloneGroup;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ca.concordia.jdeodorant.eclipse.commandline.parsers.CloneToolParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ExcelFileColumns;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model.ClonePair;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model.CloneSet;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model.CodeFragment;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model.DataFileReadError;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model.Model;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.utility.PrepReader;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.utility.PrepReaderError;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.utility.PrepToken;


public class CCFinderOutputParser extends CloneToolParser {
	
	private final String ccFinderFile;
	private final String preprocessorFilesPath;
	private final String optionalSrcPathPrefix;
	
	/**
	 * @param relativePathToSRCFolder CCFinder model can give us the common sub-path of the all analyzed files.
	 * for example, if all the files (when doing analysis) were in <code>path/to/project/</code>, the common sub-path 
	 * that is returned by the CCFinder model might be <code>path/to/project/src/org/blah</code>, while the Eclipse project 
	 * path is <code>path/to/project</code>, hence we cannot find the real source path (which would be
	 * <code>path/to/project/src</code> in this example). The path to the real src path is necessary because it will be used by
	 * Eclipse APIs to find the ICompilationUnits, i.e. the objects representing the Packages, Classes, etc. Hence we may need to add
	 * an optional prefix (through this parameter) which will be added to the end of project path to find the src folder.
	 * In this example we have to provide "src" for this parameter.
	 */
	public CCFinderOutputParser(IJavaProject jProject, String ccfinderResultsFilePath, 
			String preprocessorFilesPath, String relativePathToSRCFolder, File excelFile,
			boolean launchApplication, String binFolder) {
		super(jProject, excelFile, launchApplication, binFolder);
		this.ccFinderFile = replaceBackSlasheshWithSlashesh(ccfinderResultsFilePath);
		this.preprocessorFilesPath = replaceBackSlasheshWithSlashesh(preprocessorFilesPath);
		this.optionalSrcPathPrefix = relativePathToSRCFolder;
	}

	@Override
	protected void fillInCloneExcelFile() {
		
		// CCFinder's clone model, contains clone info
		Model rootModel = new Model();
		
		// Clone set in CCFinder = clone group in CEDAR 
		
		// Try to read the ccfx data file
		try {
			rootModel.readCloneDataFile(ccFinderFile);
		} catch (DataFileReadError | IOException e) {
			e.printStackTrace();
			return;
		}
					
		// Get the array of clone sets. every clone set contains a set of clone pairs.
		CloneSet[] cloneSets = rootModel.getCloneSets(Integer.MAX_VALUE);
		
		// Start writing from row 1 (row 0 is header)
		int row = 1;
		for (int i = 0; i < cloneSets.length; i++) {

			// TODO for testing
			//if (i == 100) break;
			
			int clonegroupID = (int)cloneSets[i].id;
			
			// Get clone pairs for the current clone set
			ClonePair[] clonePairs = rootModel.getClonePairsOfCloneSets(new long[] { clonegroupID });
			
			// Only keep unique clone code fragments
			Set<CodeFragment> uniqueCloneGroupClones = new HashSet<>();
			for (int j = 0; j < clonePairs.length; j++) {
				uniqueCloneGroupClones.add(clonePairs[j].getLeftCodeFragment());
				uniqueCloneGroupClones.add(clonePairs[j].getRightCodeFragment());
			}
			
			// Write the clone's info in the excel file
			writeCloneInfo(rootModel, uniqueCloneGroupClones, clonegroupID, row);
			row += uniqueCloneGroupClones.size();
			
			progress(Math.round((100 * ((i + 1F) / cloneSets.length))));
		}
		
	}

	private void writeCloneInfo(Model rootModel, Set<CodeFragment> cloneCodeFragments, int groupID, int row) {
		
		int currentCloneNumber = 0;
		
		// Keep track of unique code fragment's files and method names
		// These will be used to understand whether clones are in the same files, methods, etc
		Set<String> uniqueCloneCodeFragmentsSourceFiles = new HashSet<>();
		Set<IMethod> uniqueCloneMethodIMethods = new HashSet<>();
		
		// Create CEDAR's clone group object
		CloneGroup cloneGroup = new CloneGroup(groupID);
		//addCloneGroup(cloneGroup);
		
		
		for (CodeFragment codeFragment : cloneCodeFragments) {
			
			currentCloneNumber++;
			
			//Make an ID for current clone, we will use it in the CEDAR's clone info
			int cloneID = Integer.valueOf(groupID + "" + currentCloneNumber);

			// Get the folder of the analyzed files by CCFinder
			String rootAnalayzedFilesPath = replaceBackSlasheshWithSlashesh(rootModel.getDetectionOption().get("n")[0]);
			
			// Get the path of the java file containing current clone
			String javaFilePath = replaceBackSlasheshWithSlashesh(rootModel.getFile(codeFragment.file).path);
			
			// Remove the root folder from java file's path (to make a relative path, so it can be understood by Eclipse project)
			String projectFilePath = javaFilePath.replace(rootAnalayzedFilesPath, "").replaceFirst("/", "");
			
			// Get the compilation unit for java file from it's relative path
			String resourcePath = projectFilePath;
			if (!"".equals(optionalSrcPathPrefix))
				resourcePath = optionalSrcPathPrefix + "/" + resourcePath;
			
			ResourceInfo resourceInfo = getResourceInfo(jProject, resourcePath);
			ICompilationUnit iCompilationUnit = resourceInfo.getICompilationUnit();
			
			// Add the class's path to the list of unique files
			uniqueCloneCodeFragmentsSourceFiles.add(iCompilationUnit.getPath().toPortableString());
					
			String className = projectFilePath.substring(projectFilePath.lastIndexOf("/") + 1, projectFilePath.lastIndexOf("."));
			String packageName = ""; //projectFilePath.substring(0, projectFilePath.lastIndexOf("/")).replace("/", ".");
						
			// Parse the Java file and get the CompilationUnit
			CompilationUnit cunit = getCompilationUnitFromICompilationUnit(iCompilationUnit);
			
			// Get the package name from CompilationUnit parser
			if (cunit.getPackage() != null)
				packageName = cunit.getPackage().getName().toString();
		
			// Get the corresponding .ccfxprep file from Java file path, contains token info which is necessary for finding offsets.
			String preprocessorFilePath = this.preprocessorFilesPath + "/" + projectFilePath + rootModel.getDetectionOption().getPostfix();
	
			// tokens array is needed to find the offset of the clone's code fragment in the file, because of CCFinder's design
			PrepToken[] tokens; 
			try {
				tokens = (new PrepReader()).read(preprocessorFilePath, rootModel.getDetectionOption().getPostfix());
			} catch (PrepReaderError e) {
				tokens = null;
			} catch (IOException e) {
				tokens = null;
			}
			
			// Adapted from CCFinder code, get the offsets
			int beginPos = tokens[codeFragment.begin].beginIndex;
			int endPos = tokens[codeFragment.end - 1].endIndex;
			
			// Get the iDocument from Compilation unit, so the line numbers and code fragment's text could be obtained
			IDocument iDocument = getIDocument(cunit.getJavaElement());
		
			// Create a text file containing current 
			createTextFileFromOffset(iDocument, beginPos, endPos - beginPos, groupID + "-" + currentCloneNumber + ".txt");
					
			// Create clone info (CEDAR's clone objects)
			Clone cloneInfo = new Clone(cloneID, cloneGroup, new Path(iCompilationUnit.getResource().getLocation().toPortableString()), beginPos, endPos);	
			cloneGroup.addClone(cloneInfo);
			
			
			addLabel(row, ExcelFileColumns.CLONE_GROUP_ID, groupID);
			addLabel(row, ExcelFileColumns.PACKAGE_NAME, packageName);
			addLabel(row, ExcelFileColumns.CLASS_NAME, className);
			addLabel(row, ExcelFileColumns.SOURCE_FOLDER, resourceInfo.getSourceFolder());
			
			// Get method info (name and signature)
			IMethod iMethod = getIMethod(iCompilationUnit, cunit, beginPos, endPos - beginPos);
			if(iMethod != null) {
				addLabel(row, ExcelFileColumns.METHOD_NAME, iMethod.getElementName()); 
				try {
					addLabel(row, ExcelFileColumns.METHOD_SIGNATURE, iMethod.getSignature());
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				// Add it to the list of unique method names for this clone group
				uniqueCloneMethodIMethods.add(iMethod);
			}
	
			try {
				int startLine = iDocument.getLineOfOffset(beginPos);
				addLabel(row, ExcelFileColumns.START_LINE, startLine);
				int endLine = iDocument.getLineOfOffset(endPos);
				addLabel(row, ExcelFileColumns.END_LINE, endLine);
				writeCoverageInfo(packageName, className, row, startLine + 1, endLine + 1);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			
			addLabel(row, ExcelFileColumns.START_OFFSET, beginPos);
			addLabel(row, ExcelFileColumns.END_OFFSET, endPos);
			
			
						
			row++;
		
		}
				
		// Number of clones in the current clone set
		addLabel(row - cloneCodeFragments.size(), ExcelFileColumns.CLONE_GROUP_SIZE, cloneCodeFragments.size());
		
		// Write clone group's info (subclone, repeated, etc)
		writeCloneGroupInfo(row - cloneCodeFragments.size(), ExcelFileColumns.CLONE_GROUP_INFO, ExcelFileColumns.CONNTECTED, cloneGroup);
		
		// Clone location
		String clonesLocationDescription = "Clones are ";
		if (uniqueCloneCodeFragmentsSourceFiles.size() == 1) {
			if (uniqueCloneMethodIMethods.size() == 1) {
				clonesLocationDescription += "within the same method"; 
			} else {
				clonesLocationDescription += "within the same file";
			}
		} else {
			clonesLocationDescription += "in different files";
		}
		addLabel(row - cloneCodeFragments.size(), ExcelFileColumns.CLONE_LOCATION, clonesLocationDescription);
		
		// Create txt file and add its hyperlink to the file
		// createWinmergeFileAndWriteHyperlink(groupID, row, ExcelFileColumns.DETAILS, cloneCodeFragments.size());
	}

	@Override
	protected String getMainInputFile() {
		return this.ccFinderFile;
	}
	
}
