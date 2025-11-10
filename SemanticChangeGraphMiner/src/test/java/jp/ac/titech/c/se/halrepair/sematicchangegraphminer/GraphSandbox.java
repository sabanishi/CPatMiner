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
    @Test
    public void test() throws Exception {
        ArrayList<GROUMGraph> graphs = readGraphs("e2bf7bfc3977ff7f8a7699d3e17039c78366b465.dat");

        for(GROUMGraph g : graphs){
            //System.out.println(g.getName());
            CASTNode before = g.getChangeGraph().getBeforeAST();

            System.out.println("original tree:\n"+before.printTree());
            System.out.println("normalized tree:\n"+before.printNormalizedTree());
        }
    }


    private static ArrayList<GROUMGraph> readGraphs(String changesPath) {
        ArrayList<GROUMGraph> graphs = new ArrayList<>();

        ClassLoader classLoader = TestUtil.class.getClassLoader();
        URL resource = classLoader.getResource(changesPath);
        File sub = new File(resource.getPath());

        @SuppressWarnings("unchecked")
        HashMap<String, HashMap<String, ChangeGraph>> fileChangeGraphs = (HashMap<String, HashMap<String, ChangeGraph>>) FileIO.readObjectFromFile(sub.getAbsolutePath());

        for (String fp : fileChangeGraphs.keySet()) {
            System.out.println(fp);
            HashMap<String, ChangeGraph> cgs = fileChangeGraphs.get(fp);
            for (String method : cgs.keySet()) {
                System.out.println("method: \n"+method);
                int index = sub.getName().indexOf('.');
                if (index < 0) {
                    index = sub.getName().length();
                }
                String name = FileIO.getSimpleFileName(sub.getName().substring(0, index)) + "," + fp + "," + method;
                ChangeGraph cg = cgs.get(method);
                if (cg.getNodes().size() <= 2) continue;
                GROUMGraph g = new GROUMGraph(cg, name);
                // FIXME
                g.pruneDoubleEdges();
                g.setProject("dummy");
                graphs.add(g);
            }
        }

        return graphs;
    }
}
