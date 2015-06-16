package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.concordia.jdeodorant.eclipse.commandline.coverage.TestReportResults.TestReportDifference;
import gr.uom.java.ast.decomposition.cfg.mapping.PDGRegionSubTreeMapper;

public class PDGSubTreeMapperInfo {
	private final PDGRegionSubTreeMapper mapper;
	private long timeElapsedToCalculate;
	
	private long wallNanoTimeElapsedForMapping;
	private List<TestReportDifference> testResultsDifferences = new ArrayList<>();
	private Set<String> filesHavingCompileErrors = new HashSet<>();
	private boolean refactoringNotOk;
	
	public long getWallNanoTimeElapsedForMapping() {
		return wallNanoTimeElapsedForMapping;
	}

	public void setWallNanoTimeElapsedForMapping(long wallNanoTime) {
		this.wallNanoTimeElapsedForMapping = wallNanoTime;
	}

	public PDGSubTreeMapperInfo(PDGRegionSubTreeMapper mapper) {
		this.mapper = mapper;
	}
	
	public PDGRegionSubTreeMapper getMapper() {
		return this.mapper;
	}

	/**
	 * Get time elapsed for only mapping phase (excluding the bottom-up subtree matching) 
	 * @return
	 */
	public long getTimeElapsedToMap() {
		return timeElapsedToCalculate;
	}

	/**
	 * Set time elapsed for only mapping phase (excluding the bottom-up subtree matching) 
	 * @return
	 */
	public void setTimeElapsedForMapping(long timeElapsedToCalculate) {
		this.timeElapsedToCalculate = timeElapsedToCalculate;
	}
	
	/**
	 * Is this mapper refactorable?
	 * @return
	 */
	public boolean isRefactorable() {
		return this.mapper != null && this.mapper.getPreconditionViolations().size() == 0 &&
				this.mapper.getRemovableNodesG1().size() > 0 && this.mapper.getRemovableNodesG2().size() > 0;
	}
	

	public void addFileHavingCompileError(String path) {
		this.filesHavingCompileErrors.add(path);
	}
	
	public Iterable<String> getFilesHavingCompileError() {
		return this.filesHavingCompileErrors ;		
	}

	public void setTestDifferences(List<TestReportDifference> compareTestResults) {
		this.testResultsDifferences = compareTestResults;
	}
	
	public List<TestReportDifference> getTestDifferences() {
		return this.testResultsDifferences;
	}

	public void setRefactoringNotOK() {
		refactoringNotOk = true;
	}
	
	public boolean getRefactoringWasOK() {
		return refactoringNotOk;
	}
	
	public boolean getHasCompileErrorsAfterRefactoring() {
		return filesHavingCompileErrors.size() > 0;
	}
	
	public boolean testsFailedAfterRefactoring() {
		return testResultsDifferences.size() > 0;
	}
}
