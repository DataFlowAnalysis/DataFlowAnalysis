# 📊 PCM Model: CoCarNextGen

## 🔗 Link to Original Paper/Article

[View Audi Fakten Source](<https://www.audi-mediacenter.com/de/der-audi-a6-bis-2025-das-multitalent-in-der-business-class-10240/die-fakten-10242>)

[View Audi Vernetzung Source](<https://www.audi-mediacenter.com/de/der-neue-audi-a6-das-multitalent-in-der-business-class-10240/digitalisierung-10243>)

[View Audi Bedienung und Anzeige, Infotainment](<https://www.audi-mediacenter.com/de/audi-technik-lexikon-7180/bedienung-und-anzeige-infotainment-16948>)


## 📝 Short Description
This model represents a high-level architecture of an **Audi A6 vehicle**, focusing on sensor-based perception, centralized driver assistance, infotainment, and cloud connectivity. Environmental data is collected by multiple sensors (radar, cameras, ultrasonic) and fused centrally by the **zFAS** unit to create a consistent world model for driver assistance functions. These functions are processed by the **Central Comfort Control Unit** and translated into vehicle actuation commands via the **DriveByWire Control Unit**. In parallel, the **MIB 2+** infotainment platform provides human–machine interaction, navigation, voice control, smartphone integration, and **Car-to-X** communication. Connectivity to external cloud services, including **HERE** servers, **Google** navigation services, **Audi Car-to-X** backends, and online speech recognition, is realized through a **Gateway Control Unit**, enabling advanced navigation, traffic awareness, and connected services while maintaining a clear separation between in-vehicle systems and external backends.

## 🔤 Abbreviations
- Car2X: Exchange of information between a vehicle and external entities, for example cloud backends. 
- HERE: HERE Technologies is a company that many car manufacturers use for maps, navigations, and traffic intelligence
- zFAS: zentrales Fahrerassistenzsteuergerät

## 📖 Extensive Description (if possible)
For more information or more technical knowledge, please refer to the links above, as they were the basis of knowledge for the construction of this model.

**1. Sensor & Perception Domain**
The Sensors block represents the full set of environment-sensing devices described for the Audi A6, which includes **Radar sensors** (long- and mid-range), front and surround-view **cameras** and **ultrasonic sensors**. These sensors provide raw environment data such as object distance and velocity, lane markings or obstacles.

**2. zFAS - Central Driver Assistance Processing**
The zFAS component is the central driver assistance control unit used by Audi. The previously mentioned sensor data is transmitted a FlexRay bus to the **sensor fusion layer**. Thi fusion produces a consistent world model, which is required for higher-level assistance functions.

**3.Central Comfort Control Unit**
This component hosts logical driver assistance functions, which rely on **zFas** outputs, which in turn are transmitted through the **CAN bus** which act as the central backbone between the **zFAS, Infotainment systems, Vehicle CUs, Gateways CUs**. The driver assistance functions represented in the model are  the **adaptive drive assist**, **efficiency assistant** and the **junction assistant**.

**4. DriveByWire Control Unit**
The **DriveByWireCU** represents the interface to vehicle actuation (steering, acceleration, braking) and it receives control commands from the assistance functions.

**5. MIB 2+ (Modular Infotainment Baukasten)**
The **MIB 2+** unit is Audi´s infotainment platform and central HMI controller. The basic component is the **MMITouchResponseDisplay** which in turn connects with the different internal components:
- **1. MMI Search**: This component allows the driver to do a normal **Google** search (for restaurants or the price of gas nearby) or a **HERE location-based**
search.
- **2. Voice Control**: This component allows natural language interaction for online commands through the **OnlineSpeechRecognitionBackend**.
- **3. Self-learning Navigation System**: This component suggests routes based on the learned driver behavior and habits. The route and traffic calculations are done by the **HERE servers**. There is also the possibility to use the Google Earth navigation system, which is offered through the **MMINavigationPlus** component. This component connects to the Google Earth Navigation backend for achieving this purpose.
- **4. Car2xModule**: This component handles vehicle-to-backend communication for things such as hazard warning (possibly received from the cameras/sensors) or traffic information (based on the gps location).
- **5. Smartphone interface**: The smartphone interfaceenables app integration and vehicle status access. The owner´s smartphone, running the **myAudi App**, enables **accessing calendar information**, **finding the car or navigation functionality** and **remote comfort features**. This last features are done through the **SmartPhoneConnectability** component, which is able to send the signals to the **HeatedSeatCU**, **CentralLockingCU** and **AuxiliaryHeatingCU**.

**6. Gateway Control Unit**: The Gateway Control Unit connects **in-vehicle networks** with **external networks** such as the HERE or Google Servers, AudiCar 2 Backend and Online Speach Recognition Backend. 

**7. HERE Servers**: The model also represents the functionality provided by the **HERE Servers** as the **HERETrafficLoadCalculation**, **HERERouteCalculation** and **HERELocationBasedSearch** components.

**8. Google Navigation backend**: The functionality provided by the **google navigation backend** is also shown in the model by the **GoogleSearch** and **GoogleEarthNavigationBackend** components.
 
## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### DataType:
        - __VehicleIdentifier__: The data is the identifier of the vehicle
        - __Car2X__: 
        - __GPSLocation__: The data is the GPSLocation of the car
        - __Navigation__: The data is the navgation data of the route
        - __ExternalAuth__: The data is an external authentication
        - __Traffic__: The data is the traffic data
        - __DriveControlInstruction__: The data is a drive control instruction

    - ### Granularity:
        - __Detailed__: The granularity of the data is detailed
        - __Aggregated__: The granularity of the data is aggregated

    - ### Status:
        - __GatewayChecked__: The status of the gateway is checked

- ### 🏷️ Node Labels:
    - ### Trigger:
        - __Automatic__: This node is triggered automatically
        - __Manual__: This node is a triggered manually

    - ### Location:
        - __Onboard__: This node resides on board
        - __AudiCloud__: This node resides in the Audi cloud
        - __Smartphone__: This node resides in the smartphone
        - __HEREBackend__: This node resides in the HERE backend
        - __GoogleBackend__: This node resides in the Google backend
    
        
## ⚠️ Constraints
- There are no constraints for this model

## 🚨 Violations
- __None__










