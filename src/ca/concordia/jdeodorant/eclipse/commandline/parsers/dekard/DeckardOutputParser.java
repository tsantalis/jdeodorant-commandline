package ca.concordia.jdeodorant.eclipse.commandline.parsers.dekard;

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

//package org.eclipse.contribution.cedar.actions.results;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.contribution.cedar.elements.clones.CeDARPlugin;
import org.eclipse.contribution.cedar.elements.clones.Clone;
import org.eclipse.contribution.cedar.elements.clones.CloneGroup;
import org.eclipse.contribution.cedar.elements.clones.CloneType;
import org.eclipse.contribution.cedar.elements.clones.IdentifyClone;
import org.eclipse.contribution.cedar.elements.clones.Visit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.concordia.jdeodorant.eclipse.commandline.parsers.CloneToolParser;
import ca.concordia.jdeodorant.eclipse.commandline.parsers.ExcelFileColumns;



public class DeckardOutputParser extends CloneToolParser {

	private final String deckardFile;

	public DeckardOutputParser(IJavaProject jProject, String deckardFilePath, File excelFile,
			boolean launchApplication, String binFolder) {
		super(jProject, excelFile, launchApplication, binFolder);
		this.deckardFile = replaceBackSlasheshWithSlashesh(deckardFilePath);
	}


	@Override
	protected void fillInCloneExcelFile() {

		IProject iProject = jProject.getProject();

		if (iProject == null)
			return;

		// This code is DIRTY.
		CeDARPlugin.project = iProject;

		Pattern pattern;
		Matcher matcher;

		String lookingFor = "(.*)\n";

		pattern = Pattern.compile(lookingFor);
		matcher = pattern.matcher(readResultsFile(this.deckardFile));

		int groupID = 0;

		boolean inGroup = false;
		int cloneCount = 1;
		CloneGroup cloneGroup = null;
		int row = 1;

		while (matcher.find()) {
			
			String strLine = matcher.group(1);

			lookingFor = "[0-9]+\\sdist:\\d+\\.\\d+\\sFILE\\s([[\\w\\s\\.-]+/]+[\\w\\s\\.-]+)\\sLINE:([0-9]+):([0-9]+)\\s.*";

			Pattern linePattern = Pattern.compile(lookingFor);
			Matcher lineMatcher = linePattern.matcher(strLine);

			CompilationUnit cunit = null;
			ICompilationUnit icunit = null;

			if (lineMatcher.find()) {

				ResourceInfo resourceInfo = getResourceInfo(jProject, lineMatcher.group(1));
				if (resourceInfo == null) {
					continue;
				}

				icunit = resourceInfo.getICompilationUnit();

				if (icunit != null) {

					if (!inGroup) {
						inGroup = true;
						groupID++;
						cloneCount = 1;

						cloneGroup = new CloneGroup(groupID);
						//addCloneGroup(cloneGroup);
					}

					int start = Integer.parseInt(lineMatcher.group(2));
					int length = Integer.parseInt(lineMatcher.group(3));

					Clone cloneInfo = new Clone(cloneCount, cloneGroup, new Path(icunit.getResource().getLocation().toPortableString()), start, start + length);	

					String packageName = "";

					File f = new File(icunit.getResource().getLocation().toPortableString());

					cloneGroup.addClone(cloneInfo);

					addLabel(row, ExcelFileColumns.CLONE_GROUP_ID, groupID); 
					//addLabel(serialNumber, 1, packageName); 
					String className = cloneInfo.getFileName().substring(0,cloneInfo.getFileName().lastIndexOf('.'));
					addLabel(row, ExcelFileColumns.CLASS_NAME, className); 
					addLabel(row, ExcelFileColumns.SOURCE_FOLDER, resourceInfo.getSourceFolder());

					if(f != null && f.exists()) {
						cunit = getCompilationUnitFromICompilationUnit(icunit);

						if(cunit.getPackage() != null) packageName = cunit.getPackage().getName().toString();

						addLabel(row, ExcelFileColumns.PACKAGE_NAME, packageName); 

						IdentifyClone astInfo = new Visit();
						int info = astInfo.run(cloneInfo, cunit);

						if (info >= 0) {
							int startOffset;
							int endOffset;
							if (info == CloneType.STATEMENTS_CLONE) {
								List<ASTNode> nodes = astInfo.getStatements();					
								startOffset = nodes.get(0).getStartPosition();
								endOffset = nodes.get(nodes.size() - 1).getStartPosition() + nodes.get(nodes.size() - 1).getLength();
							} else {
								startOffset = astInfo.getStartOffset();
								endOffset = astInfo.getEndOffset();
							}

							addLabel(row, ExcelFileColumns.START_LINE, cloneInfo.getStart()); 
							addLabel(row, ExcelFileColumns.END_LINE, cloneInfo.getEnd()); 
							addLabel(row, ExcelFileColumns.START_OFFSET, startOffset); 
							addLabel(row, ExcelFileColumns.END_OFFSET, endOffset); 
							
							writeCoverageInfo(packageName, className, row, cloneInfo.getStart(), cloneInfo.getEnd());

							createTextFileFromOffset(getIDocument(cunit.getJavaElement()), startOffset, endOffset - startOffset, groupID + "-" + cloneCount + ".txt");
						}
					}

					row++;
					cloneCount++;

				}
				
			} else {
				
					if (cloneGroup != null) {
					
					inGroup = false;
	
					writeCloneGroupInfo(row - cloneGroup.getCloneCount(), ExcelFileColumns.CLONE_GROUP_INFO, ExcelFileColumns.CONNTECTED, cloneGroup);
	
					ArrayList<Clone> clones = cloneGroup.getClones();					
	
					int info = 0;
					boolean clonesWithinTheMethod = true;
					boolean clonesInTheSameFile = true;
					IMethod previousIMehtod = null;
					String prevCloneFileName = "";
					for (int i = 0; i < clones.size(); i++) {
						
						Clone clone = clones.get(i);
						
						if(i > 0 && !prevCloneFileName.equals(clone.getFileLocation().toString()))
							clonesInTheSameFile = false;
						prevCloneFileName = clone.getFileLocation().toString();
	
						File f = new File(clone.getFileLocation().toString());
	
						if(f.exists()) {
							if (cunit == null) {
								icunit = (ICompilationUnit)JavaCore.create(clone.getIFile());
								cunit = getCompilationUnitFromICompilationUnit(icunit);
							}
	
							IdentifyClone astInfo = new Visit();
							info = astInfo.run(clone, cunit);
	
							if (info >= 0) {
								clone.setType(info);
								int startOffset;
								int endOffset;
								
								if (info == CloneType.STATEMENTS_CLONE) {
									List<ASTNode> nodes = astInfo.getStatements();
									startOffset = nodes.get(0).getStartPosition();
									endOffset = nodes.get(nodes.size() - 1).getStartPosition() + nodes.get(nodes.size() - 1).getLength();
								} else {
									ASTNode topNode = astInfo.getTopNode();
									startOffset = topNode.getStartPosition();
									endOffset = topNode.getStartPosition() + topNode.getLength();				
								}
	
								// Get method info (name and signature)
								IMethod iMethod = getIMethod(icunit, cunit, startOffset, endOffset - startOffset);
								if(iMethod != null) {
	
									if (i > 0 && !iMethod.equals(previousIMehtod))
										clonesWithinTheMethod = false;
									previousIMehtod = iMethod;
	
									addLabel(row - cloneGroup.getCloneCount() + i, ExcelFileColumns.METHOD_NAME, iMethod.getElementName()); 
									try {
										addLabel(row - cloneGroup.getCloneCount() + i, ExcelFileColumns.METHOD_SIGNATURE, iMethod.getSignature());
									} catch (JavaModelException e) {
										e.printStackTrace();
									}
								}
							} 
	
						}
	
						if (i < (clones.size() - 1))
							if (!clone.getFileLocation().toString().equals(clones.get(i + 1).getFileLocation().toString())) {
								cunit = null;
							}
	
						if (info > 0) {
							clone.setType(info);			
						}
					}
	
					// Number of clones in the current clone set
					addLabel(row - cloneGroup.getCloneCount(), ExcelFileColumns.CLONE_GROUP_SIZE, cloneGroup.getCloneCount());
	
					// Clone location
					String clonesLocationDescription = "Clones are ";
					if (clonesInTheSameFile) {
						if (clonesWithinTheMethod) {
							clonesLocationDescription += "within the same method"; 
						} else {
							clonesLocationDescription += "within the same file";
						}
					} else {
						clonesLocationDescription += "in different files";
					}
					addLabel(row - cloneGroup.getCloneCount(), ExcelFileColumns.CLONE_LOCATION, clonesLocationDescription);
	
	
				} else { // if clone group != null but we are not inside a group, there must be an error with the file
					throw new RuntimeException(String.format("The file %s seems to be corroupt.", this.deckardFile));
				}
			}
		}


	}
	
	@Override
	protected String getMainInputFile() {
		return this.deckardFile;
	}

}
