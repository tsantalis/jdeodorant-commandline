package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

import gr.uom.java.ast.decomposition.cfg.mapping.PDGRegionSubTreeMapper;

public class PDGSubTreeMapperInfo {
	private final PDGRegionSubTreeMapper mapper;
	private long timeElapsedToCalculate;
	
	private long wallNanoTimeElapsedForMapping;
	
	public long getWallNanoTimeElapsedForMapping() {
		return wallNanoTimeElapsedForMapping;
	}

	public void setWallNanoTimeElapsedForMapping(long wallNanoTime) {
		this.wallNanoTimeElapsedForMapping = wallNanoTime;
	}

	public PDGSubTreeMapperInfo(PDGRegionSubTreeMapper mapper) {
		this.mapper = mapper;
	}
	
	public PDGRegionSubTreeMapper getMapper() {
		return this.mapper;
	}

	/**
	 * Get time elapsed for only mapping phase (excluding the bottom-up subtree matching) 
	 * @return
	 */
	public long getTimeElapsedToMap() {
		return timeElapsedToCalculate;
	}

	/**
	 * Set time elapsed for only mapping phase (excluding the bottom-up subtree matching) 
	 * @return
	 */
	public void setTimeElapsedForMapping(long timeElapsedToCalculate) {
		this.timeElapsedToCalculate = timeElapsedToCalculate;
	}
	
	/**
	 * Is this mapper refactorable?
	 * @return
	 */
	public boolean isRefactorable() {
		return this.mapper != null && this.mapper.getPreconditionViolations().size() == 0 &&
				this.mapper.getRemovableNodesG1().size() > 0 && this.mapper.getRemovableNodesG2().size() > 0;
	}
	
}
