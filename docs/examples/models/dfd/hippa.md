# 📊 Diagram: Health Insurance Portability and Accountability Act (HIPAA) Case-Study

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/HipaaCaseStudy/Hipaa_CaseStudy.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://www.research-collection.ethz.ch/handle/20.500.11850/641986>)


## 📝 Short Description
This diagram illustrates the data flow between the components/functionalities of the HIPAA application from the study.  The diagram is based on the code from the study, in which the nodes show different functionalities and labels show decorators that the functions had. This application contains only the functionality of viewing index records.The diagram also shows the added components for privacy policies and consents implemented in the app, which was one of the main focus of the paper.

## 🔤 Abbreviations
- __None__

## 📖 Extensive Description (if possible)
The left side of the diagram shows the user entering its _credentials_ (_email_, _username_, and _password_) into the __Flask App__. From here on there are 2 possible flows of data, which depend on the user being already registered or not. In both flows the _credentials_ flow to the __Login Flask__ or __Register Flask__ nodes. Afterwards in the login case the _credentials_ flow to the __Check Credentials__ node where they are checked and consequentially to the __SQLite DB__ node. In the case of the registration flow the _credentials_ flow to the __After Register Hook__ and finally to the __SQLite DB__. From this node the *user_consents* and *index_records* flow to the __Current User__ node (which represents the user currently using the app). From this point on there are some information flows. The first one send the *user_consents* data to the __Policy__ node which forwards it to the __View Consents Template__ where the user its able to see its current consent settings. In the second one *user_consents* flow to the __Remove Consents__ node, which then forwards the *actualize_consents* to the __Save To DB__ node. The third one is again the forwarding of the *user_consents* to the __Add Consents__ node, which forwards the *new_consents* to the __Save To DB__ node. Lastly there is the flow of *index_records* to the __View Index Record__ node, which then forwards this to the __View Index Template__ node so that the user can see the records. This last flow is the only functionality, outside of the consent policies, which is implemented in the source code of the paper.

## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### DataType:
        - __ConsentSettings__: List of consented actions/purposes of the user
        - __IndexRecords__: List of index records from a patient
        - __NewConsentSettings__: List of updated actions/purposes of the user after adding a new consent
    
    - ### Credentials:
        - __Email__: Email of the user
        - __Password__: Password of the user
        - __Username__: User name of the user
        
- ### 🏷️ Node Labels:
    - ### Decorator:
        - __Secure__: Decorator that defines that given node (or function in the code) as secure
        - __UserRegistered__: Decorator that defines that for the given node (or function in the code) the user has to be registered
        - __LoginRequired__: Decorator that defines that for the given node (or function in the code) the user has to be logged in
    - ### ConsentedPurposes
        - __ViewRecords__: The purpose of viewing the records is consented

## ⚠️ Constraints
- Sensitive data from the user such as ConsentSettings (old or new) and IndexRecords can not flow to an insecure node:

    1. __secure__: `data DataType.ConsentSettings,DataType.NewConsentSettings,DataType.IndexRecords neverFlows vertex !Decorator.Secure`

- Index records data neverflows to a node which does not have the purpose/settings of ViewRecords allowed:

    2. __purposes__: `data DataType.IndexRecords neverFlows vertex !ConsentedPurposes.ViewRecords`

## 🚨 Violations
- __None__




<script setup>
import { VPButton } from 'vitepress/theme'
</script>


