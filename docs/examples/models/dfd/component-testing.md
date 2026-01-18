# 📊 Diagram:  Component Testing System Case Study

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/ComponentTestingCaseStudy/ComponentTestingCaseStudy.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Full Main Source](<https://www.sciencedirect.com/science/article/pii/S016412122100100X?via%3Dihub>)


## 📝 Short Description
This diagram illustrates the first case study in the paper, which briefly describes a system focused on component testing. The diagram shows the flow of data between the different components in the system. The paper does not describe the structure of these systems as they belong to private companies. For this reason we took the liberty of adding some components to the very brief description that is given.

## 🔤 Abbreviations
- UI: User Interface
- I/O: Input/Output

## 📖 Extensive Description (if possible)
Starting from the left side of the diagram there is the __Test Operator__ node, which sends its *credentials* to an __Authorization Service__ and a *test_request* (request to test a component) to the __Platform UI__. From the __Authorization Service__ an access token is generated (after the credentials of the operator have been checked) and sent to the __Platform UI__ and __View Measurements__ (at the right end of the diagram) nodes. From the __Platform UI__ the *test_request* is forwarded to the __Schedule Test__ for scheduling a test, and the selected component data (*component*) is sent to the __Select Component__ node. This node then forwards it to the __Run Test__ node, which in addition receives the *simulation_data* from the __Simulator__ node. From the __Run Test__ node *test_data* is generated and sent to the __Real Time Processor__. From here one the *test_data* is forwarded throughout various nodes (__I/O__ and __ECU__) till it reaches the __Component__ node, where the *test_data* is going to be tested physically. After testing it in the component, the *measurements* are sent to the __Collect Measurements__ node. From here the *measurements* flow to the __View Measurements__ and __Store In The CLoud__ node. Measurements can be viewed by the operator with the previously *access_token* in the __View Measurements__ node. Lastly from the __Store In The Cloud__ node the *measurements* flow to the __Private Cloud__ node where they are stored.
 
## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### Data:
        - __Credentials__: Credentials data from the operator
        - __TestRequest__: Request from the operator to test a component
        - __AccessToken__: Access token generated after the credentials have been checked to do some operations
        - __Component__: Component which the test data will be tested on
        - __SimulationData__: Data from the simulatior
        - __Measurements__: Measurements from the testing of the component
        - __TestData__: Data from running the test
        
- ### 🏷️ Node Labels:
    __None__

## ⚠️ Constraints
- __None__

## 🚨 Violations
- __None__






<script setup>
import { VPButton } from 'vitepress/theme'
</script>


