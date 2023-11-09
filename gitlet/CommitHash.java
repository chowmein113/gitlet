package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

public class CommitHash implements Serializable {
    public CommitHash() {
        _CommitTable = new TreeMap<>();
    }
    public TreeMap<String, String> getCommitTable() {
        return _CommitTable;
    }
    public void addtoCommitTable(Commit c) {
        _CommitTable.put(c.getShaKey(), c.toFile());
    }
    public Commit findCommit(String sha, GitUtils commandCentral) {
        return Utils.readObject(new File(_CommitTable.get(sha)), Commit.class);
    }
    public void toFile() {
        File self = GitUtils.COMMITHASHFILE;
        try {
            if (!self.exists()) {
                self.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("commit did not work");
        }
        Utils.writeObject(self, this);
    }
    public boolean containsRepo(String r) {
        return _CommitTable.containsKey(r);
    }
    public void putRepo(String r, String d) {
        _CommitTable.put(r, d);
    }
    public String getRepoPath(String r) {
        return _CommitTable.get(r);
    }
    public void rmRepo(String name) {
        _CommitTable.remove(name);
    }
    public static CommitHash fromFile() {
        File self = GitUtils.COMMITHASHFILE;
        return Utils.readObject(self, CommitHash.class);
    }
    /** Commit table. */
    private TreeMap<String, String> _CommitTable;


}
