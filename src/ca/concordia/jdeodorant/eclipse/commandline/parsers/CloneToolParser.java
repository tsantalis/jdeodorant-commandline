package ca.concordia.jdeodorant.eclipse.commandline.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.eclipse.contribution.cedar.analysis.util.NodeFinder;
import org.eclipse.contribution.cedar.elements.clones.CloneGroup;
import org.eclipse.contribution.cedar.elements.clones.CloneGroupGroup;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.jdeodorant.eclipse.commandline.ApplicationRunner;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.CoverageStatus;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.LineCoverage;
import ca.concordia.jdeodorant.eclipse.commandline.utility.SourceDirectoryUtility;

public abstract class CloneToolParser {

	private ArrayList<CloneGroupGroup> cloneGroupGroups = new ArrayList<CloneGroupGroup>();

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CloneToolParser.class);

	private WritableWorkbook workbook;
	private WritableSheet sheet;
	public final static String CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME = "code-fragments";

	protected final String codeFragmentsDirectory;
	protected final String excelFileParentDirectory;
	protected final String projectName;
	protected final IJavaProject jProject;
	protected List<LineCoverage> lineCoverageInfo;

	public CloneToolParser(IJavaProject jProject, File excelFile, boolean launchApplication, String binFolder) {
		this.excelFileParentDirectory = replaceBackSlasheshWithSlashesh(excelFile
				.getParent()) + "/";
		this.codeFragmentsDirectory = this.excelFileParentDirectory + "/"
				+ CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME + "/";
		File outputFolder = new File(this.codeFragmentsDirectory);
		if (!outputFolder.exists())
			outputFolder.mkdir();

		this.jProject = jProject;
		this.projectName = jProject.getElementName();
		createExcelFile(excelFile);
		
		if (launchApplication) {
			ApplicationRunner runner;
			try {
				runner = new ApplicationRunner(jProject,
						binFolder, excelFileParentDirectory);
				LOGGER.info("Started running tests and getting test coverage");
				runner.launchApplication();
				LOGGER.info("Finished running tests and getting test coverage");
				lineCoverageInfo = ApplicationRunner.readCoverageFile(excelFileParentDirectory);
			} catch (IOException | CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void writeCoverageInfo(String packageName, String className,   int cloneRow, int startLine, int endLine) {
		if (lineCoverageInfo != null) {
			String packageNameDotsReplaced = packageName.replace(".", "/");
			List<LineCoverage> filteredLines = lineCoverageInfo
					.stream()
					.filter(row -> row.getPackageName().equals(packageNameDotsReplaced)
							&& row.getClassName().equals(className)
							&& row.getLine() >= startLine
							&& row.getLine() <= endLine)
					.collect(Collectors.toList());
			int allLinesCount = filteredLines.size();
			long coveredLinesCount = filteredLines.stream().filter(row->row.getStatus() != CoverageStatus.NOT_COVERED).count();
			float lineCoverage = 0;
			if (allLinesCount != 0)
				lineCoverage = (float)coveredLinesCount / allLinesCount;
			Number number = new Number(
					ExcelFileColumns.LINE_COVERAGE_PERCENTAGE
					.getColumnNumber(),
					cloneRow, lineCoverage);
			try {
				sheet.addCell(number);
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!filteredLines.isEmpty()) {

			}
		}
	}

	protected void createExcelFile(File excelFile) {
		try {
			workbook = Workbook.createWorkbook(excelFile);
			sheet = workbook.createSheet("First Sheet", 0);
			addColoumnHeaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addColoumnHeaders() {
		WritableFont boldFont = new WritableFont(WritableFont.TIMES, 11,
				WritableFont.BOLD);
		WritableCellFormat boldFormat = new WritableCellFormat(boldFont);
		for (ExcelFileColumns value : ExcelFileColumns.values()) {
			Label label = new Label(value.getColumnNumber(), 0,
					value.getColumnTitle(), boldFormat);
			try {
				sheet.addCell(label);
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}

	protected abstract void fillInCloneExcelFile();

	protected String replaceBackSlasheshWithSlashesh(String string) {
		return string.replace("\\", "/");
	}

	public static IMethod getIMethod(ICompilationUnit iCompilationUnit,
			CompilationUnit cunit, int begin, int length) {

		IMethod iMethod = null;

		try {
			ASTNode node = NodeFinder.perform(cunit.getRoot(), begin, length,
					iCompilationUnit);

			if (!(node instanceof MethodDeclaration)) {
				ASTNode parent = node.getParent();
				while (parent != null) {
					if (parent instanceof MethodDeclaration) {
						node = parent;
						break;
					}
					parent = parent.getParent();
				}
			}

			if (node instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) node;
				IJavaElement element;

				try {
					element = iCompilationUnit.getElementAt(method
							.getStartPosition());
					iMethod = (IMethod) element;

				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return iMethod;

	}

	public void execute() {
		fillInCloneExcelFile();
		try {
			workbook.write();
			workbook.close();
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
	}

	protected void addLabel(int row, ExcelFileColumns col, String labelString) {
		Label label = new Label(col.getColumnNumber(), row, labelString);
		addCell(label);
	}

	protected void addLabel(int row, ExcelFileColumns col,
			java.lang.Number number) {
		Number label = new Number(col.getColumnNumber(), row,
				Double.valueOf(number.toString()));
		addCell(label);
	}

	protected void addHyperlink(int row, int col, File file, String description) {
		WritableHyperlink fileLink = new WritableHyperlink(col, row, file,
				description);
		try {
			sheet.addHyperlink(fileLink);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	protected void addHyperlink(int row, int col, String path,
			String description) {
		Formula hyperLinkFormula = new Formula(col, row, "HYPERLINK(\""
				+ CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME + "/" + path + "\",\""
				+ description + "\")");
		try {
			sheet.addCell(hyperLinkFormula);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	private void addCell(WritableCell cell) {
		try {
			sheet.addCell(cell);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	protected void createTextFileFromOffset(IDocument iDocument, int offSet,
			int length, String fileName) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(this.codeFragmentsDirectory
					+ fileName));
			writer.write(iDocument.get(offSet, length));

		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	protected void createWinMergeFile(long cloneGroupNumber,
			int firstCloneNumber, int secondCloneNumber) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			org.w3c.dom.Document doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = doc.createElement("project");
			doc.appendChild(rootElement);

			org.w3c.dom.Element paths = doc.createElement("paths");
			rootElement.appendChild(paths);

			org.w3c.dom.Element left = doc.createElement("left");
			left.appendChild(doc.createTextNode(codeFragmentsDirectory
					+ cloneGroupNumber + "-" + firstCloneNumber + ".txt"));
			paths.appendChild(left);

			org.w3c.dom.Element leftreadonly = doc
					.createElement("left-readonly");
			leftreadonly.appendChild(doc.createTextNode("1"));
			paths.appendChild(left);

			org.w3c.dom.Element right = doc.createElement("right");
			right.appendChild(doc.createTextNode(codeFragmentsDirectory
					+ cloneGroupNumber + "-" + secondCloneNumber + ".txt"));
			paths.appendChild(right);

			org.w3c.dom.Element rightreadonly = doc
					.createElement("right-readonly");
			rightreadonly.appendChild(doc.createTextNode("1"));
			paths.appendChild(rightreadonly);

			org.w3c.dom.Element filter = doc.createElement("filter");
			filter.appendChild(doc.createTextNode("*.*"));
			paths.appendChild(filter);

			org.w3c.dom.Element subfolders = doc.createElement("subfolders");
			subfolders.appendChild(doc.createTextNode("0"));
			paths.appendChild(subfolders);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(
					codeFragmentsDirectory + cloneGroupNumber + "-"
							+ firstCloneNumber + "-" + secondCloneNumber
							+ ".WinMerge"));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	protected IFile getIFile(IProject iProject, String relativePath) {
		return iProject.getFile(relativePath);
	}

	public static IDocument getIDocument(IJavaElement iJavaElement) {
		ITextFileBufferManager bufferManager = FileBuffers
				.getTextFileBufferManager();
		IPath path = iJavaElement.getPath();
		IDocument iDocument = null;
		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(
					path, LocationKind.IFILE);
			iDocument = textFileBuffer.getDocument();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return iDocument;
	}

	public static CompilationUnit getCompilationUnitFromICompilationUnit(
			ICompilationUnit iCompilationUnit) {
		CompilationUnit cunit = null;
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(iCompilationUnit);
		parser.setResolveBindings(true);
		cunit = (CompilationUnit) parser.createAST(null);
		return cunit;
	}

	protected void createWinmergeFileAndWriteHyperlink(int groupID, int row,
			ExcelFileColumns col, int cloneCountInCurrentGroup) {
		int count = 0;
		for (int firstCloneNumber = 1; firstCloneNumber < cloneCountInCurrentGroup; firstCloneNumber++) {
			for (int secondCloneNumber = firstCloneNumber + 1; secondCloneNumber <= cloneCountInCurrentGroup; secondCloneNumber++) {
				String fileName = groupID + "-" + firstCloneNumber + "-"
						+ secondCloneNumber;
				addHyperlink(row - cloneCountInCurrentGroup + firstCloneNumber
						- 1, col.getColumnNumber() + count++,
						CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME + fileName
								+ ".winmerge", fileName);
				createWinMergeFile(groupID, firstCloneNumber, secondCloneNumber);
			}
			count = 0;
		}
	}

	protected void addCloneGroupGroup(CloneGroupGroup cgg) {
		cloneGroupGroups.add(cgg);
	}

	protected void writeCloneGroupInfo(int row, ExcelFileColumns groupInfoCol,
			ExcelFileColumns groupExtraInfoCol, CloneGroup cloneGroup) {
		// Check whether this is a subclone
		CloneGroupGroup scgg = getSubCloneGroupGroup(cloneGroup);
		if (scgg != null) {
			addLabel(row, groupInfoCol, "Subclone");
			addLabel(row, groupExtraInfoCol, scgg.getGIDs()[0]);
		}

		// Check whether this is a repeated clone
		CloneGroupGroup cgg = getCloneGroupGroup(cloneGroup);
		if (cgg != null) {
			cgg.addCloneGroup(cloneGroup);
			cloneGroup.addCloneGroupGroup(cgg);
			addLabel(row, groupInfoCol, "Repeated");
			addLabel(row, groupExtraInfoCol, cgg.getGIDs()[0]);
		} else {
			cgg = new CloneGroupGroup(cloneGroup);
			cloneGroup.addCloneGroupGroup(cgg);
			addCloneGroupGroup(cgg);
		}
	}

	protected CloneGroupGroup getCloneGroupGroup(CloneGroup _cloneGroup) {
		for (CloneGroupGroup cgg : cloneGroupGroups) {
			if (cgg.isConnected(_cloneGroup))
				return cgg;
		}

		return null;
	}

	protected CloneGroupGroup getSubCloneGroupGroup(CloneGroup _cloneGroup) {
		for (CloneGroupGroup cgg : cloneGroupGroups) {
			if (cgg.isSubClone(_cloneGroup))
				return cgg;
		}

		return null;
	}

	/**
	 * Contains ICompilationUnit and Source Directory of the gi we are looking
	 * for
	 * 
	 * @author Davood Mazinanian
	 */
	public static class ResourceInfo {
		private final String sourceFolder;
		private final ICompilationUnit iCompilationUnit;

		public ResourceInfo(String sourceFolder,
				ICompilationUnit iCompilationUnit) {
			this.sourceFolder = sourceFolder;
			this.iCompilationUnit = iCompilationUnit;
		}

		public String getSourceFolder() {
			return sourceFolder;
		}

		public ICompilationUnit getICompilationUnit() {
			return iCompilationUnit;
		}
	}

	public static ResourceInfo getResourceInfo(IJavaProject jProject,
			String fullResourceName) {
		try {

			// First try given path, if not found, prepend src dir
			ICompilationUnit iCompilationUnit = (ICompilationUnit) JavaCore
					.create(jProject.getProject().getFile(fullResourceName));

			
			Set<String> allSrcDirectories = SourceDirectoryUtility
					.getAllSourceDirectories(jProject);

			// if (fullResourceName.endsWith(".java"))
			// fullResourceName = fullResourceName.substring(0,
			// fullResourceName.length() - 5);
			// fullResourceName = fullResourceName.replace(".", "/") + ".java";

			if (iCompilationUnit != null && iCompilationUnit.exists()) {
				for (String srcDirectory : allSrcDirectories) {
					if (fullResourceName.startsWith(srcDirectory)) {
						return new ResourceInfo(srcDirectory, iCompilationUnit);
					}
				}
			}

			for (String srcDirectory : allSrcDirectories) {
				String fullPath = srcDirectory + "/" + fullResourceName;
				iCompilationUnit = (ICompilationUnit) JavaCore.create(jProject
						.getProject().getFile(fullPath));
				if (iCompilationUnit != null && iCompilationUnit.exists()) {
					return new ResourceInfo(srcDirectory, iCompilationUnit);
				}
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		LOGGER.warn(String.format("ICompilationUnit not found for %s",
				fullResourceName));
		return null;
	}

	public ICompilationUnit getICompilationUnit(String fullPathToTheResource) {
		return getResourceInfo(jProject, fullPathToTheResource)
				.getICompilationUnit();
	}

	public String getSourceFolder(String fullPathToTheResource) {
		return getResourceInfo(jProject, fullPathToTheResource)
				.getSourceFolder();
	}

	protected void progress(int percentage) {
		LOGGER.info(String.format("Parsed %s%% of %s", percentage,
				this.getMainInputFile()));
	}

	protected abstract String getMainInputFile();

	protected String readResultsFile(String filePath) {
		try {
			StringBuffer fileData;
			char[] buffer;
			int numRead = 0;
			String readData;

			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					filePath));

			fileData = new StringBuffer(1000);
			buffer = new char[1024];

			while ((numRead = bufferedReader.read(buffer)) != -1) {
				readData = String.valueOf(buffer, 0, numRead);
				fileData.append(readData);
				buffer = new char[1024];
			}

			bufferedReader.close();

			return fileData.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static boolean isWhiteSpaceCharacter(char character) {
		return character == ' ' || character == '\t' || character == '\n'
				|| character == '\r';
	}

	public static void addResourcePathsToExistingExcelFile(
			IJavaProject iJavaProject, File excelFile) throws Exception {

		// Make a copy of the original excel file
		Workbook originalWorkbook = Workbook.getWorkbook(excelFile);
		String copyWorkBookPath = excelFile.getParentFile().getAbsolutePath()
				+ "/" + excelFile.getName() + "-resourcepaths.xls";
		WritableWorkbook copyWorkbook = Workbook.createWorkbook(new File(
				copyWorkBookPath), originalWorkbook);
		Sheet originalSheet = originalWorkbook.getSheet(0);
		WritableSheet copySheet = copyWorkbook.getSheet(0);

		int numberOfRows = originalSheet.getRows();

		for (int rowNumber = 1; rowNumber < numberOfRows; rowNumber++) {

			String packageStr = originalSheet.getCell(
					ExcelFileColumns.PACKAGE_NAME.getColumnNumber(), rowNumber)
					.getContents();
			String className = originalSheet.getCell(
					ExcelFileColumns.CLASS_NAME.getColumnNumber(), rowNumber)
					.getContents();
			if (packageStr.equals("") || className.equals("")) {
				continue;
			}
			String fullName1 = originalSheet
					.getCell(ExcelFileColumns.PACKAGE_NAME.getColumnNumber(),
							rowNumber).getContents().replace(".", "/")
					+ "/"
					+ originalSheet.getCell(
							ExcelFileColumns.CLASS_NAME.getColumnNumber(),
							rowNumber).getContents() + ".java";

			String sourceDirectory = getResourceInfo(iJavaProject, fullName1)
					.getSourceFolder();

			Label label = new Label(
					ExcelFileColumns.SOURCE_FOLDER.getColumnNumber(),
					rowNumber, sourceDirectory);
			copySheet.addCell(label);
		}
		copyWorkbook.write();
		copyWorkbook.close();
	}
}
