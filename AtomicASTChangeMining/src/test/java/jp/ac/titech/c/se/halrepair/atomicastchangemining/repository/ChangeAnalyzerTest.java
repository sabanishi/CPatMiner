package jp.ac.titech.c.se.halrepair.atomicastchangemining.repository;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.change.ChangeAnalyzer;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.TestUtil;
import org.junit.Test;

import java.io.File;
import java.net.URL;


public class ChangeAnalyzerTest {
    @Test
    public void test() throws Exception {
        ClassLoader classLoader = TestUtil.class.getClassLoader();
        URL resource = classLoader.getResource("repos/chaa");

        analyze(new File(resource.getPath()), "chaa");
    }

    private static void analyze(final File dir, final String name) {
        if (!dir.isDirectory()) return;
        File git = new File(dir, ".git");
        System.out.println(git.getAbsolutePath());
        if (git.exists()) {
            System.out.println("Analyzing " + name);
            long startProjectTime = System.currentTimeMillis();
            System.out.println(name);
            String url = dir.getAbsolutePath();
            ChangeAnalyzer ca = new ChangeAnalyzer(name, -1, url);
            ca.buildGitConnector();
            try{
                ca.analyzeGit();
                long endProjectTime = System.currentTimeMillis();
                ca.getCproject().setRunningTime(endProjectTime - startProjectTime);
                System.out.println("Done " + name + " in " + (endProjectTime - startProjectTime) / 1000 + "s");
            }catch(Exception e){
                ca.closeGitConnector();
            }
        }
    }
}
