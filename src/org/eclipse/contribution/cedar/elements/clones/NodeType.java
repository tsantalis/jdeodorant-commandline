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

public class NodeType {

	public static String getString(int n) {
		switch (n) {
		case 1:  return "ANONYMOUS_CLASS_DECLARATION";
		case 2:  return "ARRAY_ACCESS";
		case 3:  return "ARRAY_CREATION"; 
		case 4:  return "ARRAY_INITIALIZER"; 
		case 5:  return "ARRAY_TYPE"; 
		case 6:  return "ASSERT_STATEMENT"; 
		case 7:  return "ASSIGNMENT"; 
		case 8:  return "BLOCK"; 
		case 9:  return "BOOLEAN_LITERAL"; 
		case 10:  return "BREAK_STATEMENT"; 
		case 11:  return "CAST_EXPRESSION"; 
		case 12:  return "CATCH_CLAUSE"; 
		case 13:  return "CHARACTER_LITERAL"; 
		case 14:  return "CLASS_INSTANCE_CREATION"; 
		case 15:  return "COMPILATION_UNIT"; 
		case 16:  return "CONDITIONAL_EXPRESSION"; 
		case 17:  return "CONSTRUCTOR_INVOCATION"; 
		case 18:  return "CONTINUE_STATEMENT"; 
		case 19:  return "DO_STATEMENT"; 
		case 20:  return "EMPTY_STATEMENT"; 
		case 21:  return "EXPRESSION_STATEMENT"; 
		case 22:  return "FIELD_ACCESS"; 
		case 23:  return "FIELD_DECLARATION"; 
		case 24:  return "FOR_STATEMENT"; 
		case 25:  return "IF_STATEMENT"; 
		case 26:  return "IMPORT_DECLARATION"; 
		case 27:  return "INFIX_EXPRESSION"; 
		case 28:  return "INITIALIZER"; 
		case 29:  return "JAVADOC"; 
		case 30:  return "LABELED_STATEMENT"; 
		case 31:  return "METHOD_DECLARATION"; 
		case 32:  return "METHOD_INVOCATION"; 
		case 33:  return "NULL_LITERAL"; 
		case 34:  return "NUMBER_LITERAL"; 
		case 35:  return "PACKAGE_DECLARATION"; 
		case 36:  return "PARENTHESIZED_EXPRESSION"; 
		case 37:  return "POSTFIX_EXPRESSION"; 
		case 38:  return "PREFIX_EXPRESSION"; 
		case 39:  return "PRIMITIVE_TYPE"; 
		case 40:  return "QUALIFIED_NAME"; 
		case 41:  return "RETURN_STATEMENT"; 
		case 42:  return "SIMPLE_NAME"; 
		case 43:  return "SIMPLE_TYPE"; 
		case 44:  return "SINGLE_VARIABLE_DECLARATION"; 
		case 45:  return "STRING_LITERAL"; 
		case 46:  return "SUPER_CONSTRUCTOR_INVOCATION"; 
		case 47:  return "SUPER_FIELD_ACCESS"; 
		case 48:  return "SUPER_METHOD_INVOCATION"; 
		case 49:  return "SWITCH_CASE"; 
		case 50:  return "SWITCH_STATEMENT"; 
		case 51:  return "SYNCHRONIZED_STATEMENT"; 
		case 52:  return "THIS_EXPRESSION"; 
		case 53:  return "THROW_STATEMENT"; 
		case 54:  return "TRY_STATEMENT"; 
		case 55:  return "TYPE_DECLARATION"; 
		case 56:  return "TYPE_DECLARATION_STATEMENT"; 
		case 57:  return "TYPE_LITERAL"; 
		case 58:  return "VARIABLE_DECLARATION_EXPRESSION"; 
		case 59:  return "VARIABLE_DECLARATION_FRAGMENT"; 
		case 60:  return "VARIABLE_DECLARATION_STATEMENT"; 
		case 61:  return "WHILE_STATEMENT"; 
		case 62:  return "INSTANCEOF_EXPRESSION"; 
		case 63:  return "LINE_COMMENT"; 
		case 64:  return "BLOCK_COMMENT"; 
		case 65:  return "TAG_ELEMENT"; 
		case 66:  return "TEXT_ELEMENT"; 
		case 67:  return "MEMBER_REF"; 
		case 68:  return "METHOD_REF"; 
		case 69:  return "METHOD_REF_PARAMETER"; 
		case 70:  return "ENHANCED_FOR_STATEMENT"; 
		case 71:  return "ENUM_DECLARATION"; 
		case 72:  return "ENUM_CONSTANT_DECLARATION"; 
		case 73:  return "TYPE_PARAMETER"; 
		case 74:  return "PARAMETERIZED_TYPE"; 
		case 75:  return "QUALIFIED_TYPE"; 
		case 76:  return "WILDCARD_TYPE"; 
		case 77:  return "NORMAL_ANNOTATION"; 
		case 78:  return "MARKER_ANNOTATION"; 
		case 79:  return "SINGLE_MEMBER_ANNOTATION"; 
		case 80:  return "MEMBER_VALUE_PAIR"; 
		case 81:  return "ANNOTATION_TYPE_DECLARATION"; 
		case 82:  return "ANNOTATION_TYPE_MEMBER_DECLARATION"; 
		case 83:  return "MODIFIER"; 
		default: return "INVALID"; 
		}
	}

}
