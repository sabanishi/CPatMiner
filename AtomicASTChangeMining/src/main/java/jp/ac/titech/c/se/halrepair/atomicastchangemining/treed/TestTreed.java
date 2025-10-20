package jp.ac.titech.c.se.halrepair.atomicastchangemining.treed;

import org.eclipse.jdt.core.dom.ASTNode;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.JavaASTUtil;

public class TestTreed {

	public static void main(String[] args) {
		ASTNode astM = JavaASTUtil.parseSource(FileIO.readStringFromFile("input/TestM.java"), "TestM.java");
		ASTNode astN = JavaASTUtil.parseSource(FileIO.readStringFromFile("input/TestN.java"), "TestN.java");
		TreedMapper tm = new TreedMapper(astM, astN);
		tm.map(false);
		tm.printChanges();
		System.out.println("Number of changed AST nodes: " + tm.getNumOfChanges());
	}

}
