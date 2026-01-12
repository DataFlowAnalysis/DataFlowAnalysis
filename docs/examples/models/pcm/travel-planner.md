
# 📊 Model: Travel Planner

## 🔗 Link to Original Paper/Article
[View Source](https://doi.org/10.1109/ICSA.2019.00009)

## 📝 Short Description
It models a user booking a flight with an travel planner application.
Flights are queried using a travel agency that in turn communicates with the airline to determine flights. 
Using the flight information the user books an flight with their credit card details. 

## 🔤 Abbreviations
- `ccd`: Credit Card Details, including Credit Card Number and CCV(Card Code Verification)

## 📖 Extensive Description
This model consists of two different use cases:

The first use case `FlightPlanner` models a flight planner scheduling flights and adding them to an flight database.
The saved flight can be accessed by anyone, denoted by them having every value of the `GrantedRoles` label.

The second use case `User` models a user booking a flight using their credit card.
First, they store their credit card details in a local database using `store ccd` of the local `CreditCardDataDB`.
The stored credit card details should only be accessible to the user.
Then, the use the `FlightQuery` interface to find flights using a `query` and the `findFlights` method.
The data `query` should be accessible to anyone, as it does not hold any confidential data. 
Using the stored credit card details and the found flights a flight is booked using the `FlightBooking` interface that provides the `book` method. 
Both the returned flights and credit card details are passed with their stored access permissions.
Finally, the booking confirmation is returned to the user.

## 🏷️ Label Description
- ### 🗂️ Data Labels:
    - ### GrantedRoles 
        - __User__: Users are allowed to access to the data
        - __Airline__: The airline is allowed to access the data
- ### 🏷️ Node Labels:
    - ### AssignedRoles
        - __User__: The current system belongs or is directly accessible to to the User
        - __Airline__: The current system belongs or is directly accessible to to the airline

## ⚠️ Constraints
### Access Control Mechanism
Data that should only be accessed by a certain role, denoted by `GrantedRoles`, should only be accessible at nodes with the correct permissions.
The permissions of a vertex are denoted by `AssignedRoles`.
If a vertex does not have an `AssignedRole` or data does not have an `GrantedRole` the access is presumed to be allowed.
- `data GrantedRoles.$grantedRoles neverFlows vertex AssignedRoles.$assignedRoles where present $grantedRoles present $assignedRoles empty intersection($grantedRoles,$assignedRoles)`

## 🚨 Violations
### Access Control Mechanism
The vertices for the method/SEFF `book` and the contained vertex `return confirmation` violate the Access Control Rule as they receive the users credit card details without being cleared to hold them
