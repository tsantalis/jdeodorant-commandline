package ca.concordia.jdeodorant.eclipse.commandline.parsers.nicad;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.contribution.cedar.elements.clones.CeDARPlugin;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.jdeodorant.eclipse.commandline.parsers.CloneToolParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ExcelFileColumns;


public class NicadOutputParser extends CloneToolParser {

	private final String nicadXMLFilePath;
	private final String filesPrefix;
	
	private static Logger LOGGER = LoggerFactory.getLogger(NicadOutputParser.class);
	
	public NicadOutputParser(IJavaProject jProject, String nicadFile, File excelFile, String filesPrefix,
			boolean launchApplication, String binFolder) {
		super(jProject, excelFile, launchApplication, binFolder);
		this.nicadXMLFilePath = nicadFile;
		this.filesPrefix = filesPrefix;
	}

	@Override
	protected void fillInCloneExcelFile() {
		
		IProject iProject = jProject.getProject();
				
		SAXReader saxReader = new SAXReader();
		
		// This code is DIRTY.
		CeDARPlugin.project = iProject;
		
		Document document;
		try {
			document = saxReader.read(nicadXMLFilePath);
		} catch (DocumentException e) {
			e.printStackTrace();
			return;
		}	
		
		//int groupNumber = 1;
		//int groupCount = 0;
		int row = 1;
				
		Element root = document.getRootElement();
		
		for (Iterator<?> i = root.elementIterator("class"); i.hasNext(); ) {
			
			Element cloneClass = (Element) i.next();
			
			int groupNumber = Integer.parseInt(cloneClass.attributeValue("classid"));
			int cloneCount = Integer.parseInt(cloneClass.attributeValue("nclones"));
			
			CloneGroup cloneGroup = new CloneGroup(groupNumber);
			// cloneGroups.add(cloneGroup);

			Set<IMethod> uniqueMethodNames = new HashSet<>();
			Set<String> uniqueClassFiles = new HashSet<>();
			
			int currentCloneNumber = 0;

			for (Iterator<?> n = cloneClass.elementIterator("source"); n.hasNext();) {
						
				addLabel(row, ExcelFileColumns.CLONE_GROUP_ID, groupNumber);
				
				Element clone = (Element) n.next();
				
				currentCloneNumber++;
				
				// Relative path to the file
				String fileLocationInProject = clone.attributeValue("file").replace(filesPrefix, "");
				String realFilePath = iProject.getLocation().toPortableString() + "/" + fileLocationInProject;
				uniqueClassFiles.add(fileLocationInProject);
		
				int lineStart = Integer.parseInt(clone.attributeValue("startline"));
				addLabel(row, ExcelFileColumns.START_LINE, lineStart);
				int lineEnd = Integer.parseInt(clone.attributeValue("endline"));
				addLabel(row, ExcelFileColumns.END_LINE, lineEnd);
			
				String className = fileLocationInProject.substring(fileLocationInProject.lastIndexOf("/") + 1, fileLocationInProject.lastIndexOf("."));
				String packageName = ""; //filePath.substring( filePath.indexOf("/") + 1, filePath.lastIndexOf("/"));
				//location = location.replace("\\","/");
					
				Clone cloneInfo = new Clone (currentCloneNumber, (CloneGroup)cloneGroup, new Path(realFilePath), lineStart, lineEnd);	
				cloneGroup.addClone(cloneInfo);

				ResourceInfo resourceInfo = getResourceInfo(jProject, fileLocationInProject);
				
				if (resourceInfo == null) {
					continue;
				}

				ICompilationUnit iCompilationUnit = resourceInfo.getICompilationUnit();

				if (iCompilationUnit != null) {

					CompilationUnit cunit = null;
					if(new File (realFilePath).exists())
					{
						cunit = getCompilationUnitFromICompilationUnit(iCompilationUnit);
						if(cunit.getPackage() != null)
							packageName = cunit.getPackage().getName().toString();


						addLabel(row, ExcelFileColumns.PACKAGE_NAME, packageName);
						addLabel(row, ExcelFileColumns.CLASS_NAME, className);
						addLabel(row, ExcelFileColumns.SOURCE_FOLDER, resourceInfo.getSourceFolder());

						IDocument iDocument = getIDocument(cunit.getJavaElement());

						int startOffset = 0, endOffset = 0;

						try {


							startOffset = iDocument.getLineOffset(lineStart - 1);
							endOffset = iDocument.getLineOffset(lineEnd - 1) + iDocument.getLineLength(lineEnd - 1) - 1;

							while (isWhiteSpaceCharacter(iDocument.getChar(startOffset))) {
								startOffset++;
							}

							while (isWhiteSpaceCharacter(iDocument.getChar(endOffset))) {
								endOffset--;
							}


							//IRegion region = document.getLineInformationOfOffset(endOffset);

							//endOffset = endOffset + region.getLength() - 1;

							//textFileBuffer.commit(null, false);
						} catch (BadLocationException e) {
							if (endOffset == 0)
								endOffset = cunit.getStartPosition() + cunit.getLength();
						}

						addLabel(row, ExcelFileColumns.START_OFFSET, startOffset);
						addLabel(row, ExcelFileColumns.END_OFFSET, endOffset);

						writeCoverageInfo(packageName, className, row, cloneInfo.getStart(), cloneInfo.getEnd());

						createTextFileFromOffset(iDocument, startOffset, endOffset - startOffset, groupNumber + "-" + currentCloneNumber + ".txt");

						IMethod iMethod = getIMethod(iCompilationUnit, cunit, startOffset, endOffset - startOffset);

						if(iMethod != null) {
							addLabel(row, ExcelFileColumns.METHOD_NAME, iMethod.getElementName()); 
							try {
								addLabel(row, ExcelFileColumns.METHOD_SIGNATURE, iMethod.getSignature());
							} catch (JavaModelException e) {
								LOGGER.warn(String.format("Method signature could not be retrieved for method %s in file \"%s\"", iMethod.getElementName(), realFilePath));
								e.printStackTrace();
							}
							uniqueMethodNames.add(iMethod);
						} else {
							LOGGER.warn(String.format("IMethod could not be retrieved within file \"%s\" from offset %s to %s", realFilePath, startOffset, endOffset));
						}

					} else {
						LOGGER.warn("File \"" + realFilePath + "\" not exists.");
					}			 
				}
				row++;
			} 
			
			writeCloneGroupInfo(row - cloneCount, ExcelFileColumns.CLONE_GROUP_INFO, ExcelFileColumns.CONNTECTED, cloneGroup);
			
			addLabel(row - cloneCount, ExcelFileColumns.CLONE_GROUP_SIZE, cloneCount);
			
			if (uniqueClassFiles.size() == 1 && uniqueMethodNames.size() == 1) {
				addLabel(row - cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the same method"); 
			}
			else if (uniqueClassFiles.size() == 1) {
				addLabel(row-cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are within the same file"); 
			}
			else {
				addLabel(row-cloneCount, ExcelFileColumns.CLONE_LOCATION, "Clones are in different files"); 
			}
			
		}

	}

	@Override
	protected String getMainInputFile() {
		return nicadXMLFilePath;
	}

}
