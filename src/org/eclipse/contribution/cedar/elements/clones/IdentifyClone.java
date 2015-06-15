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
import java.util.List;

import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jface.text.ITextSelection;

public abstract class IdentifyClone {

	protected static final boolean debug = false;
	protected int startOffset;
	protected Clone clone;
	protected int endOffset;
	protected ASTNode startNode;
	protected ASTNode endNode;
	protected ASTNode topNode;
	protected String cloneType;
	protected ITextSelection selection;
	protected List<ASTNode> statements;
	protected int result;

	public IdentifyClone() {
		cloneType = "";
		result = 0;
	}

	public int run(Clone _clone, CompilationUnit _cunit) {
		Offset line2offset = new Offset(_clone.getIFile(), _clone.getStart(), _clone.getEnd());
		line2offset.computeOffsets();
		
		int startOffset = line2offset.getStartOffset();
		int endOffset = line2offset.getEndOffset();
		int preEndOffset = line2offset.getPreEndOffset();
		int postEndOffset = line2offset.getPostEndOffset();
		
		result = run(_clone, _cunit, startOffset, endOffset);
		
		if (result == 999) {
			result = run(_clone, _cunit, startOffset, preEndOffset);
		}
		
		if (result == 999) {
			result = run(_clone, _cunit, startOffset, postEndOffset);
		}
		
		return result;
	}
	
	protected abstract int run(Clone _clone, CompilationUnit unit, int start, int end);
	
	public int getResult() {
		return result;
	}
	
	protected ASTNode getHighestNodeOfOffset(ASTNode node, int offset) {
		if (node == null || node.getParent() == null)
			return node;
		
		ASTNode parent = node.getParent();
		
		while (parent != null) {
			if (parent.getStartPosition() == offset) {
				node = parent;
			}
			parent = parent.getParent();
		}

		return node;
	}
	
	public int getStartOffset() {
		return startOffset;
	}
	
	public int getEndOffset() {
		return endOffset;
	}
	
	protected ASTNode getTopNode(ASTNode node, int nodeType) {
		while (node.getNodeType() != nodeType) {
			node = node.getParent();
		}
		
		return node;
	}
	                                                                
	protected static List<SwitchCase> getSwitchCases(SwitchStatement node) {
		List<SwitchCase> result = new ArrayList<SwitchCase>();
		@SuppressWarnings("unchecked")
		List<ASTNode> stmts = node.statements();
		for (ASTNode element : stmts) {
			if (element instanceof SwitchCase)
				result.add((SwitchCase)element);
		}
		return result;
	}
	
	protected List<ASTNode> getStatements(ASTNode node) {
		List<ASTNode> includedStmts = new ArrayList<ASTNode>();
		
		ASTNode startParent = startNode.getParent();
		if (startParent instanceof Block) {
			Block block = (Block)startParent;
			@SuppressWarnings("unchecked")
			List<ASTNode> stmts = block.statements();
			
			boolean start = false;
			
			for (ASTNode stmt : stmts) {
				if (!start)
					if (stmt == startNode)
						start = true;
				
				if (start)
					includedStmts.add(stmt);
				
				if (stmt == node)
					break;
					
			}
		}
		
		return includedStmts;
	}
	
	protected void printStartAndEndNodes() {
		if (startNode != null)
			System.out.println("start: " + NodeType.getString(startNode.getNodeType()) + " (" + startOffset + ")");
		else
			System.out.println("start: null");
		
		if (endNode != null)
			System.out.println("end  : " + NodeType.getString(endNode.getNodeType()) + " (" + endOffset + ")");
		else
			System.out.println("end  : null");
	}
	
	public ASTNode getTopNode() {
		return topNode;
	}
	
	public List<ASTNode> getStatements() {
		return statements;
	}
	
	public MethodDeclaration getMethodDeclaration() {
		ASTNode node = startNode;
		
		while (!(node instanceof MethodDeclaration)) {
			node = node.getParent();
			
			if (node == null)
				break;
			
			if (node instanceof MethodDeclaration)
				return (MethodDeclaration)node;
		}
		
		return null;
	}
	
	public ITextSelection getTextSelection () {
		return selection;
	}
	
}
