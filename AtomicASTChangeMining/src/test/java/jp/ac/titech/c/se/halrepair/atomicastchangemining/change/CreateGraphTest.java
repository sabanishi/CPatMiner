package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.graphics.DotGraph;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.repository.GitConnector;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.TestUtil;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class CreateGraphTest {
    private static String OutputPath = "/Users/sakugawa99/WebGL/CPatMiner/Sandbox/";
    private static String OutputFileName = "sandbox";

    @Test
    public void test() throws Exception{
        String beforeSource = TestUtil.read("Before.java");
        String afterSource = TestUtil.read("After.java");

        create(beforeSource, afterSource);
    }

    private void create(String beforeSource, String afterSource) throws Exception{
        Repository repo = new InMemoryRepository(new DfsRepositoryDescription("dummy-repo"));
        RevCommit commit = createCommitFromDiff(repo, beforeSource, afterSource);

        ChangeAnalyzer ca = new ChangeAnalyzer("dummy", -1, "dummy");
        ca.setDummyGitConnector(new GitConnector(repo));
        RevisionAnalyzer ra = new RevisionAnalyzer(ca, commit);
        ra.analyzeGit();

        int i = 0;
        for (CMethod e : ra.getMappedMethodsM()) {
            ChangeGraph cg = e.getChangeGraph(repo, commit);

            // AST出力
            System.out.println( cg.getBeforeAST().printTree());
            System.out.println(cg.getAfterAST().printTree());

            DotGraph dg = new DotGraph(cg);
            String objectName = OutputPath + OutputFileName + "_" + i;
            if (!Files.exists((new File( objectName + ".dot")).toPath())) {
                Files.createFile((new File( objectName + ".dot")).toPath());
            }
            dg.toDotFile(new File( objectName + ".dot"));
            dg.toGraphics(objectName,"png");
            FileIO.writeObjectToFile(cg, objectName + ".dat", false);
        }
    }

    private RevCommit createCommitFromDiff(Repository repo, String before, String after) throws Exception{
        ObjectInserter inserter = repo.newObjectInserter();
        ObjectId blobIdBefore = inserter.insert(Constants.OBJ_BLOB, before.getBytes(StandardCharsets.UTF_8));
        TreeFormatter tfBefore = new TreeFormatter();
        tfBefore.append("Test.java", FileMode.REGULAR_FILE, blobIdBefore);
        ObjectId treeIdBefore = inserter.insert(tfBefore);

        PersonIdent who = new PersonIdent("dummy", "dummy@dummy.com", new Date(), TimeZone.getDefault());

        CommitBuilder cbBefore = new CommitBuilder();
        cbBefore.setAuthor(who);
        cbBefore.setCommitter(who);
        cbBefore.setTreeId(treeIdBefore);
        cbBefore.setMessage("init commit");
        ObjectId commitIdBefore = inserter.insert(cbBefore);
        inserter.flush();
        RevWalk rw = new RevWalk(repo);
        RevCommit commitBefore = rw.parseCommit(commitIdBefore);

        ObjectId blobAfter = inserter.insert(Constants.OBJ_BLOB, after.getBytes(StandardCharsets.UTF_8));
        TreeFormatter tfAfter = new TreeFormatter();
        tfAfter.append("Test.java", FileMode.REGULAR_FILE, blobAfter);
        ObjectId treeIdAfter = inserter.insert(tfAfter);

        CommitBuilder cbAfter = new CommitBuilder();
        cbAfter.setAuthor(who);
        cbAfter.setCommitter(who);
        cbAfter.setTreeId(treeIdAfter);
        cbAfter.setMessage("second commit");
        cbAfter.setParentIds(commitBefore);
        ObjectId commitIdAfter = inserter.insert(cbAfter);
        inserter.flush();
        RevCommit commitAfter = rw.parseCommit(commitIdAfter);

        rw.close();
        repo.close();
        return commitAfter;
    }
}
