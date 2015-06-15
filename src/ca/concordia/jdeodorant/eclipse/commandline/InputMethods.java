package ca.concordia.jdeodorant.eclipse.commandline;

import gr.uom.java.ast.decomposition.cfg.PDG;

import org.eclipse.jdt.core.IMethod;

public class InputMethods {
	private final IMethod iMethod1;
	private final IMethod iMethod2;	
	private final int startOffset1;
	private final int endOffset1;
	private final int startOffset2;
	private final int endOffset2;
	private final PDG firstPDG;
	private final PDG secondPDG;
	
	
	InputMethods(
			IMethod iMethod1, IMethod iMethod2,
			int startOffset1, int endOffset1, 
			int startOffset2, int endOffset2,
			PDG pdg1,
			PDG pdg2) {
		this.iMethod1 = iMethod1;
		this.iMethod2 = iMethod2;
		this.startOffset1 = startOffset1;
		this.endOffset1 = endOffset1;
		this.startOffset2 = startOffset2;
		this.endOffset2 = endOffset2;
		this.firstPDG = pdg1;
		this.secondPDG = pdg2;
	}

	public IMethod getIMethod1() {
		return iMethod1;
	}


	public IMethod getIMethod2() {
		return iMethod2;
	}


	public int getStartOffset1() {
		return startOffset1;
	}


	public int getEndOffset1() {
		return endOffset1;
	}


	public int getStartOffset2() {
		return startOffset2;
	}


	public int getEndOffset2() {
		return endOffset2;
	}


	public PDG getFirstPDG() {
		return firstPDG;
	}


	public PDG getSecondPDG() {
		return secondPDG;
	}	
}
