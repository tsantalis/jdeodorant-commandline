package ca.concordia.jdeodorant.eclipse.commandline.coverage;

public class LineCoverage {
	private String packageName;
	private String className;
	private int line;
	private CoverageStatus status;

	public LineCoverage(String packageName, String className, int line,
			CoverageStatus status) {
		this.setPackageName(packageName);
		this.setClassName(className);
		this.setLine(line);
		this.setStatus(status);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public CoverageStatus getStatus() {
		return status;
	}

	public void setStatus(CoverageStatus status) {
		this.status = status;
	}
}
