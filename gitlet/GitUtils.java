package gitlet;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Date;
import java.io.File;
import java.text.SimpleDateFormat;

public class GitUtils implements Serializable {

    public void init() {
        if (GIT.exists()) {
            System.out.println("Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }
        if (!GIT.exists()) {
            GIT.mkdir();
        }
        if (!BLOBS.exists()) {
            BLOBS.mkdir();
        }
        if (!COMMITS.exists()) {
            COMMITS.mkdir();
        }
        if (!LISTOFBRANCHES.exists()) {
            LISTOFBRANCHES.mkdir();
        }
        if (!THESTAGE.exists()) {
            THESTAGE.mkdir();
        }

        TempStage remove = new TempStage("remove");
        TempStage add = new TempStage("add");
        remove.toFile();
        add.toFile();
        _currentBranch = "master";
        try {
            if (!MASTERBRANCH.exists()) {
                MASTERBRANCH.createNewFile();
            }
            if (!HEADFILE.exists()) {
                HEADFILE.createNewFile();
            }
            if (!LOG.exists()) {
                LOG.createNewFile();
            }
            if (!COMMITHASHFILE.exists()) {
                COMMITHASHFILE.createNewFile();
            }
            CommitHash c = new CommitHash();
            c.toFile();
        } catch (IOException excp) {
            System.exit(0);
        }
        commit("initial commit", true);
        setPersistence();




    }
    public void addRemote(String name, String dir) {
        CommitHash c = CommitHash.fromFile();
        if (c.containsRepo(name)) {
            System.out.println("A remote with "
                    + "that name already exists.");
            System.exit(0);
        } else {
            c.putRepo(name, new File(dir).getAbsolutePath());
            c.toFile();
        }
    }
    public void rmRemote(String name) {
        CommitHash c = CommitHash.fromFile();
        if (!c.containsRepo(name)) {
            System.out.println("A remote with "
                    + "that name does not exist.");
            System.exit(0);
        } else {
            c.rmRepo(name);
            c.toFile();
        }
    }
    public static void push(String name, String rmBranch) {
        CommitHash c = CommitHash.fromFile();
        Commit headCommit = Utils.readObject(HEADFILE, Commit.class);
        if (c.containsRepo(name)) {
            File rmGitlet = new File(c.getRepoPath(name));
            if (!rmGitlet.exists()) {
                System.out.println("Remote directory not found.");
                System.exit(0);
            }
            HashSet<String> a = ancArchivenoDist(headCommit);
            File branches = Utils.join(rmGitlet, "branches");
            File branch = Utils.join(branches, rmBranch);
            if (!branch.exists()) {
                try {
                    branch.createNewFile();
                    Utils.writeObject(branch, headCommit.getShaKey());
                } catch (IOException e) {
                    System.out.println("create branch did not exist");
                }
            } else {
                Commit bCommit = Commit.fromFile(Utils
                        .readObject(branch, String.class));
                if (a.contains(bCommit.getShaKey())) {
                    Commit toCOPY = bCommit;
                    writeCommittoFolder(toCOPY, rmGitlet);

                } else {
                    System.out.println("Please pull down remote"
                            + " changes before pushing.");
                    System.exit(0);
                }
            }
            String bCurrBranch = Utils.readObject(
                    Utils.join(rmGitlet,
                            "currentBranch"), String.class);
            if (bCurrBranch.equals(rmBranch)) {
                Utils.writeObject(Utils.join(rmGitlet,
                        "HEAD"), headCommit);
            } else {
                System.out.println("couldnt write head");
            }

        }
    }
    public static HashSet<String> getChildren(Commit root, File dir) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        HashSet<String> children = new HashSet<>();
        queue.addLast(root.getShaKey());
        Commit c;
        while (!queue.isEmpty()) {
            c = Utils.readObject(Utils.join(dir, queue.remove()),
                    Commit.class);
            children.add(c.getShaKey());
            if (!c.getChildSHAs().isEmpty()) {
                queue.addAll(c.getChildSHAs());
            }
        }
        return children;
    }
    public static HashSet<String> getParents(Commit root, File dir) {
        ArrayDeque<String> queue = new ArrayDeque<>();
        HashSet<String> parents = new HashSet<>();
        queue.addLast(root.getShaKey());
        dir = Utils.join(dir, "commits");
        Commit c;
        while (!queue.isEmpty()) {
            c = Utils.readObject(Utils.join(dir, queue.remove()),
                    Commit.class);
            parents.add(c.getShaKey());
            if (!c.getParentSha().equals("")) {
                queue.addLast(c.getShaKey());
            }
            if (c.getxtraParents() != null) {
                queue.addLast(c.getxtraParents());
            }
        }
        return parents;
    }
    public static void fetch(String rmName, String rmBranch) {
        CommitHash c = CommitHash.fromFile();
        if (!c.containsRepo(rmName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File branches = Utils.join(c.getRepoPath(rmName),
                "branches");
        File branch = Utils.join(branches, rmBranch);
        if (!branch.exists()) {
            System.out.println("That remote does "
                    + "not have that branch.");
            System.exit(0);
        }
        File commits = Utils.join(c.getRepoPath(rmName),
                "commits");
        Commit rmHead = Utils.readObject(Utils.join(commits,
                Utils.readObject(branch,
                        String.class)), Commit.class);
        HashSet<String> nodes = getParents(rmHead,
                new File(c.getRepoPath(rmName)));
        File j;
        File writer;
        for (String f : nodes) {
            j = Utils.join(commits, f);
            writer = Utils.join(COMMITS, f);
            if (!writer.exists()) {
                Utils.writeObject(writer, Utils.readObject(j,
                        Commit.class));
            }
        }
        File newBranch = Utils.join(LISTOFBRANCHES,
                rmName + "/" + rmBranch);
        try {
            newBranch.createNewFile();
            Utils.writeObject(newBranch, rmHead);
        } catch (IOException e) {
            System.out.println("Couldnt create branch");
        }

    }
    public static void writeCommittoFolder(Commit item, File gitlet) {
        File dest = Utils.join(gitlet, "commits");
        File commit = Utils.join(dest, item.getShaKey());
        File blobF = Utils.join(gitlet, "blobs");
        if (!commit.exists()) {
            try {
                commit.createNewFile();
                Utils.writeObject(commit, item);
                for (String b : item.getfileNameSHASBlob().values()) {
                    writeBlobtoFolder(Blob.fromFile(b), blobF);
                }
            } catch (IOException E) {
                System.out.println("couldnt write commit over");
            }
            if (!item.getChildSHAs().isEmpty()) {
                HashSet<String> childSha = new HashSet<>(item
                        .getChildSHAs());
                for (String c : childSha) {
                    writeCommittoFolder(Commit.fromFile(c), dest);
                }
            }
        } else if (!item.getLogMsg().equals("initial commit")) {
            Utils.message("commit already existed in %s",
                    commit.getAbsolutePath());
        }
    }
    public static void writeBlobtoFolder(Blob b, File dest) {
        File blob = Utils.join(dest, b.getShaKey());
        if (!blob.exists()) {
            try {
                blob.createNewFile();
                Utils.writeObject(blob, b);
            } catch (IOException e) {
                System.out.println("couldnt write blob");
            }
        }
    }
    public static void commitDescription(Commit c, boolean initCommit) {
        String message = "===\n" + "commit " + c.getShaKey() + "\n"
                + "Date: " + c.getTime()
                + "\n" + c.getLogMsg() + "\n" + "\n";
        File f = new File(GIT, "log.txt");
        Utils.writeContents(f, message + (initCommit ? ""
                : Utils.readContentsAsString(f)));
    }
    public static String getGlobalLog() {
        setPersistence();
        return Utils.readContentsAsString(LOG);
    }
    public static String getLog() {
        setPersistence();
        if (_headCommit == null) {
            System.out.println("Head Commit null for Global Log");
            System.exit(0);
        }
        Commit tWalker = _headCommit;
        String message = "===\n" + "commit " + tWalker.getShaKey() + "\n"
                + "Date: " + tWalker.getTime()
                + "\n" + tWalker.getLogMsg() + "\n" + "\n";
        while (tWalker.getParentSha() != "") {
            tWalker = Commit.fromFile(tWalker.getParentSha());
            message += "===\n" + "commit " + tWalker.getShaKey() + "\n"
                    + "Date: " + tWalker.getTime()
                    + "\n" + tWalker.getLogMsg() + "\n" + "\n";
        }
        return message;
    }
    public void commit(String logmsg, boolean initCommit) {
        if (logmsg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Date d = new Date();

        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String time = formatter.format(d);
        Commit c = new Commit(logmsg, (initCommit
                ? "Thu Jan 1 00:00:00 1970 -0800"
                : time));
        c.setShaKey(Utils.sha1(Utils.serialize(c)));
        if (!initCommit) {
            setPersistence();
            TempStage addStage = TempStage.fromfile(TempStage.ADD);
            TempStage rmStage = TempStage.fromfile(TempStage.REMOVE);
            if (addStage.getStage().isEmpty()
                    && rmStage.getStage().isEmpty()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            c.setfileNameSHASBlob(copyTree(
                    addStage.getStage(), new TreeMap<>()));
            addStage.clearStage();
            addStage.toFile();
            for (String fileName
                    : _headCommit.getFiles()) {
                if (!c.containsFile(fileName)) {
                    c.addtofileNameSHASBlob(fileName,
                            _headCommit.getfileNameSHASBlob().get(fileName));
                }
            }

            for (String f : rmStage.getStage().keySet()) {
                if (c.containsFile(f, rmStage.getStage().get(f))) {
                    c.removeFile(f);
                }
            }
            rmStage.clearStage();
            rmStage.toFile();
        }

        commitDescription(c, initCommit);
        if (!initCommit) {
            _headCommit.addChild(c.getShaKey());
            c.setParentSha(_headCommit.getShaKey());
        }
        _headCommit = c;
        c.toFile();
        Utils.writeObject(HEADFILE, _headCommit);
        Utils.writeObject(Utils.join(LISTOFBRANCHES,
                _currentBranch), _headCommit.getShaKey());



    }

    /**
     * Checks FILENAME out to COMMITID version if exists.
     * @param commitID
     * A commit ID
     * @param fileName
     * A file name
     */
    public static void gitCheckOut(String commitID,
                                   String fileName) {
        try {
            if (commitID.length() < HASHLENGTH) {
                for (String fName : Utils.plainFilenamesIn(COMMITS)) {
                    if (fName.startsWith(commitID)) {
                        Commit cHash = Commit.fromFile(fName);
                        File f = new File(GitUtils.CWD, fileName);
                        Utils.writeContents(f,
                                Blob.fromFile(cHash.getBlob(fileName))
                                        .contentAsString());
                        return;
                    }
                }
            }
            Commit cCore = Commit.fromFile(commitID);
            if (!cCore.getfileNameSHASBlob().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            } else {
                File f = new File(GitUtils.CWD, fileName);
                Utils.writeContents(f,
                        Blob.fromFile(cCore
                                .getBlob(fileName)).contentAsString());
            }
        } catch (Exception e) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

    }

    /**Checks FILENAME out to most recent commit version. */
    public static void gitCheckOut(String fileName) {
        setPersistence();
        gitCheckOut(_headCommit.getShaKey(), fileName);
    }

    public void gitReset(String id) {
        try {
            Commit c = Commit.fromFile(id);
            String pstBranch = _currentBranch;
            String rstBranch = "rstBranch";
            File rBranch = Utils.join(LISTOFBRANCHES, rstBranch);
            while (rBranch.exists()) {
                rstBranch += 1;
                rBranch = Utils.join(LISTOFBRANCHES, rstBranch);
            }
            rBranch.createNewFile();
            Utils.writeObject(rBranch, c.getShaKey());
            gitCheckoutBranch(rstBranch);
            Utils.writeObject(Utils.join(LISTOFBRANCHES,
                    pstBranch), c.getShaKey());
            gitCheckoutBranch(pstBranch);
            rmBranch(rstBranch);
        } catch (RuntimeException | IOException e) {
            Utils.message("No commit with that id exists.");
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    public void rmBranch(String bName) {
        if (bName.equals(_currentBranch)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        File bBranch = Utils.join(LISTOFBRANCHES, bName);
        if (bBranch.exists() && !bBranch.isDirectory()) {
            bBranch.delete();
        } else {
            Utils.message("branch with that name does not exist.");
            System.exit(0);
        }
    }

    public void gitCheckoutBranch(String branchName) {
        setPersistence();
        if (branchName.equals(_currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit compareCommit;
        for (String bName : Utils.plainFilenamesIn(LISTOFBRANCHES)) {
            if (bName.equals(branchName)) {
                File curBranch = new File(LISTOFBRANCHES, _currentBranch);
                Utils.writeObject(curBranch, _headCommit.getShaKey());
                File bFile = Utils.join(LISTOFBRANCHES, bName);
                compareCommit = Commit.fromFile(
                        Utils.readObject(bFile, String.class));
                for (String fWalker : Utils.plainFilenamesIn(CWD)) {
                    if (compareCommit.containsFileName(fWalker)
                            && !_headCommit.containsFile(fWalker)) {
                        System.out.println("There is an untracked "
                                + "file in the way; "
                                + "delete it, or add and commit it first.");
                        System.exit(0);

                    }
                }
                for (String fWalker
                        : _headCommit.getFiles()) {
                    if (!compareCommit.getfileNameSHASBlob()
                            .containsKey(fWalker)) {
                        Utils.restrictedDelete(fWalker);
                    }
                }
                for (String fWalker : compareCommit.getFiles()) {
                    Utils.writeContents(Utils.join(CWD, fWalker),
                            Blob.fromFile(compareCommit.getBlob(fWalker))
                                    .contentAsString());
                }
                _headCommit = compareCommit;
                _currentBranch = branchName;
                Utils.writeObject(HEADFILE, _headCommit);
                TempStage addStage = TempStage.fromfile(TempStage.ADD);
                TempStage rmStage = TempStage.fromfile(TempStage.REMOVE);
                addStage.clearStage();
                rmStage.clearStage();
                addStage.toFile();
                rmStage.toFile();
                return;

            }
        }
        System.out.println("No such branch exists.");
    }

    public String lstCommonAnc(String aSHA, String bSHA) {
        Commit aBranch = Commit.fromFile(aSHA);
        Commit bBranch = Commit.fromFile(bSHA);
        HashSet<String> bParents = ancArchivenoDist(bBranch);
        return ancArchive(aBranch, bParents);
    }
    public static HashSet<String> ancArchivenoDist(Commit aCommit) {
        HashSet<String> ancHist = new HashSet<>();
        ArrayDeque<String> findSplits = new ArrayDeque<>();
        findSplits.push(aCommit.getShaKey());
        while (!findSplits.isEmpty()) {
            Commit aWalker = Commit.fromFile(findSplits.remove());
            ancHist.add(aWalker.getShaKey());
            if (!aWalker.getParentSha().equals("")) {
                findSplits.push(aWalker.getParentSha());
            }
            if (aWalker.getxtraParents() != null) {
                findSplits.push(aWalker.getxtraParents());
            }
        }
        return ancHist;
    }

    public String ancArchive(Commit aCommit, HashSet<String> bArc) {
        ArrayDeque<String> findSplit = new ArrayDeque<>();
        findSplit.push(aCommit.getShaKey());
        while (!findSplit.isEmpty()) {
            Commit aWalker = Commit.fromFile(findSplit.remove());
            if (bArc.contains(aWalker.getShaKey())) {
                return aWalker.getShaKey();
            }
            if (!aWalker.getParentSha().equals("")) {
                findSplit.addLast(aWalker.getParentSha());
            }
            if (aWalker.getxtraParents() != null) {
                findSplit.addLast(aWalker.getxtraParents());
            }
        }
        return null;
    }
    public void handleMergeCases(Commit ancCont,
                                        Commit headCont, Commit bCont) {
        ArrayList<String> bFileSet = bCont.getFiles();
        boolean hasConf = false;
        int actOn;
        for (String f : headCont.getFiles()) {
            bFileSet.remove(f);
            actOn = findMergeCases(bCont.getBlob(f),
                    headCont.getBlob(f),
                    ancCont.getBlob(f));
            if (actOn == 1) {
                gitCheckOut(bCont.getShaKey(), f);
                add(f);
            } else if (actOn == -1) {
                remove(f);
            } else if (actOn == 2) {
                writeConflicttoFile(bCont, f);
                hasConf = true;
            }

        }
        for (String f : bFileSet) {
            actOn = findMergeCases(bCont.getBlob(f), headCont.getBlob(f),
                    ancCont.getBlob(f));
            if (actOn == 1) {
                gitCheckOut(bCont.getShaKey(), f);
                add(f);
            } else if (actOn == -1) {
                remove(f);
            } else if (actOn == 2) {
                writeConflicttoFile(bCont, f);
                hasConf = true;
            }
        }
        for (String bName : Utils.plainFilenamesIn(LISTOFBRANCHES)) {
            if (Utils.readObject(Utils.join(LISTOFBRANCHES, bName),
                    String.class).equals(bCont.getShaKey())) {
                commit("Merged " + bName + " into "
                        + _currentBranch + ".", false);
                break;
            }
        }
        _headCommit.addxtraParents(bCont.getShaKey());
        bCont.toFile();
        _headCommit.toFile();
        if (hasConf) {
            System.out.println("Encountered a merge conflict.");
            System.exit(0);
        }

    }
    public void writeConflicttoFile(Commit bCommit, String fName) {
        String msgStatus = "<<<<<<< HEAD\n";
        if (_headCommit.containsFileName(fName)) {
            msgStatus += Blob.fromFile(
                    _headCommit.getBlob(fName)).contentAsString();
        }
        msgStatus += "=======\n";
        if (bCommit.containsFileName(fName)) {
            msgStatus += Blob.fromFile(
                    (bCommit.getBlob(fName))).contentAsString();
        }
        msgStatus += ">>>>>>>\n";
        Utils.writeContents(Utils.join(CWD, fName), msgStatus);
        add(fName);
    }

    /**
     *
     * @param bCont
     * file content in given rbanch.
     * @param cCont
     * file content in curretn branch.
     * @param aCont
     * file content in split.
     * @return
     * 0 nothing to deal with
     * 1 checkout and add file that is in given branch
     * -1 call rm on file name
     * 2 A merge conflict
     */
    public static int findMergeCases(String bCont, String cCont, String aCont) {
        aCont = aCont == null ? "null" : aCont;
        bCont = bCont == null ? "null" : bCont;
        cCont = cCont == null ? "null" : cCont;
        if (aCont.equals("null")) {
            if (!cCont.equals("null") && bCont.equals("null")) {
                return 0;
            } else if (cCont.equals("null") && !bCont.equals("null")) {
                return 1;
            } else if (!bCont.equals("null")
                && !cCont.equals("null") && !bCont.equals(cCont)) {
                return 2;
            }
        } else {
            if (!bCont.equals("null") && !cCont.equals("null")) {
                if (cCont.equals(aCont) && !bCont.equals(aCont)) {
                    return 1;
                } else if (!cCont.equals(aCont) && bCont.equals(aCont)) {
                    return 0;
                } else if (!cCont.equals(aCont) && !bCont.equals(aCont)) {
                    if (cCont.equals(bCont)) {
                        return 0;
                    } else {
                        return 2;
                    }
                }
            } else if (!cCont.equals("null") && bCont.equals("null")) {
                if (cCont.equals(aCont)) {
                    return -1;
                } else if (!cCont.equals(aCont)) {
                    return 2;
                }
            } else if (cCont.equals("null") && !bCont.equals("null")) {
                if (!bCont.equals(aCont)) {
                    return 2;
                }
            }

        }
        return 0;
    }

    /**
     * Returns a treemap with for each file name.
     * 0 if unmodded since split
     * 1 if modded since split
     * 2 if added since split
     * 3 if removed since split
     * @param sPoint
     * split point commit
     * @param bPoint
     * head commit
     * @return
     * TreeMap<String, Integer>
     */
    public static TreeMap<String, Integer> fileFromSplit
    (Commit sPoint, Commit bPoint) {
        TreeMap<String, Integer> allPos = new TreeMap<>();
        ArrayList<String> rmSinceSplit = sPoint.getFiles();
        for (String fWalker : bPoint.getFiles()) {
            if (sPoint.containsFileName(fWalker)) {
                rmSinceSplit.remove(fWalker);
                if (!sPoint.getBlob(fWalker).equals(bPoint.getBlob(fWalker))) {
                    allPos.put(fWalker, 1);
                } else {
                    allPos.put(fWalker, 0);
                }
            } else {
                allPos.put(fWalker, 2);
            }
        }
        for (String fWalker : rmSinceSplit) {
            allPos.put(fWalker, 3);
        }
        return allPos;
    }

    public void mergeBranch(String bName) {
        setPersistence();
        TempStage addStage = TempStage.fromfile(TempStage.ADD);
        TempStage rmStage = TempStage.fromfile(TempStage.REMOVE);
        File mBranch = Utils.join(LISTOFBRANCHES, bName);
        if (bName.equals(_currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (!mBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (addStage.size() > 0 || rmStage.size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Commit brCommit = Commit.fromFile(Utils.readObject(mBranch,
                String.class));
        for (String fWalker : Utils.plainFilenamesIn(CWD)) {
            if (brCommit.containsFileName(fWalker)
                    && !_headCommit.containsFileName(fWalker)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }
        }
        String anNode = lstCommonAnc(_headCommit.getShaKey(),
                brCommit.getShaKey());
        if (anNode.equals(brCommit.getShaKey())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        } else if (anNode.equals(_headCommit.getShaKey())) {
            Commit cHead = Commit.fromFile(_headCommit.getShaKey());
            String currBranch = _currentBranch;
            gitCheckoutBranch(bName);
            Utils.writeContents(Utils.join(LISTOFBRANCHES,
                    currBranch), cHead.getShaKey());
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        Commit splitPoint = Commit.fromFile(anNode);
        handleMergeCases(splitPoint, _headCommit, brCommit);
    }




    /**
     * Returns TreeMap copy from k.
     * @param k
     * TreeMap to copy from
     * @param v
     * TreeMap to copy to
     * @return TreeMap
     */
    public TreeMap<String, String> copyTree
    (TreeMap<String, String> k, TreeMap<String, String> v) {

        for (String filesName : k.keySet()) {
            v.put(filesName, k.get(filesName));
        }
        return v;

    }

    public void add(String fileName) {
        File toAdd = new File(CWD, fileName);
        setPersistence();
        TempStage addStage = TempStage.fromfile(TempStage.ADD);
        TempStage rStage = TempStage.fromfile(TempStage.REMOVE);
        if (!toAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else if (rStage.contains(fileName, false)) {
            rStage.removeFromtempStage(fileName);
            rStage.toFile();
        } else if (_headCommit.containsFile(fileName)
                || addStage.contains(fileName, true)) {
            return;

        } else {
            TempStage add = TempStage.fromfile(TempStage.ADD);
            Blob f = new Blob(toAdd);
            add.addTotempStage(f.getName(), f.getShaKey());
            f.toFile();
            add.toFile();
        }


    }

    public static GitUtils fromFile() {
        return Utils.readObject(SAVE, GitUtils.class);
    }

    public void toFile() {
        Utils.writeObject(SAVE, this);
        Utils.writeObject(CURRBRANCH, _currentBranch);
    }


    public void remove(String fileName) {
        File toRemove = new File(CWD, fileName);
        setPersistence();
        TempStage rmStage = TempStage.fromfile(TempStage.REMOVE);
        TempStage addStage = TempStage.fromfile(TempStage.ADD);
        if (addStage.contains(fileName, false)) {
            addStage.removeFromtempStage(fileName);
            addStage.toFile();
        } else if (_headCommit
                .getfileNameSHASBlob().containsKey(fileName)) {
            GitUtils.setPersistence();
            rmStage.addTotempStage(fileName,
                    _headCommit
                            .getfileNameSHASBlob()
                            .get(fileName));
            rmStage.toFile();
            if (toRemove.exists()) {
                Utils.restrictedDelete(toRemove);
            }


        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }
    /** Returns String of status of gitlet. */
    public String statusGitLet() {
        setPersistence();
        String msgStatus = "=== Branches ===\n";
        for (String fWalker : Utils.plainFilenamesIn(LISTOFBRANCHES)) {
            if (!fWalker.equals(_currentBranch)) {
                msgStatus += fWalker + "\n";
            } else {
                msgStatus += "*" + _currentBranch + "\n";
            }
        }
        msgStatus += "\n=== Staged Files ===\n";
        TempStage addStage = TempStage.fromfile(TempStage.ADD);
        for (String fName : addStage.getStage().keySet()) {
            msgStatus += fName + "\n";
        }
        msgStatus += "\n=== Removed Files ===\n";
        TempStage rmStage = TempStage.fromfile(TempStage.REMOVE);
        for (String fName : rmStage.getStage().keySet()) {
            msgStatus += fName + "\n";
        }
        msgStatus += "\n=== Modifications Not Staged For Commit ===\n";
        String untrckFiles = "";
        ArrayList<String> rmFiles = _headCommit.getFiles();
        File fFile;
        String msgIgnore = Utils.readObject(GITIGNORE, String.class);
        for (String fWalker : Utils.plainFilenamesIn(CWD)) {
            fFile = new File(CWD, fWalker);
            rmFiles.remove(fWalker);
            if (_headCommit.containsNewerFile(fWalker)) {
                if ((addStage.contains(fWalker, false)
                        && !Blob.fromFile(addStage.getStage()
                        .get(fWalker))
                        .isEqual(Utils
                                .readContentsAsString(fFile)))
                        || !addStage.contains(fWalker,
                        false)) {
                    msgStatus += fWalker + "(modified)\n";
                }
            } else if (!msgIgnore.contains(fWalker)
                    && !addStage.contains(fWalker, false)
                    && !rmStage.contains(fWalker, false)
                    && !_headCommit.
                    getfileNameSHASBlob().containsKey(fWalker)) {
                untrckFiles += "\n" + fWalker;
            }
        }
        for (String fName : rmFiles) {
            msgStatus += (addStage.contains(fName, true)
                    || !rmStage.contains(fName, false))
                    ? fName + "(deleted)\n" : "";
        }
        msgStatus += "\n=== Untracked Files ===" + untrckFiles + "\n";
        return  msgStatus;
    }

    /** Creates a branch commit BRANCH if it doesnt
     * exist yet in the LIST OF BRANCHES folder.
     */
    public void setBranch(String branch) {
        setPersistence();
        File branchCommit = new File(LISTOFBRANCHES, branch);
        if (!branchCommit.exists()) {
            Commit newCommit = _headCommit;
            Utils.writeObject(branchCommit, newCommit.getShaKey());
        } else {
            Utils.error("A branch with that name already exists.");
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }
    /** Makes sure key variable exist. */
    public static void setPersistence() {
        if (!GIT.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (!BLOBS.exists()) {
            BLOBS.mkdir();
        }
        if (!COMMITS.exists()) {
            COMMITS.mkdir();
        }
        if (!LISTOFBRANCHES.exists()) {
            LISTOFBRANCHES.mkdir();
        }
        if (!THESTAGE.exists()) {
            BLOBS.mkdir();
        }
        _headCommit = Utils.readObject(HEADFILE, Commit.class);



        try {
            if (!MASTERBRANCH.exists()) {
                MASTERBRANCH.createNewFile();
            }
            if (!HEADFILE.exists()) {
                HEADFILE.createNewFile();
            }
            if (!LOG.exists()) {
                LOG.createNewFile();
            }
            if (!COMMITHASHFILE.exists()) {
                COMMITHASHFILE.createNewFile();
            }
            if (!CURRBRANCH.exists()) {
                CURRBRANCH.createNewFile();
            }

            try {
                CommitHash c = CommitHash.fromFile();
            } catch (RuntimeException e) {
                CommitHash c = new CommitHash();
                c.toFile();
            }
            if (!GITIGNORE.exists()) {
                GITIGNORE.createNewFile();
                Utils.writeObject(GITIGNORE,
                        ".gitletignore\nMakefile\n"
                                + "proj3.iml\n.gitignore\n"
                                + "desktop.ini");
            }
        } catch (IOException excp) {
            System.exit(0);
        }
    }

    /** Returns STRING of all commits with given MSG. */
    public static String findCommits(String msg) {
        String idStrings = "";
        Commit commitID;
        boolean fLoop = true;
        for (String cName : Utils.plainFilenamesIn(COMMITS)) {
            commitID = Commit.fromFile(cName);
            if (commitID.getLogMsg().equals(msg)) {
                if (fLoop) {
                    fLoop = false;
                    idStrings += commitID.getShaKey();
                } else {
                    idStrings += "\n" + commitID.getShaKey();
                }
            }
        }
        return idStrings.isEmpty() ? "Found no commit with that message."
                : idStrings;
    }
    public Commit getHeadCommit() {
        return _headCommit;
    }
    public String getCurrentBranch() {
        return _currentBranch;
    }
    public void setCurrentBranch(String nBranch) {
        _currentBranch = nBranch;
    }





    /** The current working directory. */
    public static final File CWD = new File(".");
    /** Current branch I am on. */
    private String _currentBranch;
    /** .git folder for holding things. */
    public static final File GIT = Utils.join(CWD, ".gitlet");
    /** Where the gitlet obj saves and reads itself. */
    public static final File SAVE = Utils.join(GIT, "save");
    /** The txt file for the global log. */
    public static final File LOG = Utils.join(GIT, "log.txt");
    /** The commit hash file. */
    public static final File COMMITHASHFILE = Utils.join(GIT, "CommitHash");
    /** dir for blobs. */
    public static final File BLOBS = Utils.join(GIT, "blobs");
    /** dir for commits. */
    public static final File COMMITS = Utils.join(GIT, "commits");
    /** dir for branches. */
    public static final File LISTOFBRANCHES = Utils.join(GIT, "branches");
    /** directory for stages. */
    public static final File THESTAGE = Utils.join(GIT, "stage");
    /** The directory for master branch. */
    public static final File MASTERBRANCH = Utils.join(GIT, "branches/master");
    /** file for Head commit. */
    public static final File HEADFILE = Utils.join(GIT, "HEAD");
    /** File for ignoring other files. */
    public static final File GITIGNORE = Utils.join(CWD, ".gitletignore");
    /** The current branch. */
    public static final File CURRBRANCH = Utils.join(GIT, "currentBranch");
    /** The head commit. */
    private static Commit _headCommit;
    /** hash length for commit id. */
    public static final int HASHLENGTH = 40;


}
