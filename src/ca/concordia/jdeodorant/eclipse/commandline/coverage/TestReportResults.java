package ca.concordia.jdeodorant.eclipse.commandline.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestReportResults {
	
	public class TestReportDifference {

		private final TestReport testReport;
		private final TestReport originalTestReport;

		public TestReportDifference(TestReport testReport, TestReport originalTestReport) {
			this.testReport = testReport;
			this.originalTestReport = originalTestReport;
		}
		
		public TestReport getTestReport() {
			return testReport;
		}

		public TestReport getOriginalTestReport() {
			return originalTestReport;
		}
		
		@Override
		public String toString() {
			return testReport + System.lineSeparator() + originalTestReport;
		}
		
	}

	private final Map<String, TestReport> testResults = new HashMap<>();
	
	public void addTestResult(TestReport testReport) {
		testResults.put(testReport.getClassName() + "#" + testReport.getMethodName(), testReport);
	}

	public boolean testResultsEqual(TestReportResults originalTestResults) {
		if (testResults.size() != originalTestResults.testResults.size())
			return false;
		for (String key : testResults.keySet()) {
			if (!testResults.get(key).getTestResult().equals(originalTestResults.testResults.get(key).getTestResult())) {
				return false;
			}
		}
		return true;
	}

	public List<TestReportDifference> compareTestResults(TestReportResults originalTestResults) {
		List<TestReportDifference> toReturn = new ArrayList<TestReportResults.TestReportDifference>();
		for (String key : testResults.keySet()) {
			
			TestReport testReport = testResults.get(key);
			TestReport originalTestReport = originalTestResults.testResults.get(key);
			
			if (!testReport.equals(originalTestReport)) {
				toReturn.add(new TestReportDifference(testReport, originalTestReport));
			}

		}
		
		return toReturn;
	}
}
