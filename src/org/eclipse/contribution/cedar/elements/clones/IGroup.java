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

public interface IGroup {
	
	public ArrayList<Clone> getClones();
	
	public ArrayList<Clone> getSelectedClones();

	public String getTitle();
	
	public int getCloneCount();
	
	public CloneGroupGroup getCloneGroupGroup();
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
}