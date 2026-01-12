# 📊 Diagram: Bank Branches

## 🔗 Link to Original Paper/Article
[View Full Main Source](https://doi.org/10.1016/j.jss.2021.111138)

## 📝 Short Description
This Model represents a bank managing two different branches in the US and Asia.
Normal Customers can be registered by clerks, meanwhile celebrities get registered by managers.
Clerks are also able to use the system to determine the credit line a customer might be able to get.
Additionally, managers are able to search for customers and move them to a different region

## 🔤 Abbreviations

## 📖 Extensive Description (if possible)
Entry points to the system are either accesses by clerks (`Role.Clerk`) or managers (`Role.Managers`) in one of the two regions the bank operates in (either `Location.USA` or `Location.Asia`)
Customers can be registered by calling the `registerCustomer` function.
Clerks are also able to determine the credit line of customers using `determineCreditLine`.
Lastly, using `findCustomer` clerks can find customers in the backend of the system.

Managers are also able to register customers using `registerCustomer` in order to register celebrities.
Additionally, they can use `moveCustomer` to move customers between the different regions of the bank.

The backend consists of three basic components:
The `CustomerHandling` component is responsible for registering customers and determining customer credit lines.
The `CustomerMovement` component is used when customers are moved between regions.
Lastly, the `CustomerStore` component stores the data of customers in a database.

## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### Origin
        - __USA__: Data originating from the United States 
        - __Asia__: Data originating from Asia
- ### 🏷️ Node Labels:
    - ### Location
        - __USA__: Bank Branch Location in the United States 
        - __Asia__: Bank Branch Location in Asia 
    - ### Role
        - __Clerk__: System managed and accessible by a bank clerk 
        - __Manager__: System managed and accessible to a manager 
    - ### Status 
        - __Regular__: Customer is a regular customer 
        - __Celebrity__: Customer is a celebrity

## ⚠️ Constraints(if any)
- Data of a celebrity is not accessible to a clerk:
    1. `data Status.Celebrity neverFlows vertex Role.Clerk`
- Clerks may only access information of customers in the same region 
    1. `data Origin.$OriginLocation neverFlows vertex Role.Clerk Location.$DestinationLocation where empty intersection($OriginLocation, $DestinationLocation)`

## 🚨 Violations
