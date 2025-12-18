package jp.ac.titech.c.se.halrepair.sematicchangegraphminer.mining;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.groum.GROUMGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.mining.Miner;
import jp.ac.titech.c.se.halrepair.sematicchangegraphminer.utils.ReadGraph;
import org.junit.Test;

import java.util.ArrayList;

public class MinerTest {
    @Test
    public void test(){
        ArrayList<GROUMGraph> graphs = ReadGraph.readAll();

        Miner miner = new Miner(1);
        miner.setCurrDir("hoge");
        miner.mine(graphs,"/Users/sakugawa99/WebGL/CPatMiner/repos","/Users/sakugawa99/WebGL/CPatMiner/tmpOutput");
    }
}
