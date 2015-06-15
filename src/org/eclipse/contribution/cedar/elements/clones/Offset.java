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

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class Offset {

	private CompilationUnit unit;
	private IFile file;
	private int start;
	private int end;
	private int origEnd;
	private int startOffset;
	private int endOffset;
	private char character;
	private String sourceCode;

	public Offset(CompilationUnit _cunit, int _start, int _end) {
		unit = _cunit;
		start = _start;
		end = _end;
		origEnd = _end;
		
		startOffset = 0;
		endOffset = 0;
		character = ' ';
	}
	
	public Offset(IFile _file, int _start, int _end) {
		file = _file;
		start = _start;
		end = _end;
		origEnd = _end;
		
		startOffset = 0;
		endOffset = 0;
		character = ' ';
	}
	
	public void computeOffsets() {
		if (unit != null)
			useCUnit();
		else if (file != null)
			useFile();
	}
	
	public void computeOffsets(int otherEnd) {
		this.end = otherEnd;
		computeOffsets();
	}
	
	public int getPreEndOffset() {
		end = origEnd - 1;
		computeOffsets();
		return endOffset;
	}
	
	public int getPostEndOffset() {
		end = origEnd + 1;
		computeOffsets();
		return endOffset;
	}
	
	private void useCUnit() {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath path = unit.getJavaElement().getPath();
	
		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			IDocument document = textFileBuffer.getDocument();
	
			startOffset = document.getLineOffset(start - 1);
			endOffset = document.getLineOffset(end - 1);
	
			character = document.getChar(startOffset);
	
			while ((character == ' ') || (character == '\t')) {
				startOffset++;
				character = document.getChar(startOffset);
			}
			
			character = document.getChar(endOffset);
			
			while ((character == ' ') || (character == '\t')) {
				endOffset++;
				character = document.getChar(endOffset);
			}
			
			//for (int i = 1764 - 10; i < 1764 + 10; i++)
			//	System.out.println(i + ": " + document.getChar(i));
	
			//IRegion region = document.getLineInformationOfOffset(endOffset);
	
			//endOffset = endOffset + region.getLength() - 1;
	
			textFileBuffer.commit(null, false);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			if (endOffset == 0)
				endOffset = unit.getStartPosition() + unit.getLength();
		} finally {
			try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
			}
		}
	}
	
	private void useFile() {
		IDocumentProvider provider = new TextFileDocumentProvider();
		
		try {
			provider.connect(file);
			IDocument document = provider.getDocument(file);
			if ( document != null )
			{
				startOffset = document.getLineOffset(start - 1);
				
				if (document.getNumberOfLines() == (end - 1)) {
					endOffset = document.getLineOffset(end - 2);
				} else {
					endOffset = document.getLineOffset(end - 1);					
				}
		
				character = document.getChar(startOffset);
				
				while ((character == ' ') || (character == '\t')) {
					startOffset++;
					character = document.getChar(startOffset);
				}
				
				if (endOffset != document.getLength()) {
					character = document.getChar(endOffset);
					
					while ((character == ' ') || (character == '\t')) {
						endOffset++;
						character = document.getChar(endOffset);
					}					
				}
				
				sourceCode = document.get(startOffset, (endOffset - startOffset));
			}
			document = null;
			provider = null;
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} 
		
		/*
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath path = file.getFullPath();
	
		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			IDocument document = textFileBuffer.getDocument();
	
			startOffset = document.getLineOffset(start - 1);
			endOffset = document.getLineOffset(end - 1);
	
			character = document.getChar(startOffset);
	
			while ((character == ' ') || (character == '\t')) {
				startOffset++;
				character = document.getChar(startOffset);
			}
			
			character = document.getChar(endOffset);
			
			while ((character == ' ') || (character == '\t')) {
				endOffset++;
				character = document.getChar(endOffset);
			}
			
			textFileBuffer.commit(null, false);
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			if (endOffset == 0)
				endOffset = unit.getStartPosition() + unit.getLength();
		} finally {
			try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		*/
	}
	
	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}
	
	public String getSourceCode() {
		return sourceCode;
	}
	
}