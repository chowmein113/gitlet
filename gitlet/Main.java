package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Kevin Chow
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  init  inits a gitlet repo
     *  add (filename) adds filename to be added to next gitlet commit
     *  rm (filename) removes filename from next commit
     *  commit (text) commits with message and saves blobs of all files staged for commit
     *  checkout (branch) can checkout to different branch in gitlet
     *  log list all recent git commit history starting from head
     *  global-log shows all git commits since beginning of repo
     *  list-commit (commit hash) lists all files in given commit or recent by default
     *  find (message) shows all commits with given message
     *  status shows all added, removed, and untracked files currently
     *  branch (name) creates a new branch from HEAD commit with NAME
     *  rm-branch (name) removes the branch with NAME
     *  reset resets gitlet repo
     *  merge (branch Name) merges current branch with given branch NAME*/
    public static void main(String... args) {
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        String frontArg = args[0];
        switch (frontArg) {
        case "init":
            validArg(1, args.length, "==");
            init();
            break;
        case "add":
            validArg(2, args.length, "==");
            add(args[1]);
            break;
        case "rm":
            validArg(2, args.length, "==");
            rm(args[1]);
            break;
        case "commit":
            commit(args);
            break;
        case "checkout":
            checkout(args);
            break;
        case "log":
            validArg(1, args.length, "==");
            System.out.println(GitUtils.getLog());
            break;
        case "global-log":
            globalLog(args);
            break;
        case "list-commit":
            listCommit(args);
            break;
        case "find":
            find(args);
            break;
        case "status":
            status(args);
            break;
        case "branch":
            branch(args);
            break;
        case "rm-branch":
            rmBranch(args);
            break;
        case "reset":
            resetCommit(args);
            break;
        case "merge":
            mergeCommit(args);
            break;
        default:
            ec(args);
        }
        return;
    }
    public static void init() {
        GitUtils yGit = new GitUtils();
        yGit.init();
        yGit.toFile();
    }
    public static void add(String args) {
        GitUtils gGit = GitUtils.fromFile();
        gGit.setPersistence();
        gGit.add(args);
        gGit.toFile();
    }
    public static void ec(String[] args) {
        String frontArg = args[0];
        switch (frontArg) {
        case "add-remote":
            validArg(3, args.length, "==");
            addRemote(args);
            break;
        case "push":
            validArg(3, args.length, "==");
            push(args);
            break;
        case "fetch":
            validArg(3, args.length, "==");
            GitUtils.fetch(args[1], args[2]);
            break;
        default:
            Utils.message("No command with that name exists.");
            break;
        }
    }
    public static void addRemote(String[] args) {
        validArg(3, args.length, "==");
        GitUtils gGit = GitUtils.fromFile();
        GitUtils.setPersistence();
        gGit.addRemote(args[1], args[2]);
        gGit.toFile();
    }
    public static void push(String[] args) {
        validArg(3, args.length, "==");
        GitUtils.setPersistence();
        GitUtils.push(args[1], args[2]);
    }
    public static void rm(String args) {
        GitUtils hGit = GitUtils.fromFile();
        GitUtils.setPersistence();
        hGit.remove(args);
        hGit.toFile();
    }
    public static void commit(String[] args) {
        validArg(2, args.length, "==");
        GitUtils dGit = GitUtils.fromFile();
        dGit.setPersistence();
        dGit.commit(args[1], false);
        dGit.toFile();
    }
    public static void checkout(String[] args) {
        GitUtils.setPersistence();
        validArg(2, args.length, ">=");
        validArg(4, args.length, "<=");
        if (args.length == 3) {
            GitUtils.gitCheckOut(args[2]);
        } else if (args.length == 4
                && args[2].equals("--")) {
            GitUtils.gitCheckOut(args[1], args[3]);
        } else if (args.length == 2) {
            GitUtils bGit = GitUtils.fromFile();
            bGit.gitCheckoutBranch(args[1]);
            bGit.toFile();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    public static void listCommit(String[] args) {
        validArg(3, args.length, "<");
        GitUtils lGit = GitUtils.fromFile();
        GitUtils.setPersistence();
        if (args.length == 1) {
            System.out.println(lGit.getHeadCommit().listFilesinCommit());
        } else {
            Commit c = Commit.fromFile(args[1]);
            System.out.println(c.listFilesinCommit());
        }
    }
    public static void status(String[] args) {
        GitUtils.setPersistence();
        validArg(1, args.length, "==");
        GitUtils uGit  = GitUtils.fromFile();
        System.out.println(uGit.statusGitLet());
        uGit.toFile();
    }
    public static void find(String[] args) {
        validArg(2, args.length, "==");
        GitUtils.setPersistence();
        System.out.println(GitUtils.findCommits(args[1]));
    }
    public static void branch(String[] args) {
        validArg(2, args.length, "==");
        GitUtils.setPersistence();
        GitUtils bGit = GitUtils.fromFile();
        bGit.setBranch(args[1]);
        bGit.toFile();
    }
    public static void rmBranch(String[] args) {
        validArg(2, args.length, "==");
        GitUtils.setPersistence();
        GitUtils rGit = GitUtils.fromFile();
        rGit.rmBranch(args[1]);
        rGit.toFile();
    }
    public static void resetCommit(String[] args) {
        GitUtils.setPersistence();
        validArg(2, args.length, "==");
        GitUtils rstGit = GitUtils.fromFile();
        GitUtils.setPersistence();
        rstGit.gitReset(args[1]);
        rstGit.toFile();
    }
    public static void mergeCommit(String[] args) {
        validArg(2, args.length, "==");
        GitUtils mGit = GitUtils.fromFile();
        GitUtils.setPersistence();
        mGit.mergeBranch(args[1]);
        mGit.toFile();
    }
    public static void globalLog(String[] args) {
        GitUtils.setPersistence();
        validArg(1, args.length, "==");
        System.out.println(GitUtils.getGlobalLog());
    }
    public static void validArg(int corLen, int actLen,
                                String errorMSG, String opSign) {
        switch (opSign) {
        case "<":
            if (actLen < corLen) {
                return;
            }
        case "<=":
            if (actLen <= corLen) {
                return;
            }
        case "==":
            if (actLen == corLen) {
                return;
            }
        case "!=":
            if (actLen != corLen) {
                return;
            }
        case ">":
            if (actLen > corLen) {
                return;
            }
        case ">=":
            if (actLen >= corLen) {
                return;
            }
        default:
            break;
        }
        Utils.message(errorMSG);
        System.exit(0);
    }
    public static void validArg(int corLen, int actLen,
                                 String opSign) {
        validArg(corLen, actLen,
                "Incorrect operands.", opSign);
    }

}
