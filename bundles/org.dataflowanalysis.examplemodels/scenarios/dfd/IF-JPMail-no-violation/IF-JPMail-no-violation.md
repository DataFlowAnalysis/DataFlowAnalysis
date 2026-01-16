# 📊 Model: (InformationFlow-JPMail-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)

## 📝 Short Description

This model describes a mail exchange via public mail server.

## 🔤 Abbreviations

- **Policy DS**: Policy DataStore
- **Bob DS**: Bob DataStore
- **SMTP**: Simple Mail Transfer Protocol
- **POP3**: Post Office Protocol Version 3

## 📖 Extensive Description

The case is about __Alice__ who sends a mail to __Bob__ via a public mail server. Initially, Alice provides an *email_body_and_header*, which is split into header and body. The body is encrypted (__encrypt__) with the public key of Bob (*BobPubKey*). Header and encrypted body are sent via mail servers to __Bob__. __Bob__ decrypts the body in the `Decrypt` zone and reads the mail. 

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: This label denotes the zones a data flow may access. There are levels `High` and `Low`.
- **LevelBeforeEncryption**: This label stores the security classification of a data flow before it is encrypted with the `declassify` node. This can be `High` or `Low`.
### 🏷️ Node Labels:
- **Zone**: There are two `Zones`: `Trust` and `Attack`. A node can be in either or none of these `Zones`.

## ⚠️ Constraint
### SafetyConstraint
The fundamental requirement is that system parts or actors in the attack zone must not have access to data classified High:
- `SafetyConstraint: Level.High neverFlows vertex Zone.Attack`
