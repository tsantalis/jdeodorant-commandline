package ca.concordia.jdeodorant.eclipse.commandline.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.junit.Test;

import junit.framework.TestCase;

public class MatchingSubtreesTest extends TestCase {

	String optimalMatch, obtainedMatch;
	boolean obtainedMatchIsOptimal;
	public MatchingSubtreesTest(String fileNameId, String obtainedMatch) throws IOException {
			
		byte[] encoded = Files.readAllBytes(Paths.get("C:/Users/g_pana/workspace/ca.concordia.jdeodorant.eclipse.commandline/common_subtrees_cdt/"+fileNameId+".txt"));
		//byte[] encoded = Files.readAllBytes(Paths.get("C:/Users/g_pana/Downloads/common_subtrees_cdt/common_subtrees_cdt/"+fileNameId+".txt"));
		String content = Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
		Scanner scanContent = new Scanner(content);
		Scanner scanObtainedMatch = new Scanner(obtainedMatch);
		obtainedMatchIsOptimal = true;
		
		while(scanContent.hasNextLine() && scanObtainedMatch.hasNextLine()) {
			if(!scanContent.nextLine().equals(scanObtainedMatch.nextLine())) {
				obtainedMatchIsOptimal = false;
				break;
			}
		}
	}
	
	public void test() {		
		if(obtainedMatchIsOptimal == true)
			System.out.println("Subtrees Matched!"); 
		else
			System.out.println("Subtrees Not Matched!!");
				
	}
	
	public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("ca.concordia.jdeodorant.eclipse.commandline.test.MatchingSubtreesTest");
   }
}

