# 📊 Diagram: Branching Online Shop

## 🔗 Link to Original Paper/Article
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/pcm/BranchingOnlineShop)

## 📝 Short Description
This model describes a small online shop that is able to present the inventory to a user and allows the user to buy items.
The shops' backend is run on two different systems one of which might be selected for a certain user.

## 🔤 Abbreviations

## 📖 Extensive Description (if possible)
Users interacting with the system first view the inventory of the online store using `ViewEntryLevelSystemCall`.
This returns the inventory of the online shop that is stored in an internal database.
Then users are able to order specific items using the `BuyEntryLevelSystemCall`.
During this process `userData` is transmitted.
Internally requests are processed on either a system that is deployed within or outside the EU.


## 🏷️ Label description
- ### 🗂️ Data Labels:
    - ### DataSensitivity
        - __Personal__: Personal data requiring special treatment
        - __Public__: Data that is publicly accessible
- ### 🏷️ Node Labels:
    - ### ServerLocation
        - __EU__: Server is located within the European Union
        - __NonEU__: Server is located outside the European Union

## ⚠️ Constraints
- Personal data of users must be processed within the European Union
    1. `data DataSensitivity.Personal neverFlows vertex ServerLocation.NonEU`

## 🚨 Violations
In the case the request to purchase an item is processed by the server outside the European Union, the constraint is violated.
