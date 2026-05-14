# 📊 Diagram: Mobility as a Service (MaaS) System

## 🔗 Link to Original Paper/Article
[View Full Main Source](https://link.springer.com/chapter/10.1007/978-3-658-39438-7_32)

## 📝 Short Description
The MaaS ticketing system is a distributed system in which customer manage and operate a vehicle which generates data that needs to be collected, managed, analyzed and sufficiently secured.

## 🔤 Abbreviations 
- MaaS: Mobility as a Service
- STS: Short term secret
- LTS: Long term secret

## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### DataGranularity
        - __FineGranular__: Data containing fine granular information
        - __Aggregated__: Data aggregated from multiple data sources
    - ### DataType
        - __LTS__: Data is a long term secret
        - __STS__: Data is a short term secret
        - __TripData__: Data related to trip information
        - __VehicleInformation__: Data concerned with the vehicle
        - __InvoiceValue__: Invoice amount data
        - __InspectionResult__: Result data of an inspection 
        - __LoginData__: Data used for login 
        - __ContactInformation__: Contact information of a customer 
        - __CustomerPseudonym__: Pseudonym of a customer
- ### 🏷️ Node Labels:
    - ### Role 
        - __AnalysisStaff__: Action performed by Analysis staff
        - __Inspector__: Action performed by an inspector 
        - __Customer__: Actions performed by customers
        - __Vehicle__: Actions performed by the vehicle
        - __SupportStaff__: Actions performed by the performed staff
        - __BillingStaff__: Actions performed by the billing staff 
        - __Administrators__: Actions performed by system administrators
        - __MoblityProvider__: Actions performed by the mobility provider
    - ### StateMachineAccessType
        - __Read__: Read-only operations accesses only 
        - __Write__: Write and read operations allowed

## ⚠️ Constraints
1. Fine granular trip, STS and LTS data never leaves the customer
    - `CNFR11: data DataGranularity.FineGranular DataType.TripData neverFlows vertex !Role.Customer`
    - `CNFR12: data DataGranularity.FineGranular DataType.STS neverFlows vertex !Role.Customer`
    - `CNFR13: data DataGranularity.FineGranular DataType.LTS neverFlows vertex !Role.Customer`
2. Trip data and vehicle data from the customer never reaches an inspector
    - `CNFR2: data DataType.TripData Role.Customer DataType.VehicleInformation Role.Vehicle neverFlows vertex Role.Inspector`
3. Fine granular trip, STS and LTS data from the customer never reaches analysis staff or an inspector
    - `CNFR31: data DataType.TripData Role.Customer DataGranularity.FineGranular neverFlows vertex Role.AnalysisStaff,Role.Inspector`
    - `CNFR32: data DataType.STS Role.Customer DataGranularity.FineGranular neverFlows vertex Role.AnalysisStaff,Role.Inspector`
    - `CNFR33: data DataType.LTS Role.Customer DataGranularity.FineGranular neverFlows vertex Role.AnalysisStaff,Role.Inspector`
4. Fine granular trip data from the customer never reaches analysis staff
    - `CNFR4: data Role.Customer DataType.TripData DataGranularity.FineGranular neverFlows vertex Role.AnalysisStaff`
5. Interfaces written to never flow to interfaces used in read-only
    - `INFR1: data WriteAction.$Write neverFlows vertex StateMachineAccessType.Read where present $Write`

## 🚨 Violations
