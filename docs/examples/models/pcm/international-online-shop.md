# 📊 Diagram: Branching Online Shop

## 🔗 Link to Original Paper/Article

## 📝 Short Description
This model describes a small online shop that is able to present the inventory to a user and allows the user to buy items.
The shops' backend is run on premise, but data is stored in a cloud environment.

## 🔤 Abbreviations

## 📖 Extensive Description (if possible)
Users interacting with the system first view the inventory of the online store using `ViewEntryLevelSystemCall`.
This returns the inventory of the online shop that is stored in the cloud database.
Then users are able to order specific items using the `BuyEntryLevelSystemCall`.
During this process `userData` is transmitted.
Internally data is processed on premise first, but in order for save data for the order, data is sent to the database in the cloud environment


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
User data collected for the purchasing process is stored on a cloud server that is not deployed within the European Union, therefore causing a violation of the above constraint.
