package jp.ac.titech.c.se.halrepair.sematicchangegraphminer;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.CASTNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.ChangeGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.graphics.DotGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMNode;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;
import jp.ac.titech.c.se.halrepair.sematicchangegraphminer.utils.TestUtil;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphSandbox {
    private static String OutputPath = "/Users/sakugawa99/WebGL/CPatMiner/Sandbox/";
    private static String OutputFileName = "sandbox";

    @Test
    public void test() throws Exception {
        GROUMGraph g = readGraphs(0);

        //System.out.println(g.getName());
        CASTNode before = g.getChangeGraph().getBeforeAST();
        CASTNode after = g.getChangeGraph().getAfterAST();

        System.out.println(g.getRawText(false));
        System.out.println("=====================================");
        System.out.println(after.printTree());
        System.out.println("=====================================");
        System.out.println(g.getNormalizedText(false));

        //System.out.println("original tree:\n"+before.printTree());
        //System.out.println("normalized tree:\n"+after.printTree());

        for(GROUMNode node : g.getNodes()){
            if(node.getVersion()==1){
                System.out.println(node.getLabel());
                for(CASTNode castNode : node.getCAstNodeList()){
                    System.out.println(castNode.toString());
                }
            }
        }
    }


    private static GROUMGraph readGraphs(int number) {
        String objectName = OutputPath + OutputFileName + "_" + number;
        ChangeGraph cg = (ChangeGraph)FileIO.readObjectFromFile(objectName + ".dat");
        String name = OutputFileName + "_" + number;
        GROUMGraph g = new GROUMGraph(cg, name);
        g.pruneDoubleEdges();
        g.setProject("dummy");
        return g;
    }
}
