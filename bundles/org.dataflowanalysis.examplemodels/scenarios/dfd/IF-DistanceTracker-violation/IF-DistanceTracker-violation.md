# 📊 Model: (InformationFlow-DistanceTracker-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)

## 📝 Short Description

The case is about a user that wants to track running statistics by an external service.

## 🔤 Abbreviations

-

## 📖 Extensive Description

The __User__ sends his/her *location* to a *Distance Tracker* service that stores locations in the __Location Store__ and derives the run distance. This happens with the *consent* of the __User__ Afterwards, this service transmits the *distance* to a __Tracking Service__.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **ClassificationLevel**: These are classified as either `User`, `UserTrackingService`, and `OnlyDistance`, depending on which area the data flow is allowed to flow through.
### 🏷️ Node Labels:
- **ClearanceLevel**: These are the same as the data labels. `User`, `UserTrackingService`, and `OnlyDistance` denote which **ClassificationLevel** is allowed to flow through a given node.

## ⚠️ Constraint
### DistanceConstraint
This constraint ensures that no data flow passes a node it does not have the corresponding **ClassificationLevel** for.
- `data ClassificationLevel.UserTrackingService neverFlows vertex ClearanceLevel.OnlyDistance`

## 🚨 Violations
The error introduced in the case is that the calculated distance can bypass the declassification process, which implies a higher classification level.
