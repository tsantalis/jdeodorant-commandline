package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

public enum ClonePairLocation {
	SAME_METHOD ("Clones are in the same method"),
	SAME_DECLARING_CLASS ("Clones are declared in the same class"),
	SAME_JAVA_FILE ("Clones are in the same java file"),
	SAME_HIERARCHY ("Clones are in different classes having the same super class"),
	DIFFERENT_CLASSES ("Clones are in different classes");
	
	String description;
	
	private ClonePairLocation(String desc) {
		this.description = desc;
	}
	
	public String getDescription() {
		return this.description;
	}
}
