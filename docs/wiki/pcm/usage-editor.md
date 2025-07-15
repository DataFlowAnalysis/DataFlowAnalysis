# Usage of Usage Model Editor
The usage model editor allows specifying the interaction of users with the system. 
In order to do that, it is necessary to have a system specified via the [Assembly Editor](/wiki/pcm/assembly-editor).

![Simple usage model diagram](/img/pcm/sirius-usagemodel.svg) 

## Contents of Editor
The editor contains multiple usage scenarios. 
A usage scenario describes the interaction of a certain type of users with the system. 
The scenario contains a behavior description. 
The description consists of a sequence of activies. 
The most important activities are calls to the system.

## Specification of Entry Level System Calls
Entry level system calls make calls to system services. 
The effect of a call on quality properties is specified by so called variable usages. 
A variable usage describes how a variable, which can essentially be a parameter or return value, is characterized. 
The variable is defined by a name shown in the top of the grey rectangles that visualize variable usages. 
The definition of characteristics shown below the name can refer to other characteristics, which enables the propagation of characteristics through the system.

Call actions allow to specify two types of variable usages: usages for parameters of the called service (i.e. characterizations of the sent parameters) and usages for the return value of the called service (i.e. characterizations of the received return value). 
To specify characteristics for a parameter, the variable usages has to have the very same name as the parameter in the signature of the called service. 
To specify characteristics of the return value, a variable usage with any name can be defined. 
However, when referring to the characteristics of the particular return value from the called service, the keyword `RETURN` has to be used.
For creating variable usages for analyzing confidentiality see the [confidentiality Modeling Page](/wiki/pcm/confidentiality-usages).
