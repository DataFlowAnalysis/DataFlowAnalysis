# 📊 Model: (AccessControl-ABAC-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://www.scitepress.org/Link.aspx?doi=10.5220/0010515300260037)

## 📝 Short Description

The case is about a banking system deployed in the USA and Asia. Clerks can register customers, look them up and determine a credit line for them. Managers can do everything Clerks can do but can also register celebrities or move customers between regions. 

## 🔤 Abbreviations

-

## 📖 Extensive Description
(In this discription you may use/are encouraged to use __Vertex Names__ and *Variable/Edge Names* so that the reader may follow the flow)

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
The introduced flow *celebrity_customer_details* lets `Celebrity` data flows into normal __Customer Storage__, which Clerks can access.
