package ca.concordia.jdeodorant.eclipse.commandline.parsers.conqat;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.contribution.cedar.elements.clones.CloneGroup;
import org.eclipse.core.resources.IProject;
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



public class ConQatOutputParser extends CloneToolParser {

	private String conqatXMLFilePath;

	public ConQatOutputParser(IJavaProject jProject, String conqatOutputXMLFilePath, File excelFile,
			boolean launchApplication, String binFolder) {
		super(jProject, excelFile, launchApplication, binFolder);
		this.conqatXMLFilePath = conqatOutputXMLFilePath;
	}


	public void fillInCloneExcelFile() {
		SAXReader saxReader = new SAXReader();
		int groupNumber = 1;
		int row = 1;

		Document document;
		try {
			document = saxReader.read(conqatXMLFilePath);
		} catch (DocumentException e1) {
			e1.printStackTrace();
			return;
		}			
		Element root = document.getRootElement();
		int groupCount = 0;
		IProject iProject = jProject.getProject();
		// For each clone class,
		for (Iterator<?> i = root.elementIterator("cloneClass"); i.hasNext();) {
			Element cloneClass = (Element) i.next();
			int cloneCount = 0;
			CloneGroup cloneGroup = new CloneGroup(groupCount++);
			// cloneGroups.add(cloneGroup);

			Set<IMethod> uniqueIMethods = new HashSet<>();
			Set<String> uniqueClassFiles = new HashSet<>();

			// For each clone
			for (Iterator<?> n = cloneClass.elementIterator("clone"); n.hasNext();) {
				cloneCount++;
				addLabel(row, ExcelFileColumns.CLONE_GROUP_ID, groupNumber);
				Element clone = (Element) n.next();
				String sourceFileId = clone.attributeValue("sourceFileId");
				String location = "";

				Element values = clone.element("values");
				@SuppressWarnings("unchecked")
				List<Element> positionValueList = values.elements();
				int startLine = 0;
				int endLine = 0;
				int startOffset = 0;
				int endOffset = 0;
				for (int t = 0; t < positionValueList.size(); t++) {
					String field = positionValueList.get(t).attributeValue("key");
					String value = positionValueList.get(t).attributeValue("value");
					switch (field) {
					case "START_LINE":
						startLine = Integer.valueOf(value);
						addLabel(row, ExcelFileColumns.START_LINE, startLine);
						break;
					case "END_LINE":
						endLine = Integer.valueOf(value);
						addLabel(row, ExcelFileColumns.END_LINE, endLine);
						break;
					case "START_OFFSET":
						startOffset = Integer.valueOf(value);
						break;
					case "END_OFFSET":
						endOffset = Integer.valueOf(value);
						break;
					}
				}
				// Search for the file based on the file id
				for (Iterator<?> m = root.elementIterator("sourceFile"); m.hasNext();) {
					Element sourceFile = (Element) m.next();
					String filePath = "";
					String className = "";
					String packageName = "";
					// When file is found, add the information about the file to the excel file
					if (sourceFile.attributeValue("id").equals(sourceFileId)) {
						filePath = sourceFile.attributeValue("path");
						location = sourceFile.attributeValue("location");

						className = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
						packageName = filePath.substring(filePath.indexOf("/") + 1, filePath.lastIndexOf("/"));
						location = location.replace("\\","/");
						String projectFile = location.replace(iProject.getLocation().toString() + "/", "");
						uniqueClassFiles.add(projectFile);
						
						Clone cloneInfo = new Clone (cloneCount, (CloneGroup)cloneGroup, new Path(location), startLine, endLine);	
						cloneGroup.addClone(cloneInfo);

						ResourceInfo resourceInfo = getResourceInfo(jProject, projectFile);
						ICompilationUnit iCompilationUnit = resourceInfo.getICompilationUnit();
						
						CompilationUnit cunit = null;
						if(new File (location).exists())
						{
							cunit = getCompilationUnitFromICompilationUnit(iCompilationUnit);
							if(cunit.getPackage() != null) packageName = cunit.getPackage().getName().toString();
						}
						
						addLabel(row, ExcelFileColumns.PACKAGE_NAME, packageName);
						addLabel(row, ExcelFileColumns.CLASS_NAME, className);
						addLabel(row, ExcelFileColumns.SOURCE_FOLDER, resourceInfo.getSourceFolder());

						IDocument iDocument = getIDocument(cunit.getJavaElement());

						try {

							startOffset = iDocument.getLineOffset(startLine);
							endOffset = iDocument.getLineOffset(endLine)+iDocument.getLineLength(endLine);

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

							addLabel(row, ExcelFileColumns.START_OFFSET,startOffset);
							addLabel(row, ExcelFileColumns.END_OFFSET, endOffset);
							
							writeCoverageInfo(packageName, className, row, startLine, endLine);

							IMethod iMethod = getIMethod(iCompilationUnit, cunit, startOffset, endOffset - startOffset);
							if(iMethod != null) {
								addLabel(row, ExcelFileColumns.METHOD_NAME, iMethod.getElementName()); 
								addLabel(row, ExcelFileColumns.METHOD_SIGNATURE, iMethod.getSignature());
								uniqueIMethods.add(iMethod);
							}

						} catch (BadLocationException | JavaModelException e) {
							e.printStackTrace();
						}
						
						createTextFileFromOffset(iDocument, startOffset, endOffset - startOffset, groupNumber + "-" + cloneCount + ".txt");
						// Dont search in other files
						break;
					}
				}
				

				
				row++;
				
			}

			writeCloneGroupInfo(row - cloneCount, ExcelFileColumns.CLONE_GROUP_INFO, ExcelFileColumns.CONNTECTED, cloneGroup);

			addLabel(row - cloneCount, ExcelFileColumns.CLONE_GROUP_SIZE, cloneCount);

			if (uniqueClassFiles.size() == 1 && uniqueIMethods.size() == 1) {
				addLabel(row - cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the method"); 

			}
			else if (uniqueClassFiles.size() == 1) {
				addLabel(row-cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the same file"); 
			}
			else {
				addLabel(row-cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are in different files"); 
			}

			++groupNumber;
		}

	}


	@Override
	protected String getMainInputFile() {
		return this.conqatXMLFilePath;
	}
}