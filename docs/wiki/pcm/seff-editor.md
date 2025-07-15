# Usage of SEFF Editor
The SEFF editor allows specifying the effect on aspects relevant for particular quality predictions for a particular service provided by a component. 
The SEFF editor from within the [Repository Editor](/wiki/pcm/repository-editor).

## Contents of Editor
![Simple SEFF diagram](/img/pcm/sirius-seff-overview.png) 
The editor consists of a sequence of actions and follows a notation similar to UML activity diagrams. 
The various actions are described in the following section:

## Specification of Actions
### External Call Action
External call actions make calls to required services. 
When creating such a call, the called component is not know yet because this depends on the actual wiring in the system or assembly diagram. 
This is beneficial because it decouples components and reduces their interaction to interactions through defined interfaces.

The effect of a call on quality properties is specified by so called variable usages. 
A variable usage describes how a variable, which can essentially be a parameter or return value, is characterized. 
The variable is defined by a name shown in the top of the grey rectangles that visualize variable usages. 
The definition of characteristics shown below the name can refer to other characteristics, which enables the propagation of characteristics through the system.

Call actions allow to specify two types of variable usages: usages for parameters of the called service (i.e. characterizations of the sent parameters) and usages for the return value of the called service (i.e. characterizations of the received return value). 
To specify characteristics for a parameter, the variable usages has to have the very same name as the parameter in the signature of the called service. 
To specify characteristics of the return value, a variable usage with any name can be defined. 
However, when referring to the characteristics of the particular return value from the called service, the keyword `RETURN` has to be used.

For creating variable usages for analyzing confidentiality see the [confidentiality Modeling Page](/wiki/pcm/confidentiality-usages).

### Set Variable Action
Set variable actions are used to define characteristics of the return value of the provided service. 
To specify the return value, a variable usage named `RETURN` has to be created and specified. 
For creating variable usages for analyzing confidentiality see the [confidentiality Modeling Page](/wiki/pcm/confidentiality-usages).
