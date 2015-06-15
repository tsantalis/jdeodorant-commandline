/*******************************************************************************
 * Copyright (c) 2009 Software Composition and Modeling Laboratory (Softcom Lab)
 *                    Department of Computer and Information Sciences
 *                    University of Alabama at Birmingham
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Tairas - initial version
 *******************************************************************************/

package org.eclipse.contribution.cedar.elements.clones;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

public class Clone {

	private CloneGroup cloneGroup;
	private Path fileLocation;
	private int cloneID;
	protected int start;
	protected int end;
	private int topLevelTotal;
	private int type;
	private int subStartLine;
	private int subEndLine;
	private int subStartPosition;
	private int subLength;
	private boolean selected;

	public Clone(int _cloneID, CloneGroup _cloneGroup, Path _path, int _start, int _end) {
		cloneID = _cloneID;
		cloneGroup = _cloneGroup;
		fileLocation = _path;
		start = _start;
		end = _end;
		
		topLevelTotal = 0;
		type = 0;
		selected = true;
		
		resetSubInfo();
	}
	
	public CloneGroup getCloneGroup() {
		return cloneGroup;
	}

	public int getEnd() {
		return end;
	}

	public String getFileName() {
		return fileLocation.lastSegment();
	}
	
	public IFile getIFile() {
		String projectFile = getFileLocation().toString().replace(CeDARPlugin.project.getLocation().toString() + "/", "");
	
		return CeDARPlugin.project.getFile(projectFile);
	}

	public Path getFileLocation() {
		return fileLocation;
	}
	
	public int getCloneID() {
		return cloneID;
	}
	
	public void setCloneID(int _cloneID) {
		cloneID = _cloneID;
	}

	public int getStart() {
		return start;
	}
	
	public int getTopLevelTotal() {
		return topLevelTotal;
	}
	
	public void setTopLevelTotal(int i) {
		topLevelTotal = i;
	}
	
	public int getType() {
    	return type;
    }
    
    public void setType(int type) {
    	this.type = type;
    }
    
    public int getSubStartLine() {
    	return subStartLine;
    }
    
    public void setSubStartLine(int _subStartLine) {
    	subStartLine = _subStartLine;
    }
    
    public int getSubEndLine() {
    	return subEndLine;
    }
    
    public void setSubEndLine(int _subEndLine) {
    	subEndLine = _subEndLine;
    }
    
    public int getSubStartPosition() {
    	return subStartPosition;
    }
    
    public void setSubStartPosition(int _subStartPosition) {
    	subStartPosition = _subStartPosition;
    }
    
    public int getSubLength() {
    	return subLength;
    }
    
    public void setSubLength(int _subLength) {
    	subLength = _subLength;
    }
    
    public void resetSubInfo() {
    	subStartLine = 0;
    	subEndLine = 0;
    	subStartPosition = 0;
    	subLength = 0;
    }
    
    public boolean isSubSet() {
    	if (subStartLine == 0 && subEndLine == 0)
    		return false;
    	
    	return true;
    }
    
	public boolean isConnected(Clone _clone) {
		if (this.getFileLocation().equals(_clone.getFileLocation())) {
			if ((this.getStart() == _clone.getStart()) && (this.getEnd() == _clone.getEnd())) {
				return true;
/*			} else  if ((this.getStart() <= _clone.getStart()) && (this.getEnd() > _clone.getStart()) && (this.getEnd() <= _clone.getEnd())) {
				return true;
			} else if ((this.getStart() >= _clone.getStart()) && (this.getStart() <= _clone.getEnd()) && (this.getEnd() >= _clone.getEnd())) {
				return true;
			} else if ((this.getStart() <= _clone.getStart()) && (this.getEnd() >= _clone.getEnd())) {
				return true;
			} else if ((this.getStart() >= _clone.getStart()) && (this.getEnd() <= _clone.getEnd())) {
				return true;*/
			}
		}
		
		return false;
	}
	
	public boolean isSubClone(Clone _clone) {
		if (this.getFileLocation().equals(_clone.getFileLocation())) {
			/*if ((this.getStart() <= _clone.getStart()) && (this.getEnd() > _clone.getStart()) && (this.getEnd() <= _clone.getEnd())) {
				return true;
			} else if ((this.getStart() >= _clone.getStart()) && (this.getStart() <= _clone.getEnd()) && (this.getEnd() >= _clone.getEnd())) {
				return true;
			} else*/ 
			
			/*if ((this.getStart() == _clone.getStart()) && (this.getEnd() == _clone.getEnd())) {
				return false;
			}
			else */
			if(this.getType() == 4 || this.getType() == 5 || _clone.getType() == 4 || _clone.getType() == 5)
				return false;
			if ((this.getStart() <= _clone.getStart()) && (this.getEnd() >= _clone.getEnd())) {
				return true;
			} else if ((this.getStart() >= _clone.getStart()) && (this.getEnd() <= _clone.getEnd())) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setSelected(boolean _selected) {
		selected = _selected;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public String toString() {
		return fileLocation + " (" + start + " - " + end + ")\n";
	}

}
