# 📊 Diagram: Volkswagen-Cariad Automotive System

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://www.spiegel.de/netzwelt/web/volkswagen-konzern-datenleck-wir-wissen-wo-dein-auto-steht-a-e12d33d0-97bc-493c-96d1-aa5892861027>)

## 📝 Description
This diagram illustrates the issues associated with the Volkswagen (and Cariad) scandal and data leakage affecting their automobile fleet. It shows how information flows between the mobile app, internal servers (and storage), and the in-vehicle system.

The first (leftmost) section of the diagram presents a simplified data flow from the mobile app, including functionalities such as login, retrieving the car's location, locking the car, and more. This part connects to Cariad’s main Spring application, which is primarily responsible for storing all vehicle-related data.

Finally, on the right side of the diagram, we see the in-car system, composed of sensors, actuators, controllers, control modules, and communication components.

## ⚠️ Violated constraints (If Any)
The following violations relate to the issues revealed in the scandal referenced in the previous article (among others). These have been added to the diagram to highlight what went wrong. 

- The following violated constraints show that not only was the **heapdump** endpoint relatively easy to access, but it also exposed confidential and highly sensitive information (e.g., tokens for accessing databases):

    1. `data DataSensitivity.confidential neverFlows vertex EndpointConfiguration.public`
    2. `data RequestSensitivity.confidential neverFlows vertex EndpointConfiguration.public`
    3. `data DataBaseToken.AWSToken,DataBaseToken.AzureToken neverFlows vertex EndpointConfiguration.public`

- The next set of violations corresponds to another issue in the scandal: the presence of non-anonymized and unencrypted data in the AWS bucket:

    1. `data DataSensitivity.non_anonymized neverFlows vertex DataBaseType.AWSBucket,DataBaseType.AzureDataLake`
    2. `data DataEncryption.nonEncrypted neverFlows vertex DataBaseType.AWSBucket,DataBaseType.AzureDataLake`





