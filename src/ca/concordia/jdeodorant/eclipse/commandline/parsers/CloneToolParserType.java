package ca.concordia.jdeodorant.eclipse.commandline.parsers;

import ca.concordia.jdeodorant.clone.parsers.CloneDetectorType;

public enum CloneToolParserType {
	
	CLONE_TOOL_CCFINDER(CloneDetectorType.CCFINDER),
	CLONE_TOOL_CONQAT(CloneDetectorType.CONQAT),
	CLONE_TOOL_DECKARD(CloneDetectorType.DECKARD),
	CLONE_TOOL_NICAD(CloneDetectorType.NICAD),
	CLONE_TOOL_CLONEDR(CloneDetectorType.CLONEDR);
	
	private final CloneDetectorType detectorType;
	private CloneToolParserType(CloneDetectorType detectorType) {
		this.detectorType = detectorType;
	}
	
	public CloneDetectorType getCloneDetectorType() {
		return detectorType;
	}
}
