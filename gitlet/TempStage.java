package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

public class TempStage implements Serializable {
    public TempStage(String var) {
        this.variant = var;
        table = new TreeMap<>();
    }


    public int size() {
        return table.size();
    }

    public void addTotempStage(String name, String blob) {
        table.put(name, blob);
    }

    /**
     * Rmoves name file from stage.
     * @param name
     * name of file
     */
    public void removeFromtempStage(String name) {
        if (table.containsKey(name)) {
            table.remove(name);
        }
    }


    public TreeMap<String, String> getStage() {
        return table;
    }
    public void clearStage() {
        table = new TreeMap<>();
    }

    public boolean contains(String name, boolean deepContain) {
        return table.containsKey(name)
                && (deepContain ? fileExistsandDiff
                (Utils.join(GitUtils.CWD, name)) : true);
    }
    public boolean fileExistsandDiff(File f) {
        if (!f.exists() || f.isDirectory()) {
            return false;
        } else {
            return Utils.readContentsAsString(f)
                    .equals(Blob.fromFile(table.get(f.getName())));
        }
    }

    public void toFile() {
        File stageFile = Utils.join(GitUtils.THESTAGE, variant);
        Utils.writeObject(stageFile, this);
    }

    public static TempStage fromfile(File f) {
        return Utils.readObject(f, TempStage.class);
    }
    /** type. */
    private String variant;
    /** table of files. */
    private TreeMap<String, String> table;
    /**The add stage. */
    public static final File ADD = Utils.join(GitUtils.THESTAGE, "add");
    /** the remove file name. */
    public static final File REMOVE = Utils.join(GitUtils.THESTAGE, "remove");
}
