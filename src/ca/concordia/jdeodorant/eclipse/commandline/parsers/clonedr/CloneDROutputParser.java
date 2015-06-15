package ca.concordia.jdeodorant.eclipse.commandline.parsers.clonedr;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.contribution.cedar.elements.clones.CloneGroup;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;

import ca.concordia.jdeodorant.eclipse.commandline.parsers.CloneToolParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ExcelFileColumns;

public class CloneDROutputParser extends CloneToolParser {
	
	private String pathToFiles;
	private Set<Integer> allCloneGroupIDs;
	private String pathPrefix;
	
	public CloneDROutputParser(IJavaProject jProject, String pathToFiles, File outputExcelFile, String pathPrefix,
			boolean launchApplication, String binFolder) {
		super(jProject, outputExcelFile, launchApplication, binFolder);
		this.pathToFiles = replaceBackSlasheshWithSlashesh(pathToFiles);
		if (!this.pathToFiles.endsWith("/"))
			this.pathToFiles = this.pathToFiles + "/";
		this.allCloneGroupIDs = getAllCloneGroups(pathToFiles);
		this.pathPrefix = pathPrefix;
	}

	private Set<Integer> getAllCloneGroups(String pathToFiles) {
		
		File f = new File(pathToFiles);
		File[] files = f.listFiles();
		
		Set<Integer> toReturn = new TreeSet<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return Integer.compare(o1, o2);
			}
		});
		for (File file : files) {
			Pattern pattern = Pattern.compile("xCloneSet(\\d+).html");
			Matcher matcher = pattern.matcher(file.getName());
			if (matcher.find()) {
				toReturn.add(Integer.parseInt(matcher.group(1)));
			}
		}
		
		return toReturn;
	}

	@Override
	protected void fillInCloneExcelFile() {
		int row = 1;
		int cloneGroupCount = 0;
		for (Integer cloneGroupID : this.allCloneGroupIDs) {
			cloneGroupCount++;
			Set<IMethod> uniqueIMethods = new HashSet<>();
			Set<String> uniqueClassFiles = new HashSet<>();

			String filePath = this.pathToFiles + "xCloneSet" + cloneGroupID + ".html";

			// There will be one clone group for each file
			CloneGroup cloneGroup = new CloneGroup(cloneGroupID);

			String fileContents = readResultsFile(filePath);
			// <a id="CloneInstance2">Clone Instance<br/>2</a><td>Line Count<br/>30</td><td>Source Line<br/>102</td><td><div style="{text-align:center}">Source File</div><pre>C:/Users/Davood/Desktop/apache-ant-1.7.0/src/org/apache/tools/ant/taskdefs/optional/clearcase/CCLock.java</pre>
			Pattern pattern = Pattern.compile("<a id=\\\"CloneInstance\\d+\\\">.*<br/>(\\d+)</a><td>Line Count<br/>(\\d+)</td><td>Source Line<br/>(\\d+).*Source File</div><pre>(.*)</pre>");
			Matcher cloneMatcher = pattern.matcher(fileContents);
			int cloneCount = 0;
			while (cloneMatcher.find()) {
				cloneCount++;
				int cloneNumber = Integer.valueOf(cloneMatcher.group(1) + String.valueOf(cloneGroupID));
				int cloneLineCount = Integer.parseInt(cloneMatcher.group(2));
				int cloneStartLine = Integer.parseInt(cloneMatcher.group(3));
				int cloneEndLine = cloneStartLine + cloneLineCount - 1;
				String cloneFilePath = cloneMatcher.group(4);
				int endOffset = -1, startOffset = -1;
				uniqueClassFiles.add(cloneFilePath);

				ResourceInfo resourceInfo = getResourceInfo(jProject, cloneFilePath.replace(this.pathPrefix, ""));
				if (resourceInfo == null) {
					continue;
				}

				ICompilationUnit icunit = resourceInfo.getICompilationUnit();
				CompilationUnit cunit = getCompilationUnitFromICompilationUnit(icunit);

				IDocument iDocument = getIDocument(cunit.getJavaElement());

				String className = cloneFilePath.substring(cloneFilePath.lastIndexOf("/") + 1, cloneFilePath.lastIndexOf("."));
				String packageName = cloneFilePath.replace(this.pathPrefix, "")
						.replace(className, "")
						.replace("/", ".");
				packageName = packageName.substring(0, packageName.length() - 6);
				if (cunit.getPackage() != null)
					packageName = cunit.getPackage().getName().toString();


				addLabel(row, ExcelFileColumns.CLONE_GROUP_ID, cloneGroupID);
				addLabel(row, ExcelFileColumns.SOURCE_FOLDER, resourceInfo.getSourceFolder());

				addLabel(row, ExcelFileColumns.PACKAGE_NAME, packageName);
				addLabel(row, ExcelFileColumns.CLASS_NAME, className);
				addLabel(row, ExcelFileColumns.START_LINE, cloneStartLine); 
				addLabel(row, ExcelFileColumns.END_LINE, cloneEndLine); 

				writeCoverageInfo(packageName, className, row, cloneStartLine, cloneEndLine);

				try {

					startOffset = iDocument.getLineOffset(cloneStartLine - 1);
					endOffset = iDocument.getLineOffset(cloneEndLine - 1) + iDocument.getLineLength(cloneEndLine - 1) - 1;

					char character = iDocument.getChar(startOffset);

					while ((character == ' ') || (character == '\t')) {
						startOffset++;
						character = iDocument.getChar(startOffset);
					}

					character = iDocument.getChar(endOffset);

					while ((character == ' ') || (character == '\t')) {
						endOffset--;
						character = iDocument.getChar(endOffset);
					}

					Clone newClone = new Clone(cloneNumber, cloneGroup, new Path(icunit.getResource().getLocation().toPortableString()), startOffset, endOffset);
					cloneGroup.addClone(newClone);

					addLabel(row, ExcelFileColumns.START_OFFSET,startOffset);
					addLabel(row, ExcelFileColumns.END_OFFSET, endOffset);

					IMethod iMethod = getIMethod(icunit, cunit, startOffset, endOffset - startOffset);
					if(iMethod != null) {
						addLabel(row, ExcelFileColumns.METHOD_NAME, iMethod.getElementName()); 
						addLabel(row, ExcelFileColumns.METHOD_SIGNATURE, iMethod.getSignature());
						uniqueIMethods.add(iMethod);
					}

					createTextFileFromOffset(iDocument, startOffset, endOffset - startOffset, cloneGroupID + "-" + cloneCount + ".txt");

				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println();
				}

				row++;
			}

			if (cloneCount > 0) {

				writeCloneGroupInfo(row - cloneCount, ExcelFileColumns.CLONE_GROUP_INFO, ExcelFileColumns.CONNTECTED, cloneGroup);

				addLabel(row - cloneCount, ExcelFileColumns.CLONE_GROUP_SIZE, cloneCount);

				if (uniqueClassFiles.size() == 1 && uniqueIMethods.size() == 1) {
					addLabel(row - cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the method"); 

				}
				else if (uniqueClassFiles.size() == 1) {
					addLabel(row - cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the same file"); 
				}
				else {
					addLabel(row - cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are in different files"); 
				}
			}
			progress(Math.round((100 * (((float)cloneGroupCount) / this.allCloneGroupIDs.size()))));
		}
	}

	@Override
	protected String getMainInputFile() {
		return pathToFiles;
	}

}
