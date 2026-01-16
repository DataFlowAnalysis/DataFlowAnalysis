# 📊 Model: (AccessControl-ABAC-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://www.scitepress.org/Link.aspx?doi=10.5220/0010515300260037)

## 📝 Short Description

The case is about a banking system deployed in the USA and Asia. Clerks can register customers, look them up and determine a credit line for them. Managers can do everything Clerks can do but can also register celebrities or move customers between regions. 

## 🔤 Abbreviations

-

## 📖 Extensive Description

The __Clerk US__ can register regular customers by *customer_details*. These customers are stored in the __Customer Storage__ and can be found by *customer_name*. 
The __Manager__ located in the US can __Register Celebrity__ into a seperate __Celebrity Customer Storage__. Neither Clerk can access this data. 
The __Manager__ can also change the customer location from `USA` to `Asia` via the node __Move Customer__. The *customer* is fetched by *customer_name* and stored into a __Customer Storage__ with the changed **DataOrigin** label. Now, the __Clerk Asia__ is able to find this *customer*.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **DataOrigin**: Shows the originating region of a customer. This can be either `USA` or `Asia`.
- **DataStatus**: This differentiates between `Customer` and `Celebrity`.
### 🏷️ Node Labels:
- **NodeRole**: This label designates an actor as `Clerk` or `Manager`.
- **NodeLocation**: Shows the location of a clerk or manager in the banking system. This can be either `USA` or `Asia`.

## ⚠️ Constraints
### Security

Clerks are not supposed to be able to access Celebrity customer data.
`- Security: data DataStatus.Celebrity neverFlows vertex NodeRole.Clerk`

## 🚨 Violations

None.
