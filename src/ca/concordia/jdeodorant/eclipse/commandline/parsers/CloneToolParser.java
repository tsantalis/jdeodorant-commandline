package ca.concordia.jdeodorant.eclipse.commandline.parsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.concordia.jdeodorant.clone.parsers.CloneDetectorOutputParser;
import ca.concordia.jdeodorant.clone.parsers.CloneDetectorOutputParserFactory;
import ca.concordia.jdeodorant.clone.parsers.CloneDetectorOutputParserProgressObserver;
import ca.concordia.jdeodorant.clone.parsers.CloneDetectorType;
import ca.concordia.jdeodorant.clone.parsers.CloneGroup;
import ca.concordia.jdeodorant.clone.parsers.CloneGroupList;
import ca.concordia.jdeodorant.clone.parsers.CloneInstance;
import ca.concordia.jdeodorant.clone.parsers.CloneInstanceLocationInfo;
import ca.concordia.jdeodorant.clone.parsers.InvalidInputFileException;
import ca.concordia.jdeodorant.eclipse.commandline.ApplicationRunner;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.CoverageStatus;
import ca.concordia.jdeodorant.eclipse.commandline.coverage.LineCoverage;
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

public class CloneToolParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(CloneToolParser.class);
	
	public final static String CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME = "code-fragments";

	private WritableWorkbook workbook;
	private WritableSheet sheet;
	
	private final String excelFileParentDirectory;
	private final String codeFragmentsDirectory;

	private CloneDetectorOutputParser cloneToolParser;
	
	private List<LineCoverage> lineCoverageInfo = null;

	public CloneToolParser(CloneToolParserType toolType, IJavaProject jProject, File excelFile, 
			String toolOutputMainFile, boolean coverageReport, String binFolder, String... otherArgs) throws InvalidInputFileException {
		CloneDetectorType cloneDetectorType = toolType.getCloneDetectorType();
		this.excelFileParentDirectory = replaceBackSlasheshWithSlashesh(excelFile.getParent()) + "/";
		this.codeFragmentsDirectory = this.excelFileParentDirectory + "/" + CODE_FRAGMENTS_TXT_FILES_FOLDER_NAME + "/";
		File outputFolder = new File(this.codeFragmentsDirectory);
		if (!outputFolder.exists())
			outputFolder.mkdir();
		//this.projectName = jProject.getElementName();
		createExcelFile(excelFile);
		
		if (coverageReport) {
			ApplicationRunner runner;
			try {
				runner = new ApplicationRunner(jProject, binFolder, excelFileParentDirectory);
				LOGGER.info("Started running tests and getting test coverage");
				runner.launchApplication();
				LOGGER.info("Finished running tests and getting test coverage");
				lineCoverageInfo = ApplicationRunner.readCoverageFile(excelFileParentDirectory);
			} catch (IOException | CoreException e) {
				e.printStackTrace();
			}
		}
		
		cloneToolParser = CloneDetectorOutputParserFactory.getCloneToolParser(cloneDetectorType, jProject, toolOutputMainFile, otherArgs);
	}
	
	protected void writeCoverageInfo(String packageName, String className, int cloneRow, int startLine, int endLine) {		
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
			if (allLinesCount != 0) {
				lineCoverage = (float)coveredLinesCount / allLinesCount;
			}
			Number number = new Number(ExcelFileColumns.LINE_COVERAGE_PERCENTAGE.getColumnNumber(), cloneRow, lineCoverage);
			try {
				sheet.addCell(number);
			} catch (WriteException e) {
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
		WritableFont boldFont = new WritableFont(WritableFont.TIMES, 11, WritableFont.BOLD);
		WritableCellFormat boldFormat = new WritableCellFormat(boldFont);
		for (ExcelFileColumns value : ExcelFileColumns.values()) {
			Label label = new Label(value.getColumnNumber(), 0, value.getColumnTitle(), boldFormat);
			try {
				sheet.addCell(label);
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}

	private void fillInCloneExcelFile() throws InvalidInputFileException {
		
		cloneToolParser.addParserProgressObserver(new  CloneDetectorOutputParserProgressObserver() {
			@Override
			public void notify(int cloneGroupIndex) {
				int percentage = (int)(cloneGroupIndex / (double)cloneToolParser.getCloneGroupCount() * 100);
				if (percentage % 10 == 0)
					LOGGER.info(String.format("%d%%: Parsing %s", percentage, cloneToolParser.getToolOutputFilePath()));
			}
		});
		CloneGroupList cloneGroupList = cloneToolParser.readInputFile();
		
		int rowNumber = 1;
		for (CloneGroup cloneGroup : cloneGroupList.getCloneGroups()) {
			List<CloneInstance> cloneInstances = cloneGroup.getCloneInstances();
			int cloneNumber = 1;
			for (CloneInstance cloneInstance : cloneInstances) {
				addLabel(rowNumber, ExcelFileColumns.CLONE_GROUP_ID, cloneGroup.getCloneGroupID());
				addLabel(rowNumber, ExcelFileColumns.PACKAGE_NAME, cloneInstance.getPackageName());
				addLabel(rowNumber, ExcelFileColumns.CLASS_NAME, cloneInstance.getClassName());
				addLabel(rowNumber, ExcelFileColumns.SOURCE_FOLDER, cloneInstance.getSourceFolder());
				addLabel(rowNumber, ExcelFileColumns.METHOD_NAME, cloneInstance.getMethodName()); 
				addLabel(rowNumber, ExcelFileColumns.METHOD_SIGNATURE, cloneInstance.getIMethodSignature());
				CloneInstanceLocationInfo locationInfo = cloneInstance.getLocationInfo();
				addLabel(rowNumber, ExcelFileColumns.START_LINE, locationInfo.getStartLine());
				addLabel(rowNumber, ExcelFileColumns.END_LINE, locationInfo.getEndLine());
				addLabel(rowNumber, ExcelFileColumns.START_OFFSET, locationInfo.getStartOffset());
				addLabel(rowNumber, ExcelFileColumns.END_OFFSET, locationInfo.getEndOffset());
				writeCoverageInfo(cloneInstance.getPackageName(), cloneInstance.getClassName(), 
						rowNumber, locationInfo.getStartLine(), locationInfo.getEndLine());				
				createTextFileFromOffset(cloneInstance.getOriginalCodeFragment(), cloneGroup.getCloneGroupID() + "-" + cloneNumber++ + ".txt");
				rowNumber++;
			}
			if (cloneGroup.isSubClone()) {
				addLabel(rowNumber - cloneInstances.size(), ExcelFileColumns.CLONE_GROUP_INFO, "Subclone");
				addLabel(rowNumber - cloneInstances.size(), ExcelFileColumns.CONNTECTED, cloneGroup.getSubcloneOf().getCloneGroupID());
			}
			addLabel(rowNumber - cloneInstances.size(), ExcelFileColumns.CLONE_GROUP_SIZE, cloneInstances.size());
			addLabel(rowNumber - cloneInstances.size(), ExcelFileColumns.CLONE_LOCATION, cloneGroup.getClonesRelativeLocation().toString());
		}
	}

	private String replaceBackSlasheshWithSlashesh(String string) {
		return string.replace("\\", "/");
	}

	public void execute() throws InvalidInputFileException {
		fillInCloneExcelFile();
		try {
			workbook.write();
			workbook.close();
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
	}

	private void addLabel(int row, ExcelFileColumns col, String labelString) {
		Label label = new Label(col.getColumnNumber(), row, labelString);
		addCell(label);
	}

	private void addLabel(int row, ExcelFileColumns col, java.lang.Number number) {
		Number label = new Number(col.getColumnNumber(), row, Double.valueOf(number.toString()));
		addCell(label);
	}

	protected void addHyperlink(int row, int col, File file, String description) {
		WritableHyperlink fileLink = new WritableHyperlink(col, row, file, description);
		try {
			sheet.addHyperlink(fileLink);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	protected void addHyperlink(int row, int col, String path, String description) {
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

	private void createTextFileFromOffset(String code, String fileName) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File(codeFragmentsDirectory + fileName));
			writer.write(code);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void createWinMergeFile(long cloneGroupNumber, int firstCloneNumber, int secondCloneNumber) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			org.w3c.dom.Document doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = doc.createElement("project");
			doc.appendChild(rootElement);

			org.w3c.dom.Element paths = doc.createElement("paths");
			rootElement.appendChild(paths);

			org.w3c.dom.Element left = doc.createElement("left");
			left.appendChild(doc.createTextNode(codeFragmentsDirectory + cloneGroupNumber + "-" + firstCloneNumber + ".txt"));
			paths.appendChild(left);

			org.w3c.dom.Element leftreadonly = doc.createElement("left-readonly");
			leftreadonly.appendChild(doc.createTextNode("1"));
			paths.appendChild(left);

			org.w3c.dom.Element right = doc.createElement("right");
			right.appendChild(doc.createTextNode(codeFragmentsDirectory + cloneGroupNumber + "-" + secondCloneNumber + ".txt"));
			paths.appendChild(right);

			org.w3c.dom.Element rightreadonly = doc.createElement("right-readonly");
			rightreadonly.appendChild(doc.createTextNode("1"));
			paths.appendChild(rightreadonly);

			org.w3c.dom.Element filter = doc.createElement("filter");
			filter.appendChild(doc.createTextNode("*.*"));
			paths.appendChild(filter);

			org.w3c.dom.Element subfolders = doc.createElement("subfolders");
			subfolders.appendChild(doc.createTextNode("0"));
			paths.appendChild(subfolders);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(
					new File(codeFragmentsDirectory + cloneGroupNumber + "-"
							+ firstCloneNumber + "-" + secondCloneNumber
							+ ".WinMerge"));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	protected void createWinmergeFileAndWriteHyperlink(int groupID, int row, ExcelFileColumns col, int cloneCountInCurrentGroup) {
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

}
