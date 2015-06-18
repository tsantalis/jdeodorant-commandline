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
			String originalTestResultText = "null", newTestResultText = "null";
			if (originalTestReport != null)
				originalTestResultText = originalTestReport.getTestResult().toString();
			if (testReport != null)
				newTestResultText = testReport.getTestResult().toString();
			return String.format("%s -> %s", originalTestResultText, newTestResultText);
		}
		
	}

	private final Map<String, TestReport> testResults = new HashMap<>();
	
	public void addTestResult(TestReport testReport) {
		String key = testReport.getClassName() + "#" + testReport.getMethodName();
		if (!testResults.containsKey(key)) {
			testResults.put(key, testReport);
		} else {
			throw new RuntimeException(String.format("Test %s already exists in the results", key));
		}
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
		List<TestReportDifference> toReturn = new ArrayList<>();
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
