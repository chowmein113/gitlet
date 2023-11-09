# Gitlet
author: Kevin Chow

##USAGE:
run Main.java in terminal to use CLI:
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