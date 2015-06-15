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

import org.eclipse.contribution.cedar.analysis.util.NodeFinder;
import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;

public class Visit extends IdentifyClone {
	
	protected int run(Clone _clone, CompilationUnit _cunit, int _startOffset, int _endOffset) {
		int result = 0;		

		startOffset = _startOffset;
		endOffset = _endOffset;
		clone = _clone;
		
		startNode = NodeFinder.perform(_cunit, startOffset, 1);
		endNode = NodeFinder.perform(_cunit, endOffset, 1);
		
		if(startNode != null) {
		startNode = getHighestNodeOfOffset(startNode, startOffset);
		endNode = getHighestNodeOfOffset(endNode, endOffset);
		
		result = isStatements();
		if (result > 0) {
			ASTNode startParent = startNode.getParent();
			ASTNode endParent = endNode.getParent();
			
			if (startParent == endParent)
				statements = getStatements(endNode);
				
			if (startParent == endParent.getParent())
				statements = getStatements(endParent);
			
			if (startParent == endParent.getParent().getParent())
				statements = getStatements(endParent.getParent());
			
			return result;
		}
		}
		startNode = NodeFinder.perform(_cunit, startOffset, endOffset - startOffset);
		
		if (startNode != null) {
			result = CloneType.convert(startNode.getNodeType());
			topNode = startNode;
		}
		
		return result;
	}
	
	private int isStatements() {
		if(startNode == null || endNode == null) 
			return 0;
		ASTNode startParent = startNode.getParent();
		ASTNode endParent = endNode.getParent();
		
		if (startParent instanceof Block) {
			if (startParent == endParent)
				return CloneType.STATEMENTS_CLONE;
			
			if (startParent == endParent.getParent())
				return CloneType.STATEMENTS_CLONE;
			
			/*if (startParent == endParent.getParent().getParent())
				return CloneType.STATEMENTS_CLONE;*/
		}
		
		
		return 0;
	}

}
