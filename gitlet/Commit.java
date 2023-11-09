package gitlet;

import java.io.File;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class Commit implements Serializable {
    public Commit(String logmsg, String time) {
        _blobs = new ArrayList<>();
        _fileNametoSHABlob = new TreeMap<>();
        _childShas = new ArrayList<>();
        _time = time;
        _logMsg = logmsg;
        _parentsha = "";
        _shakey = Utils.sha1(Utils.serialize(time + logmsg));
        _xtraParents = new ArrayList<>();
    }
    public String toFile() {
        File self = new File(GitUtils.COMMITS, _shakey);

        Utils.writeObject(self, this);
        return self.getPath();
    }
    public static Commit fromFile(String id) {
        File fCommit = Utils.join(GitUtils.COMMITS, id);
        if (fCommit.exists()) {
            return Utils.readObject(fCommit, Commit.class);
        } else {
            Utils.message("No commit with that id exists.");
            System.exit(0);

        }
        return new Commit("r", "56");
    }
    public ArrayList<String> getFiles() {
        return new ArrayList<>(_fileNametoSHABlob.keySet());
    }
    public boolean containsFile(Blob f) {
        for (Blob b : _blobs) {
            if (b.getName().equals(f.getName())
                    && b.isEqual(f.contentAsString())
                    && b.getVersion() == f.getVersion()) {
                return true;
            }
        }
        return false;
    }

    /** returns TRUE if commit contains filename F. */
    public boolean containsFile(String f) {
        File file = Utils.join(GitUtils.CWD, f);
        return _fileNametoSHABlob.containsKey(f)
                && !containsNewerFile(file);
    }

    /**
     * Returns TRUE if file and blob
     * are contained in FILENAMETOSHABLOB.
     * @param f
     * file name string
     * @param blob
     * blob of filename for comparing versions
     * @return true or false
     */
    public boolean containsFile(String f, String blob) {
        return _fileNametoSHABlob.containsKey(f)
                && !containsFile(Blob.fromFile(blob));
    }

    /**
     *
     * @param fName
     * File name
     * @return TRUE if file name is in this commit.
     */
    public boolean containsFileName(String fName) {
        return _fileNametoSHABlob.containsKey(fName);
    }

    /** Removes file name F from commit's treemap. */
    public void removeFile(String f) {
        if (_fileNametoSHABlob.containsKey(f)) {
            _fileNametoSHABlob.remove(f);
        }
    }

    public boolean containsNewerFile(File f) {
        if (_fileNametoSHABlob.containsKey(f.getName())) {
            Blob b = Blob.fromFile(_fileNametoSHABlob.get(f.getName()));
            if (!b.isEqual(Utils.readContentsAsString(f))) {
                return true;
            }
        }
        return false;
    }
    public boolean containsNewerFile(String f) {
        File fFile = new File(GitUtils.CWD, f);
        return containsNewerFile(fFile);
    }
    public boolean containsNewerFile(Blob b) {
        return _fileNametoSHABlob.containsKey(b);
    }
    public void setfileNameSHASBlob(TreeMap<String, String> k) {
        _fileNametoSHABlob = k;
    }
    public TreeMap<String, String> getfileNameSHASBlob() {
        return _fileNametoSHABlob;
    }
    public void addtofileNameSHASBlob(String fileName, String blobKey) {
        _fileNametoSHABlob.put(fileName, blobKey);
    }
    public String listFilesinCommit() {
        String lstString = "";
        for (String s : _fileNametoSHABlob.keySet()) {
            lstString += s + "\n";
        }
        return lstString;
    }
    /** Returns parent sha key. */
    public String getShaKey() {
        return  _shakey;
    }
    public void setShaKey(String sha) {
        _shakey = sha;
    }
    /** Returns time. */
    public String getTime() {
        return _time;
    }
    /** Returns STRING of log msg. */
    public String  getLogMsg() {
        return _logMsg;
    }
    /** Returns STRING of parent sha. */
    public String getParentSha() {
        return _parentsha;
    }
    /** Returns ARRAYLIST OF parent sha. */
    public String getxtraParents() {
        return _xtraParents.isEmpty() ? null : _xtraParents.get(0);
    }
    public void addxtraParents(String pSHA) {
        _xtraParents.add(pSHA);
    }

    public void setParentSha(String sha) {
        _parentsha = sha;
    }
    /** Adds B to BLOBS. */
    public void addBlob(Blob b) {
        _blobs.add(b);
    }

    /**
     * Returns SHA String of blob associated with FILENAME.
     * @param fileName
     * String of filename
     * @return Blob
     */
    public String getBlob(String fileName) {
        return _fileNametoSHABlob.get(fileName);
    }
    /** Adds CHILDSHA to CHILDSHAS. */
    public void addChild(String childsha) {
        _childShas.add(childsha);
    }
    public ArrayList<Blob> getBlobs() {
        return _blobs;
    }
    public ArrayList<String> getChildSHAs() {
        return _childShas;
    }
    public void makeBranchSplit() {
        _branchSplit = true;
    }
    public boolean isBranchSplit() {
        return _branchSplit;
    }
    /** An ArrayList of blobs. */
    private ArrayList<Blob> _blobs;
    /** table of file name to blob. */
    private TreeMap<String, String> _fileNametoSHABlob;
    /** An ArrayList of Childshas. */
    private ArrayList<String> _childShas;
    /** Time. */
    private String _time;
    /** Logmsg. */
    private String _logMsg;
    /** shakey. */
    private String _shakey;
    /** Parent shakey. */
    private String _parentsha;
    /** Any additonal parents. */
    private ArrayList<String> _xtraParents;
    /** is split. */
    private boolean _branchSplit = false;
}
