package ca.concordia.jdeodorant.eclipse.commandline.parsers;

public enum ExcelFileColumns {
	
	CLONE_GROUP_ID(0, "Clone Group ID"),
	SOURCE_FOLDER (1, "Source Folder"),
	PACKAGE_NAME(2, "Package"),
	CLASS_NAME(3, "Class"),
	METHOD_NAME(4, "Method"),
	METHOD_SIGNATURE(5, "Method Signature"),
	START_LINE(6, "Start Line"),
	END_LINE(7, "End Line"),
	START_OFFSET(8, "Start Offset"),
	END_OFFSET(9, "End Offset"),
	NUMBER_OF_PDG_NODES(10, "#PDG Nodes"),
	NUMBER_OF_STATEMENTS(11, "#Statements"),
	LINE_COVERAGE_PERCENTAGE(12,"Line Coverage (%)"),
	CLONE_GROUP_SIZE(13, "Clone Group Size"),
	//CLONE_TYPE(13, "Clone Type"),
	CLONE_GROUP_INFO(14, "Clone Group Info"),
	CONNTECTED(15, "Connected"),
	CLONE_LOCATION(16, "Clone Pair Location"),
	//NUMBER_OF_NODE_COMPARISONS(17, "#Node comparisons"),
	//EXECUTION_TIME(18, "Total execution time for group"),
	NUMBER_OF_REFACTORABLE_PAIRS(17, "#Refactorable Pairs"),
	DETAILS(18, "Details");

	
	private final int columnNumber;
	private final String columnTitle;
	
	private ExcelFileColumns(int col, String colTitle) {
		this.columnNumber = col;
		this.columnTitle = colTitle;
	}
	
	public int getColumnNumber() {
		return columnNumber;
	}
	
	public String getColumnTitle() {
		return columnTitle;
	}
	
	@Override
	public String toString() {
		return this.columnTitle + " (" + this.columnNumber + ")";
	}
}
