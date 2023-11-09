# Gitlet Design Document
author: Kevin Chow


## 1. Classes and Data Structures
HashMap: Accesses commits based on SHA hashkey, could be a HashMap of Hashmaps for parent child references and branching

Commit Class: Contains, time, parent reference, children references, log message, and list of pointers to Blobs. Also serializable

Blob: A serializable class that contains a file content of a pertaining file, should also contains a SHA variable for denoting versions for differentiation

Git Utils: a set of commands to be used in Main for all gitlet commands

tempStage: a treemap of files and blobs


