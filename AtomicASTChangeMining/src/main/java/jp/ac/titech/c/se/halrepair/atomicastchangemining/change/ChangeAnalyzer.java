package jp.ac.titech.c.se.halrepair.atomicastchangemining.change;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.main.MainChangeAnalyzer;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.Config;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.utils.FileIO;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.graphics.DotGraph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;

import jp.ac.titech.c.se.halrepair.atomicastchangemining.repository.GitConnector;
import jp.ac.titech.c.se.halrepair.atomicastchangemining.repository.SVNConnector;

public class ChangeAnalyzer {
    private final String projectName;
    private final String url;
    private int projectId;
    private long startRevision = -1, endRevision = -1;
    private int numOfRevisions = -1, numOfCodeRevisions = -1, numOfExtractedRevisions = -1;
    private SVNConnector svnConn;
    private GitConnector gitConn;
    private HashMap<Long, SVNLogEntry> logEntries;
    private final ArrayList<RevisionAnalyzer> revisionAnalyzers = new ArrayList<RevisionAnalyzer>();
    private CProject cproject;

    public ChangeAnalyzer(String projectName, int projectId, String svnUrl, long start, long end) {
        this.projectName = projectName;
        this.projectId = projectId;
        this.url = svnUrl;
        this.startRevision = start;
        this.endRevision = end;
    }

    public ChangeAnalyzer(String projectName, int projectId, String svnUrl) {
        this.projectName = projectName;
        this.projectId = projectId;
        this.url = svnUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public SVNConnector getSvnConn() {
        return this.svnConn;
    }

    public long getStartRevision() {
        return startRevision;
    }

    public long getEndRevision() {
        return endRevision;
    }

    public int getNumOfRevisions() {
        if (numOfRevisions == -1) {
            numOfRevisions = this.gitConn.getNumberOfCommits(null);
        }
        return numOfRevisions;
    }

    public int getNumOfCodeRevisions() {
        return numOfCodeRevisions;
    }

    public void incrementNumOfCodeRevisions() {
        this.numOfCodeRevisions++;
    }

    public SVNLogEntry getLogEntry(long revision) {
        return this.logEntries.get(revision);
    }

    public ArrayList<RevisionAnalyzer> getRevisionAnalyzers() {
        return revisionAnalyzers;
    }

    public CProject getCproject() {
        return cproject;
    }

    public void buildSvnConnector() {
        svnConn = new SVNConnector(url, "guest", "guest");
        svnConn.connect();
        if (this.startRevision == -1) {
            this.startRevision = 1;
            svnConn.setLatestRevision();
            this.endRevision = svnConn.getLatestRevision();
        }
    }

    public void buildGitConnector() {
        this.gitConn = new GitConnector(url + "/.git");
        this.gitConn.connect();
    }

    public void setDummyGitConnector(GitConnector gitConn){
        this.gitConn = gitConn;
        this.gitConn.connect();
    }

    public void closeGitConnector() {
        this.gitConn.close();
    }

    public void buildLogEntries() {
        this.logEntries = new HashMap<Long, SVNLogEntry>();
        long start = this.startRevision;
        while (start <= this.endRevision) {
            long end = start + 99;
            if (end > this.endRevision) {
                end = this.endRevision;
            }
            buildLogEntries(start, end);
            start = end + 1;
        }
    }

    private void buildLogEntries(long startRevision, long endRevision) {
        Collection<?> logEntries = null;
        try {
            logEntries = svnConn.getRepository().log(new String[]{""}, null,
                    startRevision, endRevision, true, true);
        } catch (SVNException svne) {
            System.out.println("Error while collecting log information for '" + url + "': " + svne.getMessage());
            return;
        }

        for (Iterator<?> entries = logEntries.iterator(); entries.hasNext(); ) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            this.logEntries.put(logEntry.getRevision(), logEntry);
        }
    }

    public void buildLogAndAnalyze() {
        this.cproject = new CProject(projectId, projectName);
        this.cproject.numOfAllRevisions = this.endRevision - this.startRevision + 1;
        this.cproject.revisions = new ArrayList<CRevision>();
        this.logEntries = new HashMap<Long, SVNLogEntry>();
        long start = this.startRevision;
        while (start <= this.endRevision) {
            long end = start + 99;
            if (end > this.endRevision) {
                end = this.endRevision;
            }
            buildLogEntries(start, end);
            analyze(start, end);
            start = end + 1;
        }
    }

    public void analyze() {
        analyze(this.startRevision, this.endRevision);
    }

    public void analyzeGit() {
        this.cproject = new CProject(projectId, projectName);
        this.cproject.revisions = new ArrayList<CRevision>();
        File dir = new File(MainChangeAnalyzer.outputPath + "/" + projectName);
        Iterable<RevCommit> commits = null;
        try {
            ObjectId head = this.gitConn.getRepository().resolve(Constants.HEAD);
            commits = this.gitConn.getGit().log().add(head).call();
        } catch (GitAPIException | RuntimeException e) {
            System.err.println(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
        }

        if (commits == null) return;
        this.numOfCodeRevisions = 0;
        this.numOfRevisions = 0;
        this.numOfExtractedRevisions = 0;
        for (final RevCommit commit : commits) {
            if (numOfExtractedRevisions >= Config.MAX_EXTRACTED_COMMITS) break;
            File file = new File(dir.getAbsolutePath() + "/" + commit.getName() + ".dat");
            if (file.exists()) {
                numOfExtractedRevisions++;
                continue;
            }
            analyzeGit(commit);
        }
        this.cproject.numOfAllRevisions = this.numOfRevisions;
    }

    public void analyzeGit(RevCommit commit) {
        this.numOfRevisions++;
        if (this.numOfRevisions % 1000 == 0) {
            System.out.println("Analyzing revision: " + this.numOfRevisions + " " + commit.getName() + " from " + projectName);
        }

        RevisionAnalyzer ra = new RevisionAnalyzer(this, commit);
        boolean analyzed = ra.analyzeGit();
        if (analyzed) {
            HashMap<String, HashMap<String, ChangeGraph>> changeGraphs = new HashMap<>();
            for (CMethod e : ra.getMappedMethodsM()) {
                ChangeGraph cg = e.getChangeGraph(this.gitConn.getRepository(), commit);
                int[] csizes = cg.getChangeSizes();
                System.out.println("found:" + commit.getName());
                if (csizes[0] > 0 && csizes[1] > 0
                        && (csizes[0] + csizes[1]) >= 3
                        && csizes[0] <= 100 && csizes[1] <= 100
                        && cg.hasMethods()) {

                    // DEBUG
                    DotGraph dg = new DotGraph(cg);
                    File dir = new File(MainChangeAnalyzer.outputPath + "/" + "tmp");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    File dirPath = new File(dir.getAbsolutePath() + "/" + commit.getName());
                    if (!dirPath.exists()) {
                        dirPath.mkdirs();
                    }

                    try {
                        if (!Files.exists((new File(dirPath + "/" + e.getFullName() + ".dot")).toPath())) {
                            Files.createFile((new File(dirPath + "/" + e.getFullName() + ".dot")).toPath());
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    dg.toDotFile(new File(dirPath + "/" + e.getFullName() + ".dot"));
                    dg.toGraphics(dirPath + "/" + e.getFullName(), "png");

                    HashMap<String, ChangeGraph> cgs = changeGraphs.get(e.getCFile().getPath());
                    if (cgs == null) {
                        cgs = new HashMap<>();
                        changeGraphs.put(e.getCFile().getPath(), cgs);
                    }
                    cgs.put(e.getCClass().getName() + "," + e.getSimpleName()
                            + "," + e.getParameterTypes() + "," + e.startLine, cg);
                }
                e.cleanForStats();
            }
            if (!changeGraphs.isEmpty()) {
                File dir = new File(MainChangeAnalyzer.outputPath + "/" + projectName);
                if (!dir.exists())
                    dir.mkdirs();
                FileIO.writeObjectToFile(changeGraphs, dir.getAbsolutePath() + "/" + commit.getName() + ".dat", false);
                numOfExtractedRevisions++;
            }
        }
    }

    private void analyze(long startRevision, long endRevision) {
        for (long r = startRevision; r <= endRevision; r++) {
            if (this.logEntries.containsKey(r)) {
                System.out.println("Analyzing revision: " + r);
                SVNLogEntry logEntry = this.logEntries.get(r);
                if (logEntry != null && logEntry.getDate() != null) {
                    RevisionAnalyzer ra = new RevisionAnalyzer(this, r);
                    boolean analyzed = ra.analyze();
                    if (analyzed) {
                        if (ra.getCRevision() != null) {
                            this.cproject.revisions.add(ra.getCRevision());
                        }
                        if (!ra.getMappedMethodsM().isEmpty())
                            ra.getCRevision().methods = new ArrayList<CMethod>();
                        for (CMethod e : ra.getMappedMethodsM()) {
                            e.getChangeGraph(null, null);
                            ra.getCRevision().methods.add(e);
                            e.cleanForStats();
                        }
                    }
                }
            }
        }
    }

    public String getSourceCode(String changedPath, long revision) {
        return this.svnConn.getFile(changedPath, revision);
    }

    public GitConnector getGitConn() {
        return this.gitConn;
    }
}
