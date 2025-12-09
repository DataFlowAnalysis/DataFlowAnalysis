# 📊 Model: (AccessControl-MAC-violation)

## 🔗 Link to Original Paper/Article
Roughtly based on [View Source](https://openlibrary.org/books/OL17011110M/Securing_information_and_communications_systems)

## 📝 Short Description

The case is about an airspace monitoring system for civil and military planes.

## 🔤 Abbreviations

-

## 📖 Extensive Description

There are three types of users. A `Clerk` creates and stores weather reports. A `Flight Controller` registers civil airplanes, finds their positions and determines new routes based on other plane positions and the weather reports of the clerk. The `Military Flight Controller` performs the same tasks for military airplanes and also considers the positions of civil airplanes.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **ClassificationLevel**: There are three classification levels for a data flow. These are the same as the clearance levels for nodes. A data flow may not traverse a node it for which it does not have the needed classification level.
### 🏷️ Node Labels:
- **ClearanceLevel**: There are three clearance levels. These are, in descending heirarchy: `Secret`, `Classified`, `Unclassified`. Each following level is a subset of the preceding level(s).

## ⚠️ Constraints
### SecretConstraint
This constraint ensures that data designated `Secret` does not flow to nodes with clearance level `Classified`:
- `data ClassificationLevel.Secret neverFlows vertex ClearanceLevel.Classified`

### ClassifiedConstraint
This constraint ensures that data designated `Classified` does not flow to nodes with clearance level `Unclassified`:
- `data ClassificationLevel.Classified neverFlows vertex ClearanceLevel.Unclassified`

## 🚨 Violations
The error introduced in the case is that the flight controller uses the positions of military planes to determine a new route. This additional flow is called __plane_positions__
