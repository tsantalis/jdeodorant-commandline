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

public class CloneType {
	
	public static final int METHOD_CLONE = 1;
	public static final int STATEMENTS_CLONE = 2;
	public static final int IF_CLONE = 3;
	public static final int CLASS_CLONE = 4;
	public static final int CLASS_IN_CLASS_CLONE = 5;
	public static final int ANONYMOUS_CLASS_IN_FIELD_DECLARATION = 6;
	public static final int ANONYMOUS_CLASS_IN_VARIABLE_DECLARATION = 7;
	public static final int TRY_CLONE = 8;
	public static final int CATCH_CLONE = 9;
	public static final int WHILE_CLONE = 10;
	public static final int FOR_CLONE = 11;
	public static final int CASE_CLONE = 12;
	public static final int SWITCH_CLONE = 13;
	public static final int BLOCK_CLONE = 14;
	
	public static final int TOTAL = 14;
	
	public static int convert(int n) {
		switch (n) {
		
		// BLOCK
		case 8: return BLOCK_CLONE;
		
		// CATCH_CLAUSE
		case 12: return CATCH_CLONE;
		
		// FOR_STATEMENT
		case 24: return FOR_CLONE;
		
		// IF_STATEMENT
		case 25: return IF_CLONE;
		
		// METHOD_DECLARATION
		case 31: return METHOD_CLONE;
		
		// SWITCH_CASE
		case 49: return CASE_CLONE;
		
		// SWITCH_STATEMENT
		case 50: return SWITCH_CLONE;
		
		// TRY_STATEMENT
		case 54: return TRY_CLONE;
		
		// TYPE_DECLARATION
		case 55: return CLASS_CLONE;
		
		// WHILE_STATEMENT
		case 61: return WHILE_CLONE;
 
		default: return 0; 
		}
	}

	public static String getString(int n) {
		switch (n) {
		case METHOD_CLONE:  return "Method";
		case STATEMENTS_CLONE:  return "Statements";
		case IF_CLONE:  return "If-statement"; 
		case CLASS_CLONE:  return "Class";
		case CLASS_IN_CLASS_CLONE: return "Class (in class)";
		case ANONYMOUS_CLASS_IN_FIELD_DECLARATION: return "Anonymous class (Field Declaration)";
		case ANONYMOUS_CLASS_IN_VARIABLE_DECLARATION: return "Anonymous class (Variable Declaration)";
		case TRY_CLONE:  return "Try-statement";
		case CATCH_CLONE:  return "Catch-statement";
		case WHILE_CLONE:  return "While-loop";
		case FOR_CLONE: return "For-loop";
		case CASE_CLONE: return "Case-statement";
		case SWITCH_CLONE: return "Switch-statement";
		case BLOCK_CLONE: return "Block";
		default: return "Not Accounted For"; 
		}
	}

}
