package org.eclipse.contribution.cedar.elements.clones;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Util {
	
//	public static CompilationUnit getCompilationUnit(ICompilationUnit compilationUnit){
//		ASTParser parser = ASTParser.newParser(AST.JLS3);
//		parser.setKind(ASTParser.K_COMPILATION_UNIT);
//		parser.setSource(compilationUnit);
//		parser.setResolveBindings(true);
//		return (CompilationUnit)parser.createAST(null);
//	}
//	
//	public static CompilationUnit getCompilationUnit(Clone clone) {
//		String projectFile = clone.getFileLocation().toString().replace(CeDARPlugin.project.getLocation().toString() + "/", "");
//	
//		IFile file = CeDARPlugin.project.getFile(projectFile);
//	
//		ICompilationUnit compilationUnit = (ICompilationUnit)JavaCore.create(file);
//		return getCompilationUnit(compilationUnit);
//	}
//	
//	public static <K, V> HashMap<K, V> newHashMapInstance() {
//		return new HashMap<K, V>();
//	}
	
}
