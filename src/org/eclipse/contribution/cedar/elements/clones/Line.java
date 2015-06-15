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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class Line {

	private IFile file;
	private IDocument document;

	public Line(IFile _file) {
		file = _file;
		
		connect();
	}
	
	private void connect() {
		IDocumentProvider provider = new TextFileDocumentProvider();
		
		try {
			provider.connect(file);
			document = provider.getDocument(file);
		} catch (CoreException e) {
		} 
	}
	
	public int getLine(int offset) throws BadLocationException {
		if (document != null)
			return document.getLineOfOffset(offset) + 1;

		return 0;
	}
	
	public int getNumberOfLines() throws BadLocationException {
		return document.getNumberOfLines();
	}

}