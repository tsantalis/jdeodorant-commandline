package ca.concordia.jdeodorant.eclipse.commandline.coverage;

public class TestReport {

	public enum TestResult {
		FINISHED,
		FAILED
	}

	private final String className;
	private final String methodName;
	private final TestResult testResult;
	
	public TestReport(String className, String methodName, String resultString) {
		this.className = className;
		this.methodName = methodName;
		this.testResult = TestResult.valueOf(resultString.toUpperCase());
	}
	
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public TestResult getTestResult() {
		return testResult;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result
				+ ((testResult == null) ? 0 : testResult.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestReport other = (TestReport) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (testResult != other.testResult) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s#%s (%s)", className, methodName, testResult);
	}

}
