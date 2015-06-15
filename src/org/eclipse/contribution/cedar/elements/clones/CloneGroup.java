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

import java.util.ArrayList;
import java.util.Iterator;

public class CloneGroup implements IGroup {

	private int ID;
	private boolean taken;
	private CloneGroupGroup cgg;
	private ArrayList<Clone> clones = new ArrayList<Clone>();
	private boolean visible;

	public CloneGroup(int lineNumber, int _ID) {
		ID = _ID;
		visible = true;
	}

	public CloneGroup(int ID) {
		this.ID = ID;
		visible = true;
	}

	public void addCloneGroupGroup(CloneGroupGroup _cgg) {
		cgg = _cgg;
	}
	
	public void addClone(Clone clone) {
		clones.add(clone);
	}
	
	public CloneGroupGroup getCloneGroupGroup() {
		return cgg;
	}

	public ArrayList<Clone> getClones() {
		return clones;
	}
	
	public ArrayList<Clone> getSelectedClones() {
		ArrayList<Clone> selectedClones = new ArrayList<Clone>();
		
		for (Clone clone : clones) {
			if (clone.isSelected())
				selectedClones.add(clone);
		}
		
		return selectedClones;
	}

	public int getID() {
		return ID;
	}
	
	public String getTitle() {
		return "Group " + ID;
	}
	
	public int getCloneCount() {
		return clones.size();
	}
	
	public boolean isConnected(CloneGroup _cloneGroup) {
		boolean connected = false;
		
		if (this.getCloneCount() == _cloneGroup.getCloneCount()) {
			int cloneCount = this.getCloneCount();
			int count = 0;
			
			for (Iterator<Clone> i = this.getClones().iterator(); i.hasNext(); ) {
				Clone c = (Clone)i.next();
				
				boolean satisfy = false;
				
				for (Iterator<Clone> j = _cloneGroup.getClones().iterator(); j.hasNext(); ) {
					Clone d = (Clone)j.next();
					
					if (c.isConnected(d)) {
						count++;
						satisfy = true;
						break;
					}
				}
				
				if (!satisfy)
					break;
			}
			
			if (cloneCount == count)
				connected = true;
		}
		
		return connected;
	}

	public boolean isSubClone(CloneGroup _cloneGroup) {
		boolean connected = false;
		
		if (this.getCloneCount() == _cloneGroup.getCloneCount()) {
			int cloneCount = this.getCloneCount();
			int subCloneCount = 0;
			int connectedCount = 0;
			
			for (Iterator<Clone> i = this.getClones().iterator(); i.hasNext(); ) {
				Clone c = (Clone)i.next();
				
				boolean satisfy = false;
				
				for (Iterator<Clone> j = _cloneGroup.getClones().iterator(); j.hasNext(); ) {
					Clone d = (Clone)j.next();
					
					if (c.isSubClone(d)) {
						subCloneCount++;
						satisfy = true;
						break;
					}
					
					else if (c.isConnected(d)) {
						connectedCount++;
						satisfy = true;
						break;
					}
				}
				
				if (!satisfy)
					break;
			}
			
			if (connectedCount < cloneCount && cloneCount == subCloneCount+connectedCount)
				connected = true;
		}
		
		return connected;
	}
	
	public void setTaken() {
		taken = true;
	}

	public boolean isTaken() {
		return taken;
	}
	
	public String toString() {
		String output = "";
		
		output += "Clone Group (size: " + clones.size() + ")\n";
		output += "----------------------------------------------------------\n\n";
		
		for (Clone clone : clones) {
			output += clone;
		}
		
		return output;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean _visible) {
		visible = _visible;
	}

}