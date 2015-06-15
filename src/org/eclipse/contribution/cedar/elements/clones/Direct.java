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

import java.util.Iterator;
import java.util.List;

import org.eclipse.contribution.cedar.analysis.util.NodeFinder;
import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;

public class Direct extends IdentifyClone {
	
	protected int run(Clone _clone, CompilationUnit _cunit, int _startOffset, int _endOffset) {
		startOffset = _startOffset;
		endOffset = _endOffset;
		clone = _clone;
		
		startNode = NodeFinder.perform(_cunit, startOffset, 1);
		endNode = NodeFinder.perform(_cunit, endOffset, 1);
		
		if (startNode == null || endNode == null) {
			System.out.println("startNode or endNode is null");
			return 0;
		}
		
		/*
		selection = new TextSelection(startOffset, endOffset);
		
		SelectionAnalyzer statementAnalyzer;
		try {
			statementAnalyzer = new StatementAnalyzer(selection, true);

			unit.accept(statementAnalyzer);
			
			IRegion region = statementAnalyzer.getSelectedNodeRange();
			
			if (region == null)
				return 0;


		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		*/

		if (debug) {
			printStartAndEndNodes();			
		}
		
		startNode = getHighestNodeOfOffset(startNode, startOffset);
		endNode = getHighestNodeOfOffset(endNode, endOffset);
		
		if (debug) {
			System.out.println("\nAfter evaluating parents:");
			printStartAndEndNodes();
		}
		
		return computeResult();
	}

	protected int computeResult() {
		int result = 0;
		
		result = isMethod();
		if (result > 0) {
			/*
			System.out.println("\nLines " + start + " - " + end + " is a method.");

			ParameterVisitor parameterVisitor = new ParameterVisitor();
			startNode.accept(parameterVisitor);
			
			LocalVariableVisitor localVisitor = new LocalVariableVisitor();
			startNode.accept(localVisitor);
			*/
			
			topNode = getTopNode(startNode, ASTNode.METHOD_DECLARATION);
			
			return result;
		}
		
		result = isIf();
		if (result > 0) {
			topNode = getTopNode(startNode, ASTNode.IF_STATEMENT);
			
			return result;
		}
		
		result = isClass();
		if (result > 0) {
			topNode = startNode;
			
			return result;
		}

		result = isTry();
		if (result > 0) {
			topNode = getTopNode(startNode, ASTNode.TRY_STATEMENT);
			return result;
		}

		result = isCatch();
		if (result > 0)
			return result;
		
		result = isLoop();
		if (result > 0) {
			switch(result) {
			case CloneType.FOR_CLONE:
				topNode = getTopNode(startNode, ASTNode.FOR_STATEMENT);
				break;
			case CloneType.WHILE_CLONE:
				topNode = getTopNode(startNode, ASTNode.WHILE_STATEMENT);
			}
			
			return result;
		}
		
		result = isCase();
		if (result > 0)
			return result;
		
		result = isSwitch();
		if (result > 0)
			return result;
		
		result = isBlock();
		if (result > 0) {
			topNode = getTopNode(startNode, ASTNode.BLOCK);
			
			String projectFile = clone.getFileLocation().toString().replace(CeDARPlugin.project.getLocation().toString() + "/", "");
			IFile file = CeDARPlugin.project.getFile(projectFile);
			
			Line line = new Line(file);
			
			try {
				if (Math.abs((line.getLine(topNode.getStartPosition() + topNode.getLength()) - line.getLine(topNode.getStartPosition())) - (clone.getEnd() - clone.getStart())) > 4)
					return result;
				else
					return 999;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
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
		
		return result;
	}
	
	private int isMethod() {
		if (startNode instanceof MethodDeclaration && endNode instanceof Block)
				if (endNode.getParent() == startNode)
					return CloneType.METHOD_CLONE;
		
		if (startNode.getParent() != null)
			if (startNode.getParent() instanceof MethodDeclaration && (endNode instanceof Block))
				return CloneType.METHOD_CLONE;
		
		return 0;
	}
	
	private int isIf() {
		if (startNode instanceof IfStatement) {
			ASTNode endIf = endNode.getParent();

			/*
			 * [S]if () {
			 * } else if {
			 * } else {
			 *   [E]
			 * }
			 *
			 */
			while (endIf instanceof IfStatement) {
				if (startNode == endIf)
					return CloneType.IF_CLONE;
				endIf = endIf.getParent();
			}

			/*
			 * [S]if () {
			 *   s1;
			 *   s2;
			 *   [E]
			 * }
			 *
			 */
			if (endNode instanceof Block) {
				if (endOfLastStmt(startNode))
					return CloneType.IF_CLONE;
				
				/*
				 * [S]if () {
				 * }[E]
				 *
				 */
				//if (startNode.getParent() == endNode)
				//	return CloneType.IF_CLONE;
			}
		}
		
		if (startNode instanceof Block && endNode instanceof Block) {
			if (startNode.getParent() instanceof IfStatement && endNode.getParent() instanceof IfStatement) {
				ASTNode startIf = startNode.getParent();
				ASTNode endIf = endNode.getParent();
				
				while (endIf instanceof IfStatement) {
					if (startIf == endIf)
						return CloneType.IF_CLONE;
					endIf = endIf.getParent();
				}
			}
		}
		
		return 0;
	}
	
	private int isTry() {
		if ((startNode instanceof TryStatement) && (endNode instanceof Block)) {
			if (endNode.getParent() == startNode)
				return CloneType.TRY_CLONE;
			else if (endNode.getParent() instanceof CatchClause)
				if (startNode == endNode.getParent().getParent())
					return CloneType.TRY_CLONE;
		}
		
		return 0;
	}
	
	private int isCatch() {
		if (startNode instanceof Block && endNode instanceof Block)
			if (startNode.getParent() instanceof TryStatement && endNode.getParent() instanceof CatchClause)
				if (endNode.getParent().getParent() == startNode.getParent())
					return CloneType.CATCH_CLONE;
		
		return 0;
	}
	
	private int isClass() {
		if ((startNode instanceof PackageDeclaration || startNode instanceof ImportDeclaration || startNode instanceof CompilationUnit) && (endNode instanceof TypeDeclaration))
			if (endNode.getParent().getParent() == null)
				return CloneType.CLASS_CLONE;
		
		if (startNode instanceof Modifier && startNode.getParent() instanceof TypeDeclaration) {
			if (endNode instanceof TypeDeclaration)
				if (startNode.getParent() == endNode)
					return CloneType.CLASS_CLONE;
			
			if (endNode instanceof Block)
				if (endOfLastDecl())
					return CloneType.CLASS_CLONE;
		}
		
		if (startNode instanceof TypeDeclaration && endNode instanceof TypeDeclaration)
			if (startNode == endNode)
				if (startNode.getRoot() == startNode)
					return CloneType.CLASS_CLONE;
				else
					return CloneType.CLASS_IN_CLASS_CLONE;
		
		if (startNode instanceof TypeDeclaration && endNode instanceof Block)
			if (endOfLastDecl())
				return CloneType.CLASS_CLONE;
		
		if (startNode instanceof VariableDeclarationStatement && endNode instanceof AnonymousClassDeclaration)
			if (endNode.getParent().getParent().getParent() == startNode)
				return CloneType.ANONYMOUS_CLASS_IN_VARIABLE_DECLARATION;
		
		if (startNode instanceof Modifier && startNode.getParent() instanceof FieldDeclaration && endNode instanceof AnonymousClassDeclaration)
			if (endNode.getParent().getParent().getParent() == startNode.getParent())
				return CloneType.ANONYMOUS_CLASS_IN_FIELD_DECLARATION;
		
		if (startNode instanceof FieldDeclaration && endNode instanceof AnonymousClassDeclaration)
			if (endNode.getParent().getParent().getParent() == startNode)
				return CloneType.ANONYMOUS_CLASS_IN_FIELD_DECLARATION;
		
		return 0;
	}
	
	private boolean endOfLastDecl() {
		MethodDeclaration[] decl = null;
		
		if (startNode instanceof TypeDeclaration) {
			TypeDeclaration stmt = (TypeDeclaration)startNode;
			decl = stmt.getMethods();			
		} else if (startNode.getParent() instanceof TypeDeclaration) {
			TypeDeclaration stmt = (TypeDeclaration)startNode.getParent();
			decl = stmt.getMethods();
		}
		
		if (decl.length > 0) {
			MethodDeclaration last = (MethodDeclaration)decl[decl.length - 1];
		
			if (last.getStartPosition() + last.getLength() == endNode.getStartPosition() + endNode.getLength())
				return true;
		}
		
		return false;
	}
	
	private int isLoop() {
		boolean satisfy = false;
		
		if ((startNode instanceof WhileStatement || startNode instanceof ForStatement) && endNode instanceof Block)
			if (endNode.getParent() == startNode)
				satisfy = true;
			else if (endOfLastStmt(startNode))
				satisfy = true;
		
		if (satisfy)
			if (startNode instanceof WhileStatement)
				return CloneType.WHILE_CLONE;
			else if (startNode instanceof ForStatement)
				return CloneType.FOR_CLONE;
					
		return 0;
	}
	
	private boolean endOfLastStmt(ASTNode node) {
		Object test = null;
		Block block = null;
			
		if (node instanceof WhileStatement) {
			WhileStatement stmt = (WhileStatement)node;
			test = stmt.getBody();
		} else if (node instanceof ForStatement) {
			ForStatement stmt = (ForStatement)node;
			test = stmt.getBody();
		} else if (node instanceof IfStatement) {
			IfStatement stmt = (IfStatement)node;
			
			if (stmt.getElseStatement() == null) {
				test = stmt.getThenStatement();
			} else {
				test = stmt.getElseStatement();
			}
		}
		
		Statement last = null;
		
		if (test instanceof Block) {
			block = (Block)test;
			
			last = (Statement)block.statements().get(block.statements().size() - 1);
		} else if (test instanceof Statement) {
			last = (Statement)test;
		}
		
		if (last != null)
			if (last.getStartPosition() + last.getLength() == endNode.getStartPosition() + endNode.getLength())
				return true;
		
		if (last != null)
			return endOfLastStmt(last);
		else
			return false;
	}

	private int isCase() {
		if ((startNode instanceof SwitchCase && endNode instanceof BreakStatement) || (startNode instanceof SwitchCase && endNode instanceof Block)) {
			if (startNode.getParent() == endNode.getParent()) {
				List<SwitchCase> cases = getSwitchCases((SwitchStatement)startNode.getParent());
				
				boolean caseFound = false;
				
				for (Iterator<SwitchCase> i = cases.iterator(); i.hasNext(); ) {
					SwitchCase element = i.next();

					if (caseFound) {
						if ((endNode.getStartPosition() > startNode.getStartPosition()) && (endNode.getStartPosition() < element.getStartPosition()))
							return CloneType.CASE_CLONE;
						else
							return 0;
					}
					
					if (startNode == element)
						caseFound = true;
				}
			}
		}
		
		return 0;
	}
	
	private int isSwitch() {
		if (startNode instanceof SwitchStatement && endNode instanceof SwitchStatement)
			if (startNode == endNode)
				return CloneType.SWITCH_CLONE;
		
		return 0;
	}
	
	private int isBlock() {
		if (startNode instanceof Block && endNode instanceof Block)
			if (startNode == endNode)
				return CloneType.BLOCK_CLONE;
		
		//if (endNode instanceof Block)
		//	if (startNode.getParent() == endNode)
		//		return CloneType.BLOCK_CLONE;
		
		return 0;
	}
	
	private int isStatements() {
		ASTNode startParent = startNode.getParent();
		ASTNode endParent = endNode.getParent();
		
		if (startParent instanceof Block) {
			if (startParent == endParent)
				return CloneType.STATEMENTS_CLONE;
			
			if (startParent == endParent.getParent())
				return CloneType.STATEMENTS_CLONE;
			
			if (startParent == endParent.getParent().getParent())
				return CloneType.STATEMENTS_CLONE;
		}
		
		
		return 0;
	}

}
