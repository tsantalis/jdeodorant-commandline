package ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.model;

public class ClonesetAndFileIndex extends CloneSet {
	public final int fileIndex;
	
	public ClonesetAndFileIndex(long id, int length, int fileIndex) {
		super(id, length);
		this.fileIndex = fileIndex;
	}
}
