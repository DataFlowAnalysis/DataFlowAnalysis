
# 📊 Model: (InformationFlow-FriendMap-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)

## 📝 Short Description

The case is about visualizing the locations of friends in a map and posting it on a social network.

## 🔤 Abbreviations

- `SNAPP`: Social Network App
- `Local DS`: Local DataStore

## 📖 Extensive Description

The user Alice reads friend locations from a SNAPP and stores them locally. Afterwards, the system loads code from a map provider and the friend map app. The code uses Google Maps to create the map. Alice posts this map on the social network.

There are two hierarchical security levels. The lowest level is Low. The highest level High dominates the previous level. The system parts are placed in zones. Google and the map provider and service are placed in the zone Attack. All remaining system parts are in the zone Trust. Consequently, data is characterized by the classification characteristic type. Nodes are characterized by the zone characteristic type. Additionally, data can be classified by a characteristic type describing the contained classification, which is used to record the classification of encrypted content. 

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: There are two types of levels: `High` and `Low`.
### 🏷️ Node Labels:
- **Actor**: There are four actors in this model: `Google`, `Alice`, SocialNetwork` and `CreateMap`. A node is not nessecarily part of a zone.
- **Zone**: There are two zones in this model: `Attack` and `Trust`. A node always part of one of these zones.

## ⚠️ Constraints
### Safety
System parts or actors in the attack zone must not have access to data classified `High`:
- `data Level.High neverFlows vertex Zone.Attack`

