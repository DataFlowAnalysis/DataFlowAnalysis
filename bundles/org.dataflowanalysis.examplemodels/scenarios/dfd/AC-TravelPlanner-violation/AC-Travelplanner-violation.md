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
The saved flight can be accessed by anyone, denoted by them having every value of the `Levels` label.

The second use case `User` models a user booking a flight using their credit card.
First, they store their credit card details in a local database using `store ccd` of the local `CreditCardDataDB`.
The stored credit card details should only be accessible to the user.
Then, the use the `FlightQuery` interface to find flights using a `query` and the `findFlights` method.
The data `query` should be accessible to anyone, as it does not hold any confidential data. 
Using the stored credit card details and the found flights a flight is booked using the `FlightBooking` interface that provides the `book` method. 
Both the returned flights and credit card details are passed with their stored access permissions.
Finally, the booking confirmation is returned to the user.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Levels**: Describes the roles that can access the given data. It can be either `User`, `UserAirline` or `UserAirlineAgency`.
### 🏷️ Node Labels:
- **Roles**: Describes the role of an system component. Can be either `User`,  `Airline` or `Agency`.

## ⚠️ Constraints
### SafeCCD
Data that should only be accessed by a certain role, denoted by `GrantedRoles`, should only be accessible at nodes with the correct permissions.
The permissions of a vertex are denoted by `AssignedRoles`.
- `data Levels.User neverFlows vertex Role.Airline`

## 🚨 Violations
The error introduced in the case is that credit card data (__ccd__) can bypass the declassification process, which implies a missing access permission. 
