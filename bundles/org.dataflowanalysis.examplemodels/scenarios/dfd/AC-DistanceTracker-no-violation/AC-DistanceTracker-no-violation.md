# 📊 Model: (AccessControl-DistanceTracker-no-violation)

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
- **AccessRights**: These are classified as either `User`, `TrackingService`, and `DistanceTracker`, depending on which area the data flow is allowed to flow through.
### 🏷️ Node Labels:
- **Roles**: These are the same as the data labels. `User`, `TrackingService` and `DistanceTracker` are the three parts of the model. Each node is part of exactly one of these.

## ⚠️ Constraint
### DistanceTrackerSecurity
This constraint ensures that no data flow passes a node it does not have the corresponding label (and access right) for.
- `data !AccessRights.DistanceTracker neverFlows vertex Roles.DistanceTracker`
