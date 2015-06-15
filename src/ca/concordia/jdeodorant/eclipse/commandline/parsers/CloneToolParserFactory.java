package ca.concordia.jdeodorant.eclipse.commandline.parsers;

import java.io.File;

import org.eclipse.jdt.core.IJavaProject;

import ca.concordia.jdeodorant.eclipse.commandline.parsers.ccfinder.CCFinderOutputParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.clonedr.CloneDROutputParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.conqat.ConQatOutputParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.dekard.DeckardOutputParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.nicad.NicadOutputParser;


public class CloneToolParserFactory {
	
	public enum CloneToolParserType {
		CLONE_TOOL_CCFINDER,
		CLONE_TOOL_CONQAT,
		CLONE_TOOL_DECKARD,
		CLONE_TOOL_NICAD,
		CLONE_TOOL_CLONEDR
	}

	public static CloneToolParser getCloneToolParser(CloneToolParserType tool, IJavaProject jProject, 
			File outputExcelFile, String mainFile, boolean launchApplication, String binFolder, String... otherArgs) {
		switch (tool) {
		case CLONE_TOOL_CCFINDER:
			return new CCFinderOutputParser(jProject, mainFile, otherArgs[0], otherArgs.length == 2 ? otherArgs[1] : "", outputExcelFile, launchApplication, binFolder);
		case CLONE_TOOL_CONQAT:
			return new ConQatOutputParser(jProject, mainFile, outputExcelFile, launchApplication, binFolder);
		case CLONE_TOOL_DECKARD:
			return new DeckardOutputParser(jProject, mainFile, outputExcelFile, launchApplication, binFolder);
		case CLONE_TOOL_NICAD:
			return new NicadOutputParser(jProject, mainFile, outputExcelFile, otherArgs.length == 1 ?  otherArgs[0] : "", launchApplication, binFolder);
		case CLONE_TOOL_CLONEDR:
			return new CloneDROutputParser(jProject, mainFile, outputExcelFile, otherArgs.length == 1 ?  otherArgs[0] : "", launchApplication, binFolder);
		default:
			throw new IllegalArgumentException("Not yet implemented.");
		}
	}

}