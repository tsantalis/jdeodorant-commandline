package ca.concordia.jdeodorant.eclipse.commandline.cloneinfowriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ca.concordia.jdeodorant.eclipse.commandline.coverage.TestReportResults.TestReportDifference;

public class ClonePairInfo {

	public enum AnalysisStatus {
		/** Happens when 
		 * <ul>
		 * <li>At least one of the ASTs didn't have any nodes</li>
		 * <li>SystemObject.getMethodObject() cannot find either iMethod1 or iMethod2</li>,
		 * <li>The getMethodBody() of either methodObject1 or methodObject2 (resulting from calling SystemObject.getMethodObject()) is null</li>
		 * </ul>
		 */
		NOT_ANALYZED,
		/** The bottom-up subtree mapping didn't find any common nesting structure */
		NO_COMMON_SUBTREE_FOUND,
		/** Normal */
		NORMAL


	}

	private String projectName;
	private int numberOfStatementsToBeRefactored = 0;
	private int numberOfPDGNodesInFirstMethod = 0;
	private int numberOfPDGNodesInSecondMethod = 0;
	private int numberOfCloneStatementsInFirstCodeFragment = 0;
	private int numberOfCloneStatementsInSecondCodeFragment = 0;
	private int startOffsetOfFirstCodeFragment = 0;
	private int startOffsetOfSecondCodeFragment = 0;
	private int endOffsetOfFirstCodeFragment = 0;
	private int endOffsetOfSecondCodeFragment = 0;
	private String sourceCodeFirst = null;
	private String sourceCodeSecond = null;
	private IMethod firstIMethod;
	private IMethod secondIMethod;
	private int numberOfNodeComparisons = 0;
	private long subtreeMatchingTime = 0;
	private int cloneGroupID;
	private int cloneFragment1ID;
	private int cloneFragment2ID;
	private AnalysisStatus status;
	private List<PDGSubTreeMapperInfo> subtreeMappersList = new ArrayList<>();
	private long subtreeMatchingWallNanoTime;
	private ICompilationUnit iCompilationUnit1, iCompilationUnit2;
	private String firstSourceFolder, secondSourceFolder;
	private String firstClass, secondClass, firstPackage, secondPackage;
	private Set<String> testPackages = new HashSet<>();
	private Set<String> testSourceFolders = new HashSet<>();
	private List<String> filesHavingCompileErrors = new ArrayList<>();
	private List<TestReportDifference> compareTestResults;

	public int getCloneGroupID() {
		return cloneGroupID;
	}

	public void setCloneGroupID(int cloneGroupID) {
		this.cloneGroupID = cloneGroupID;
	}

	public int getCloneFragment1ID() {
		return cloneFragment1ID;
	}

	public void setCloneFragment1ID(int cloneFragment1ID) {
		this.cloneFragment1ID = cloneFragment1ID;
	}

	public int getCloneFragment2ID() {
		return cloneFragment2ID;
	}

	public void setCloneFragment2ID(int cloneFragment2ID) {
		this.cloneFragment2ID = cloneFragment2ID;
	}

	public String getSourceCodeFirst() {
		if (sourceCodeFirst == null) {
			try {
				sourceCodeFirst = "";
				if (startOffsetOfFirstCodeFragment < Integer.MAX_VALUE && endOffsetOfFirstCodeFragment > -1)
					sourceCodeFirst = getSourceCodeStringFromICompilationUnit(startOffsetOfFirstCodeFragment, endOffsetOfFirstCodeFragment, iCompilationUnit1);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return sourceCodeFirst;
	}

	public String getSourceCodeSecond() {
		if (sourceCodeSecond == null) {
			try {
				sourceCodeSecond = "";
				if (startOffsetOfSecondCodeFragment < Integer.MAX_VALUE && endOffsetOfSecondCodeFragment > -1)
					sourceCodeSecond = getSourceCodeStringFromICompilationUnit(startOffsetOfSecondCodeFragment, endOffsetOfSecondCodeFragment, iCompilationUnit2);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return sourceCodeSecond;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getFirstMethodSignature() {
		if (firstIMethod != null)
			return getMethodJavaSignature(firstIMethod);
		return "";
	}

	public void setContaingingIMethodFirst(IMethod iMethod) {
		this.firstIMethod = iMethod;
		this.iCompilationUnit1 = iMethod.getCompilationUnit();
	}

	public String getSecondMethodSignature() {
		if (secondIMethod != null)
			return getMethodJavaSignature(secondIMethod);
		return "";
	}

	public void setContainingIMethodSecond(IMethod iMethod) {
		this.secondIMethod = iMethod;
		this.iCompilationUnit2 = iMethod.getCompilationUnit();
	}

	public int getNumberOfStatementsToBeRefactored() {
		return numberOfStatementsToBeRefactored;
	}

	public void setNumberOfStatementsToBeRefactored(int numberOfStatementsToBeRefactored) {
		this.numberOfStatementsToBeRefactored = numberOfStatementsToBeRefactored;
	}

	public int getNumberOfPDGNodesInFirstMethod() {
		return numberOfPDGNodesInFirstMethod;
	}

	public void setNumberOfPDGNodesInFirstMethod(int numberOfPDGNodesInFirstCodeFragment) {
		this.numberOfPDGNodesInFirstMethod = numberOfPDGNodesInFirstCodeFragment;
	}

	public int getNumberOfPDGNodesInSecondMethod() {
		return numberOfPDGNodesInSecondMethod;
	}

	public void setNumberOfPDGNodesInSecondMethod(int numberOfPDGNodesInSecondCodeFragment) {
		this.numberOfPDGNodesInSecondMethod = numberOfPDGNodesInSecondCodeFragment;
	}

	public int getNumberOfCloneStatementsInFirstCodeFragment() {
		return numberOfCloneStatementsInFirstCodeFragment;
	}

	public void setNumberOfCloneStatementsInFirstCodeFragment(int numberOfCloneStatementsInFirstCodeFragment) {
		this.numberOfCloneStatementsInFirstCodeFragment = numberOfCloneStatementsInFirstCodeFragment;
	}

	public int getNumberOfCloneStatementsInSecondCodeFragment() {
		return numberOfCloneStatementsInSecondCodeFragment;
	}

	public void setNumberOfCloneStatementsInSecondCodeFragment(int numberOfCloneStatementsInSecondCodeFragment) {
		this.numberOfCloneStatementsInSecondCodeFragment = numberOfCloneStatementsInSecondCodeFragment;
	}

	public int getStartOffsetOfFirstCodeFragment() {
		return startOffsetOfFirstCodeFragment;
	}

	public void setStartOffsetOfFirstCodeFragment(int startOffsetOfFirstCodeFragment) {
		this.startOffsetOfFirstCodeFragment = startOffsetOfFirstCodeFragment;
		this.sourceCodeFirst = null;
	}

	public void setStartOffsetOfSecondCodeFragment(int startOffsetOfSecondCodeFragment) {
		this.startOffsetOfSecondCodeFragment = startOffsetOfSecondCodeFragment;
		this.sourceCodeSecond = null;
	}

	public int getEndOffsetOfFirstCodeFragment() {
		return endOffsetOfFirstCodeFragment;
	}

	public void setEndOffsetOfFirstCodeFragment(int endOffsetOfFirstCodeFragment) {
		this.endOffsetOfFirstCodeFragment = endOffsetOfFirstCodeFragment;
	}

	public int getEndOffsetOfSecondCodeFragment() {
		return endOffsetOfSecondCodeFragment;
	}

	public void setEndOffsetOfSecondCodeFragment(int endOffsetOfSecondCodeFragment) {
		this.endOffsetOfSecondCodeFragment = endOffsetOfSecondCodeFragment;
	}

	/** 
	 * If one of the mappers (solutions) in this clone pair
	 * is refactorable, the clone pair is refactorable
	 * @return
	 */
	public boolean getRefactorable() {
		for (PDGSubTreeMapperInfo info : subtreeMappersList)
			if (info.isRefactorable())
				return true;
		return false;
	}

	/**
	 * Get the list of only refactorable mappers
	 * @return
	 */
	public Collection<PDGSubTreeMapperInfo> getRefactorableMappersInfo() {
		List<PDGSubTreeMapperInfo> toReturn = new ArrayList<>();
		for (PDGSubTreeMapperInfo info : subtreeMappersList)
			if (info.isRefactorable())
				toReturn.add(info);
		return toReturn;
	}

	public String getContainingFileFirst() {
		return iCompilationUnit1.getPath().toPortableString();
	}

	public String getContainingFileSecond() {
		return iCompilationUnit2.getPath().toPortableString();
	}

	public List<PDGSubTreeMapperInfo> getPDFSubTreeMappersInfoList() {
		return subtreeMappersList;
	}

	public void addMapperInfo(PDGSubTreeMapperInfo info) {
		this.subtreeMappersList.add(info);
	}

	public int getNumberOfNodeComparisons() {
		return numberOfNodeComparisons;
	}

	public void setNumberOfNodeComparisons(int nodeComparisons) {
		this.numberOfNodeComparisons = nodeComparisons;
	}

	public void clearMappersInfo() {
		this.subtreeMappersList = new ArrayList<>();
	}

	private String getMethodJavaSignature(IMethod iMethod) {

		StringBuilder toReturn = new StringBuilder();

		//toReturn.append(iMethod.getDeclaringType().getFullyQualifiedName());
		try {
			toReturn.append(Signature.toString(iMethod.getReturnType()));
		} catch (IllegalArgumentException  | JavaModelException e) {

		}
		toReturn.append(" ");
		toReturn.append(iMethod.getElementName());
		toReturn.append("(");

		String comma = "";
		for (String type : iMethod .getParameterTypes()) {
			toReturn.append(comma);
			comma = ", ";
			toReturn.append(Signature.toString(type));
		}
		toReturn.append(")");

		return toReturn.toString();
	}

	private String getSourceCodeStringFromICompilationUnit(int startOffset, int endOffset, ICompilationUnit iCompilationUnit) throws BadLocationException {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath iPath = iCompilationUnit.getPath();

		try {
			bufferManager.connect(iPath, LocationKind.IFILE, null);
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(iPath, LocationKind.IFILE);
			IDocument iDocument = textFileBuffer.getDocument();
			return iDocument.get(startOffset, endOffset - startOffset);

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getSubtreeMatchingTime() {
		return subtreeMatchingTime;
	}

	public void setSubtreeMatchingTime(
			long timeElapsedForBottomUpMatching) {
		this.subtreeMatchingTime = timeElapsedForBottomUpMatching;
	}

	public AnalysisStatus getStatus() {
		return status;
	}

	public void setStatus(AnalysisStatus status) {
		this.status = status;
	}

	public String getClonePairID() {
		return /*cloneGroupID + "-" + */cloneFragment1ID + "-" + cloneFragment2ID;
	}

	public long getSubtreeMatchingWallNanoTime() {
		return subtreeMatchingWallNanoTime;
	}

	public void setSubtreeMatchingWallNanoTime(long nanoTime) {
		this.subtreeMatchingWallNanoTime = nanoTime;
	}

	public ICompilationUnit getICompilationUnitFirst() {
		return iCompilationUnit1;
	}

	public ICompilationUnit getICompilationUnitSecond() {
		return iCompilationUnit2;
	}

	public void setICompilationUnitFirst(ICompilationUnit iCompilationUnit) {
		this.iCompilationUnit1 = iCompilationUnit;
	}

	public void setICompilationUnitSecond(ICompilationUnit iCompilationUnit) {
		this.iCompilationUnit2 = iCompilationUnit;
	}

	public void setFirstSourceFolder(String firstSrcFolder) {
		this.firstSourceFolder = firstSrcFolder;
	}

	public String getFirstSourceFolder() {
		return this.firstSourceFolder;
	}

	public void setSecondSrcFolder(String secondSrcFolder) {
		this.secondSourceFolder = secondSrcFolder;
	}

	public String getSecondSourceFolder() {
		return this.secondSourceFolder;
	}

	public String getFirstPackage() {
		return this.firstPackage;
	}

	public String getFirstClass() {
		return this.firstClass;
	}

	public String getSecondPackage() {
		return this.secondPackage;
	}

	public String getSecondClass() {
		return this.secondClass;
	}

	public void setFirstClass(String firstClass) {
		this.firstClass = firstClass;
	}

	public void setSecondClass(String secondClass) {
		this.secondClass = secondClass;
	}

	public void setFirstPackage(String firstPackage) {
		this.firstPackage = firstPackage;
	}

	public void setSecondPackage(String secondPackage) {
		this.secondPackage = secondPackage;
	}
	
	public boolean haveCommonSuperClass() {
		IType commonSuperType = commonSuperType(firstIMethod.getDeclaringType(), secondIMethod.getDeclaringType());
		if(commonSuperType != null && !commonSuperType.getFullyQualifiedName().equals("java.lang.Object"))
			return true;
		return false;
	}

	public static IType commonSuperType(IType type1, IType type2) {

		Set<IType> superTypes1 = getAllSuperTypes(type1);
		Set<IType> superTypes2 = getAllSuperTypes(type2);
		for(IType superType2 : superTypes2) {
			if(superType2.getFullyQualifiedName().equals(type1.getFullyQualifiedName()))
				return type1;
		}
		for(IType superType1 : superTypes1) {
			if(superType1.getFullyQualifiedName().equals(type2.getFullyQualifiedName()))
				return type2;
		}
		boolean found = false;
		IType commonSuperType = null;
		for(IType superType1 : superTypes1) {
			for(IType superType2 : superTypes2) {
				if(superType1.getFullyQualifiedName().equals(superType2.getFullyQualifiedName())) {
					commonSuperType = superType1;
					found = true;
					break;
				}
			}
			if(found)
				break;
		}
		return commonSuperType;
	}

	private static Set<IType> getAllSuperTypes(IType iType) {
		Set<IType> superTypes = new LinkedHashSet<IType>();
		try {
			ITypeHierarchy hierarchy = iType.newSupertypeHierarchy(null);
			IType[] superClasses = hierarchy.getAllSuperclasses(iType);
			for (IType t : superClasses)
				superTypes.add(t);
		} catch (JavaModelException ex) {
			ex.printStackTrace();
		}
		return superTypes;
	}
	
	public ClonePairLocation getLocation() {
		if(firstIMethod != null && secondIMethod != null) {
			if (firstIMethod.equals(secondIMethod)) {
				return ClonePairLocation.SAME_METHOD;
			} else if (firstIMethod.getDeclaringType().getFullyQualifiedName().equals(secondIMethod.getDeclaringType().getFullyQualifiedName())) {
				return ClonePairLocation.SAME_DECLARING_CLASS;
			} else if ((firstPackage + "." + firstClass).equals(secondPackage + "." + secondClass)) {
				return ClonePairLocation.SAME_JAVA_FILE;
			} else if (haveCommonSuperClass()) {
				return ClonePairLocation.SAME_HIERARCHY;
			} else {
				return ClonePairLocation.DIFFERENT_CLASSES;
			}
		}
		return null;
	}

	public TestCodeInfo pairTestCodeInfo() {
		
		boolean firstCodeIsTestCode = testPackages.contains(getFirstPackage()) || testSourceFolders.contains(getFirstSourceFolder());
		boolean secondCodeIsTestCode = testPackages.contains(getSecondPackage()) || testSourceFolders.contains(getSecondSourceFolder());
		
		if (firstCodeIsTestCode && secondCodeIsTestCode)
			return TestCodeInfo.BOTH_ARE_TEST_CODE;
		else if (firstCodeIsTestCode)
			return TestCodeInfo.FIRST_IS_TEST_CODE;
		else if (secondCodeIsTestCode)
			return TestCodeInfo.SECOND_IS_TEST_CODE;
		else 
			return TestCodeInfo.NONE_IS_TEST_CODE;
		
	}

	public void setTestPackages(String[] testPackages) {
		this.testPackages.clear();
		if (testPackages != null)
			for (String packageName : testPackages)
				this.testPackages.add(packageName);
	}
	
	public Set<String> getTestPackages() {
		return this.testPackages;
	}
	
	public void setTestSourceFolders(String[] testSrcFolders) {
		this.testSourceFolders.clear();
		if (testSrcFolders != null)
			for (String testSrcFolder : testSrcFolders)
				this.testSourceFolders.add(testSrcFolder);
	}
	
	public Set<String> getTestSourceFolders() {
		return this.testSourceFolders;
	}

	public void addFileHavingCompileError(String path) {
		this.filesHavingCompileErrors.add(path);
	}
	
	public Iterable<String> getFilesHavingCompileError() {
		return this.filesHavingCompileErrors;		
	}

	public void setTestDifferences(List<TestReportDifference> compareTestResults) {
		this.compareTestResults = compareTestResults;
	}
	public List<TestReportDifference> getTestDifferences() {
		return this.compareTestResults;
	}
}
