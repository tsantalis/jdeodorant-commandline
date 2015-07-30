package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

import gr.uom.java.ast.decomposition.cfg.mapping.CloneRefactoringType;
import gr.uom.java.ast.decomposition.cfg.mapping.PDGExpressionGap;
import gr.uom.java.ast.decomposition.cfg.mapping.PDGNodeBlockGap;
import gr.uom.java.ast.decomposition.cfg.mapping.precondition.PreconditionViolation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ITypeBinding;

import ca.concordia.jdeodorant.eclipse.commandline.coverage.TestReportResults.TestReportDifference;

public class CloneInfoCSVWriter extends CloneInfoWriter {

	private static final String SEPARATOR = "|";

	private static final String GLOBAL_CSV_FILE_NAME = "report.csv";
	private static final String PRECOND_VIOLATIONS_FILE_NAME = "precondviolations.csv";
	private static final String MAPPERS_FILE_NAME = "trees.csv";
	private static final String COMPILE_ERRORS_FILE_NAME = "compileerrors.csv";
	private static final String TEST_DIFFERENCES_FILE_NAME = "testdifferences.csv";
	private static final String EXPRESSION_GAPS_INFO_FILE_NAME = "exprgapsinfo.csv";
	private static final String BLOCK_GAPS_INFO_FILE_NAME = "blockgapsinfo.csv";

	private final List<String> mainCSVLines = new ArrayList<>();
	private final List<String> mappersCSVLines = new ArrayList<>();
	private final List<String> preconditionViolationsLines = new ArrayList<>();
	private final List<String> compileErrorsLines = new ArrayList<>();
	private final List<String> testReportDifferencesLines = new ArrayList<>();
	private final List<String> expressionGapsInfoLines = new ArrayList<>();
	private final List<String> blockGapsInfoLines = new ArrayList<>();

	public static final String PATH_TO_CSV_FILES = "";

	public CloneInfoCSVWriter(String outputFolder, String projectName, String fileNamePrefix) {
		super(outputFolder + "/" + PATH_TO_CSV_FILES, projectName, fileNamePrefix);
		createHeadersForFiles();
	}

	private void createHeadersForFiles() {
		mainCSVLines.add("GroupID|PairID|ClonePairLocation|IsTestCode|" +
				"#StatementsInCloneFragment1|#StatementsInCloneFragment2|#NodeComparisons|#PDGNodesInMethod1|#PDGNodesInMethod2|" +
				"#RefactorableSubtrees|SubtreeMatchingWallNanoTime|Status");
		mappersCSVLines.add("GroupID|PairID|TreeID|CloneType|PDGMappingWallNanoTime|#PreconditionViolations|#MappedStatements|" +
				"#UnMappedStatements1|#UnMappedStatements2|#Differences|RefactoringWasOK|TestsFailedAfterRefactoring|HadCompileErrorsAfterRefactoring|CloneRefactoringType");
		preconditionViolationsLines.add("GroupID|PairID|TreeID|PreconditionViolationType");
		compileErrorsLines.add("GroupID|PairID|TreeID|FileHavingCompileError");
		testReportDifferencesLines.add("GroupID|PairID|TreeID|TestDifference");
		expressionGapsInfoLines.add("GroupID|PairID|TreeID|#Params|ReturnType|#ThrownExceptions|#NonEffectiveFinalVars");
		blockGapsInfoLines.add("GroupID|PairID|TreeID|#Params|ReturnType|#ThrownExceptions|#Statements1|#Statements2");
	}

	@Override
	public void writeCloneInfo(ClonePairInfo pairInfo) {

		StringBuilder line = new StringBuilder();

		line.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
		line.append(pairInfo.getClonePairID()).append(SEPARATOR);
		line.append(pairInfo.getLocation().ordinal()).append(SEPARATOR);
		line.append(pairInfo.pairTestCodeInfo().ordinal()).append(SEPARATOR);
		line.append(pairInfo.getNumberOfCloneStatementsInFirstCodeFragment()).append(SEPARATOR);
		line.append(pairInfo.getNumberOfCloneStatementsInSecondCodeFragment()).append(SEPARATOR);
		line.append(pairInfo.getNumberOfNodeComparisons()).append(SEPARATOR);
		line.append(pairInfo.getNumberOfPDGNodesInFirstMethod()).append(SEPARATOR);
		line.append(pairInfo.getNumberOfPDGNodesInSecondMethod()).append(SEPARATOR);
		line.append(pairInfo.getRefactorableMappersInfo().size()).append(SEPARATOR);
		line.append(pairInfo.getSubtreeMatchingWallNanoTime()).append(SEPARATOR);
		line.append(pairInfo.getStatus().ordinal());
		mainCSVLines.add(line.toString());

		int treeID = 0;
		for (PDGSubTreeMapperInfo mapperInfo : pairInfo.getPDFSubTreeMappersInfoList()) {
			treeID++;
			line = new StringBuilder();
			line.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
			line.append(pairInfo.getClonePairID()).append(SEPARATOR);
			line.append(treeID).append(SEPARATOR);
			line.append(mapperInfo.getMapper().getCloneType().ordinal() + 1).append(SEPARATOR);
			line.append(mapperInfo.getWallNanoTimeElapsedForMapping()).append(SEPARATOR);
			line.append(mapperInfo.getMapper().getPreconditionViolations().size()).append(SEPARATOR);
			line.append(mapperInfo.getMapper().getRemovableNodesG1().size()).append(SEPARATOR);
			line.append(mapperInfo.getMapper().getRemainingNodesG1().size()).append(SEPARATOR);
			line.append(mapperInfo.getMapper().getRemainingNodesG2().size()).append(SEPARATOR);
			if (mapperInfo.getMapper().getMaximumStateWithMinimumDifferences() != null)
				line.append(mapperInfo.getMapper().getNodeDifferences().size());
			else
				line.append("N/A");
			line.append(SEPARATOR);
			line.append(mapperInfo.getRefactoringWasOK()).append(SEPARATOR);
			line.append(mapperInfo.testsFailedAfterRefactoring()).append(SEPARATOR);
			line.append(mapperInfo.getHasCompileErrorsAfterRefactoring()).append(SEPARATOR);
			CloneRefactoringType cloneRefactoringType = mapperInfo.getMapper().getCloneRefactoringType();
			if (cloneRefactoringType != null)
				line.append(cloneRefactoringType.ordinal());
			else 
				line.append(-1);
			mappersCSVLines.add(line.toString());

			for (PreconditionViolation pv : mapperInfo.getMapper().getPreconditionViolations()) {
				line = new StringBuilder();
				line.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
				line.append(pairInfo.getClonePairID()).append(SEPARATOR);
				line.append(treeID).append(SEPARATOR);
				line.append(pv.getType().ordinal());
				preconditionViolationsLines.add(line.toString());
			}

			for (String fileHavingCompileError : mapperInfo.getFilesHavingCompileError()) {
				StringBuilder compileErrorsLine = new StringBuilder();
				compileErrorsLine.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
				compileErrorsLine.append(pairInfo.getClonePairID()).append(SEPARATOR);
				compileErrorsLine.append(treeID).append(SEPARATOR);
				compileErrorsLine.append(fileHavingCompileError);
				compileErrorsLines.add(compileErrorsLine.toString());
			}

			for (TestReportDifference testReportDifference : mapperInfo.getTestDifferences()) {
				StringBuilder testReportDifferencesLine = new StringBuilder();
				testReportDifferencesLine.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
				testReportDifferencesLine.append(pairInfo.getClonePairID()).append(SEPARATOR);
				testReportDifferencesLine.append(treeID).append(SEPARATOR);
				testReportDifferencesLine.append(testReportDifference.toString());
				testReportDifferencesLines.add(testReportDifferencesLine.toString());
			}

			if (mapperInfo.getMapper().getMaximumStateWithMinimumDifferences() != null) {

				for (PDGNodeBlockGap pdgNodeBlockGap : mapperInfo.getMapper().getRefactorableBlockGaps()) {
					StringBuilder gapInfoLine = new StringBuilder();
					gapInfoLine.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
					gapInfoLine.append(pairInfo.getClonePairID()).append(SEPARATOR);
					gapInfoLine.append(treeID).append(SEPARATOR);
					gapInfoLine.append(pdgNodeBlockGap.getParameterBindings().size()).append(SEPARATOR);
					ITypeBinding returnType = pdgNodeBlockGap.getReturnType();
					gapInfoLine.append(returnType != null ? returnType.getQualifiedName() : "void").append(SEPARATOR);
					gapInfoLine.append(pdgNodeBlockGap.getThrownExceptions().size()).append(SEPARATOR);
					gapInfoLine.append(pdgNodeBlockGap.getNodesG1().size()).append(SEPARATOR);
					gapInfoLine.append(pdgNodeBlockGap.getNodesG2().size());
					blockGapsInfoLines.add(gapInfoLine.toString());
				}

				for (PDGExpressionGap pdgExpressionGap : mapperInfo.getMapper().getRefactorableExpressionGaps()) {
					StringBuilder gapInfoLine = new StringBuilder();
					gapInfoLine.append(pairInfo.getCloneGroupID()).append(SEPARATOR);
					gapInfoLine.append(pairInfo.getClonePairID()).append(SEPARATOR);
					gapInfoLine.append(treeID).append(SEPARATOR);
					gapInfoLine.append(pdgExpressionGap.getParameterBindings().size()).append(SEPARATOR);
					gapInfoLine.append(pdgExpressionGap.getReturnType().getQualifiedName()).append(SEPARATOR);
					gapInfoLine.append(pdgExpressionGap.getThrownExceptions().size()).append(SEPARATOR);
					gapInfoLine.append(pdgExpressionGap.getNonEffectivelyFinalLocalVariableBindings().size());
					expressionGapsInfoLines.add(gapInfoLine.toString());
				}

			}

		}
	}

	/**
	 * Writes one line per each object in the given List.
	 * 
	 * @param lines
	 * @param path
	 * @param append
	 *            If set to true, we skip the first line in the List (the header
	 *            line)
	 */
	public static void writeLinesToFile(List<?> lines, String path, boolean append) {
		try {

			BufferedWriter fw = openFile(path, append);

			for (int i = append ? 1 : 0; i < lines.size(); i++) {
				writeFile(fw, lines.get(i).toString());
			}

			closeFile(fw);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static BufferedWriter openFile(String path, boolean append) throws IOException {
		File f = new File(path);
		BufferedWriter fw = new BufferedWriter(new FileWriter(f, append));
		return fw;
	}

	public static void closeFile(BufferedWriter fw) throws IOException {
		fw.close();
	}

	public static void writeFile(BufferedWriter fw, String line) throws IOException {
		fw.append(line + System.lineSeparator());
	}

	@Override
	public void closeMedia(boolean append) {
		String filePrefix = this.outputFileSaveFolder + outputFileNamesPrefix + ".";
		writeLinesToFile(mainCSVLines, filePrefix + GLOBAL_CSV_FILE_NAME, append);
		writeLinesToFile(mappersCSVLines, filePrefix + MAPPERS_FILE_NAME, append);
		writeLinesToFile(preconditionViolationsLines, filePrefix + PRECOND_VIOLATIONS_FILE_NAME, append);
		writeLinesToFile(compileErrorsLines, filePrefix + COMPILE_ERRORS_FILE_NAME, append);
		writeLinesToFile(testReportDifferencesLines, filePrefix + TEST_DIFFERENCES_FILE_NAME, append);
		writeLinesToFile(expressionGapsInfoLines, filePrefix + EXPRESSION_GAPS_INFO_FILE_NAME, append);
		writeLinesToFile(blockGapsInfoLines, filePrefix + BLOCK_GAPS_INFO_FILE_NAME, append);
	}

}
