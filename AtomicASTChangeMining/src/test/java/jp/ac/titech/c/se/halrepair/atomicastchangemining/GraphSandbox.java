package jp.ac.titech.c.se.halrepair.atomicastchangemining;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.treed.TreedMapper;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.JavaASTUtil;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.TestUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;

public class GraphSandbox {
    @Test
    public void test() throws Exception{
        /*String targetSource = TestUtil.read("Target.java");

        ASTNode node = JavaASTUtil.parseSource(targetSource);
        System.out.println(node.getLength());*/

        String beforeSource = TestUtil.read("Before.java");
        String afterSource = TestUtil.read("After.java");

        ASTNode beforeNode = JavaASTUtil.parseSource(beforeSource);
        ASTNode afterNode = JavaASTUtil.parseSource(afterSource);

        System.out.println("Before length: " + beforeNode.getLength());
        System.out.println("After length: " + afterNode.getLength());


    }
}
