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

import org.eclipse.core.runtime.Path;

public class EditedClone extends Clone {
	
	public EditedClone(int cloneID, Path location, int start, int end) {
		super(cloneID, null, location, start, end);
	}

	public void setStart(int _start) {
		start = _start;
	}
	
	public void setEnd(int _end) {
		end = _end;
	}
	
}
