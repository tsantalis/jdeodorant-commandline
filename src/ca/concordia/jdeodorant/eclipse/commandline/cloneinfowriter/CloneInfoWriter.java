package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

import java.io.File;

public abstract class CloneInfoWriter {
	
	protected final String outputFileSaveFolder;
	protected final String projectName;
	protected String outputFileNamesPrefix;
	
	public CloneInfoWriter(String outputFolder, String projectName, String outputFileNamesPrefix) {
		outputFolder = outputFolder.replace("\\", "/");
		if (!outputFolder.endsWith("/"))
			outputFolder += "/";
		
		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		this.outputFileSaveFolder = outputFolder;
		this.projectName = projectName;
		this.outputFileNamesPrefix = outputFileNamesPrefix;
	}
	
	public abstract void writeCloneInfo(ClonePairInfo pairInfo);
	public abstract void closeMedia(boolean append);
	
}
