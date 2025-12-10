package jp.ac.titech.c.se.halrepair.sematicchangegraphminer.utils;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.ChangeGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReadGraph {
    private ReadGraph(){}

    private static String OutputPath = "/Users/sakugawa99/WebGL/CPatMiner/Sandbox/";
    private static String OutputFileName = "sandbox";

    public static ArrayList<GROUMGraph> readAll(){
        ArrayList<GROUMGraph> result = new ArrayList<>();
        for(int i=0;;i++){
            String objectName = OutputPath + OutputFileName + "_" + i;
            Path p = Path.of(objectName + ".dat");
            if(!Files.exists(p)){
                break;
            }
            ChangeGraph cg = (ChangeGraph) FileIO.readObjectFromFile(objectName + ".dat");
            String name = OutputFileName + "_" + i;
            GROUMGraph g = new GROUMGraph(cg, name);
            g.pruneDoubleEdges();
            g.setProject("dummy");
            result.add(g);
        }
        return result;
    }

    public static GROUMGraph read(int number) {
        String objectName = OutputPath + OutputFileName + "_" + number;
        ChangeGraph cg = (ChangeGraph) FileIO.readObjectFromFile(objectName + ".dat");
        String name = OutputFileName + "_" + number;
        GROUMGraph g = new GROUMGraph(cg, name);
        g.pruneDoubleEdges();
        g.setProject("dummy");
        return g;
    }
}
