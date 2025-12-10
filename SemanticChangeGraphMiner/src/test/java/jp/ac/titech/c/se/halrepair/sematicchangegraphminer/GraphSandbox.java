package jp.ac.titech.c.se.halrepair.sematicchangegraphminer;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.CASTNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.ChangeGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.graphics.DotGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;
import jp.ac.titech.c.se.halrepair.sematicchangegraphminer.utils.ReadGraph;
import jp.ac.titech.c.se.halrepair.sematicchangegraphminer.utils.TestUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphSandbox {

    @Test
    public void test() throws Exception {
        GROUMGraph g = ReadGraph.read(0);

        CASTNode before = g.getChangeGraph().getBeforeAST();
        CASTNode after = g.getChangeGraph().getAfterAST();


        System.out.println("=====================================");
        System.out.println("[before]");
        System.out.println(g.getRawText(true));
        System.out.println("=====================================");
        /*ystem.out.println("[before(normalize)]");
        System.out.println(g.getNormalizedText(true));
        System.out.println("=====================================");*/
        System.out.println("[after]");
        System.out.println(g.getRawText(false));
        System.out.println("=====================================");
        /*System.out.println("[after(normalize)]");
        System.out.println(g.getNormalizedText(false));
        System.out.println("=====================================");*/

        System.out.println(printMarkTree(before));
        System.out.println("=====================================");

        for(GROUMNode node : g.getNodes()){
            if(node.getVersion()==0){
                System.out.println(node.getLabel());
                for(CASTNode castNode : node.getCAstNodeList()){
                    System.out.println(castNode.toString());
                }
            }
        }
    }

    private static String printMarkTree(CASTNode node){
        return printMarkTreeInternal(node, 0);
    }

    private static String printMarkTreeInternal(CASTNode node, int depth){
        StringBuilder sb = new StringBuilder();

        String mark = node.getGroumNode() != null ? "*" : " ";
        sb.append(mark);

        sb.append(String.format("%s%d %s \"%s\" [%d, %d]\n", "  ".repeat(depth),
                node.getId(),
                ASTNode.nodeClassForType(node.getType()).getSimpleName(),
                node.getLabel(),
                node.getStartPosition(),
                node.getLength()));
        for(CASTNode child : node.getChildren()){
            sb.append(printMarkTreeInternal(child, depth + 1));
        }
        return sb.toString();
    }
}
