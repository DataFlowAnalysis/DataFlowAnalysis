# 📊 Model: (AccessControl-DAC-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://openlibrary.org/books/OL17011110M/Securing_information_and_communications_systems)

## 📝 Short Description
This case is about a file system for pictures in a family.

## 🔤 Abbreviations

-

## 📖 Extensive Description
There are three family users that use a store `Family Pictures`. The `Mother` and `Dad` can `Add` and `Read Pictures`. The aunt can only `Read Pictures`. An `Indexing Bot` might discover the file sharing system but must not access it. 

## 🏷️ Label Description
### 🗂️ Data Labels:
- **TraversedNodes**: Each node visited by a certain data flow adds its own tag. This can be `addPicture`, `pictureStorage`, `readPicture`, `mother`, `dad`, `aunt` and `indexingBot`.
### 🏷️ Node Labels:
- **Identity**: This node describes a certain user. There are `Mother`, `Dad`, `Aunt` and `IndexingBot`.
- **Owner**: `Mother` is the owner of the data store.
- **Read**: Designates the right to read from the data store. `Mother`, `Dad`, `Aunt` may read from it.

## ⚠️ Constraints
### Isolation
The IndexingBot is not allowed to read or access `Family Pictures` in any way.
- `data !Read.IndexingBot neverFlows vertex Identity.IndexingBot`
