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
import java.util.Arrays;
import java.util.Iterator;

public class CloneGroupGroup {

	private ArrayList<CloneGroup> cloneGroups = new ArrayList<CloneGroup>();
	private ArrayList<Clone> editedClones = new ArrayList<Clone>();
	
	public CloneGroupGroup(CloneGroup _cloneGroup) {
		cloneGroups.add(_cloneGroup);
	}
	
	public void addCloneGroup(CloneGroup _cloneGroup) {
		cloneGroups.add(_cloneGroup);
	}
	
	public ArrayList<CloneGroup> getCloneGroups() {
		return cloneGroups;
	}
	
	public boolean isConnected(CloneGroup _cloneGroup) {
		for (Iterator<CloneGroup> i = cloneGroups.iterator(); i.hasNext(); ) {
			CloneGroup cg = i.next();
			
			if (cg.getID() != _cloneGroup.getID()) {
				/*
				if (cg.isSubset(ca.concordia.jdeodorant.eclipse.commandline.test)) {
					return true;
				} else if (ca.concordia.jdeodorant.eclipse.commandline.test.isSubset(cg)) {
					return true;
				}
				*/
				
				if (cg.isConnected(_cloneGroup))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean isSubClone(CloneGroup _cloneGroup) {
		for (Iterator<CloneGroup> i = cloneGroups.iterator(); i.hasNext(); ) {
			CloneGroup cg = i.next();
			
			if (cg.getID() != _cloneGroup.getID()) {
				/*
				if (cg.isSubset(ca.concordia.jdeodorant.eclipse.commandline.test)) {
					return true;
				} else if (ca.concordia.jdeodorant.eclipse.commandline.test.isSubset(cg)) {
					return true;
				}
				*/
				
				if (cg.isSubClone(_cloneGroup))
					return true;
			}
		}
		
		return false;
	}
	
	public ArrayList<Clone> getEditedClones() {
		ArrayList<Clone> clones = getAllClones();
		
		editedClones.clear();
		
		int cloneCount = 1;
		
		for (Iterator<Clone> i = clones.iterator(); i.hasNext(); ) {
			Clone c = (Clone)i.next();
			
			boolean connected = false;
			
			for (Iterator<Clone> j = editedClones.iterator(); j.hasNext(); ) {
				EditedClone d = (EditedClone)j.next();
				
				if (c.isConnected(d)) {
					if (c.getStart() < d.getStart()) {
						d.setStart(c.getStart());
						//System.out.println("in here");
					}
					
					if (c.getEnd() > d.getEnd()) {
						//System.out.println("c: " + c.getEnd() + "; d: " + d.getEnd());
						d.setEnd(c.getEnd());
					}
					
					connected = true;
				}
			}
			
			if (!connected) {
				Clone newEditedClone = new EditedClone(cloneCount, c.getFileLocation(), c.getStart(), c.getEnd());
				cloneCount++;
				editedClones.add(newEditedClone);
			}
		}
		
		return editedClones;
	}
	
	public ArrayList<Clone> getAllClones() {
		ArrayList<Clone> clones = new ArrayList<Clone>();
		
		for (Iterator<CloneGroup> i = cloneGroups.iterator(); i.hasNext(); ) {
			CloneGroup cg = i.next();
			
			for (Iterator<Clone> j = cg.getClones().iterator(); j.hasNext(); ) {
				clones.add(j.next());
			}
		}
		
		return clones;	
	}
	
	public String toString() {
		String output = "";
		
		output += "==========================================================\n";
		output += "Clone Group Group (size: " + cloneGroups.size() + ")\n";
		output += "==========================================================\n\n";
		
		for (CloneGroup cg : cloneGroups) {
			output += cg;
		}
		
		return output;
	}
	
	public Integer[] getGIDs() {
		ArrayList<Integer> gids = new ArrayList<Integer>();
		
		for (Iterator<CloneGroup> i = cloneGroups.iterator(); i.hasNext(); ) {
			CloneGroup cg = i.next();
			
			gids.add(cg.getID());
		}
		
		return (Integer[])gids.toArray(new Integer[gids.size()]);
	}
	
	public boolean containsGID(int gid) {
		boolean found = false;
		
		for (CloneGroup group : cloneGroups) {
			if (group.getID() == gid)
				return true;
		}
		
		return found;
	}
	
	public String getTitle() {
		String output = "Groups ";
		
		/*
		Object[] gids = getGIDs();
		
		for (int i = 0; i < gids.length; i++)
			output += (Integer)gids[i] + " ";
			*/
		
		output += Arrays.toString(getGIDs());
		
		return output;
	}

	public ArrayList<Clone> getClones() {
		if (editedClones.size() > 0)
			return editedClones;
		else
			return getEditedClones();
	}

	public int getCloneCount() {
		return editedClones.size();
	}

}
