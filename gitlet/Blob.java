package gitlet;


import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    public Blob(File content) {
        if (!content.isFile()) {
            throw new RuntimeException("Not a file");
        }
        _blobFile = Utils.readContentsAsString(content);
        _version = 0;
        _name = content.getName();
        _shakey = Utils.sha1(_name, _blobFile);

    }
    public void toFile() {
        File self = new File(GitUtils.BLOBS, _shakey);

        Utils.writeObject(self, this);
    }
    public static Blob fromFile(String sha1) {
        return Utils.readObject(new File(GitUtils.BLOBS, sha1), Blob.class);
    }
    public boolean isEqual(String contentasString) {
        setPersistence();
        return _blobFile.equals(contentasString);
    }
    public String contentAsString() {
        setPersistence();
        return _blobFile;
    }
    public String getFileContents() {
        return _blobFile;
    }
    public void setFileContents(String f) {
        _blobFile = f;
    }
    public int getVersion() {
        return  _version;
    }
    public void upVersion() {
        _version += 1;
    }
    public void setPersistence() {
        File f = new File(GitUtils.BLOBS, _shakey);
        if (!f.exists()) {
            System.exit(0);
        }
    }
    public String getShaKey() {
        return _shakey;
    }
    public String getName() {
        return _name;
    }
    /** SHA Key. */
    private String _shakey;
    /** Name. */
    private String _name;
    /** version. */
    private int _version;
    /** Blob file contents. */
    private String _blobFile;
}
