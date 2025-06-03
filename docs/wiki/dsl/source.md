# Source Selectors
A **source selector** describes the origin of a data flow though the system.
It can select nodes based on their node label or data labels flowing into the node.

## Vertex Selector
To select nodes based on their label, one can use `vertex <Type>.<Value>` where `<Type>` describes a label type that must be present at a given node and `<Value>` must describe a label value of the defined label type that must be present at the node. 
The selector can be inverted to using `vertex !<Type>.<Value>`.
::: tip Example
For the label `Location` and its values `EU` and `nonEU`, one might want to match all flows originating outside of the EU.
For that, one might employ the following **source selector**:
```
vertex Location.nonEU
```
:::

<!---
FIXME: Different naming between PCM and DFD for the incoming pin/variable
--->
## Data Selector
To select nodes based on their incoming labels on *any* pin/variable, one can use `data <Type>.<Value>`, where `<Type>` describes a label type that must be folowing into the node. 
`<Value>` must be a label of label type `<Type>` and must flow into the selected node.
The selector can be inverted to match nodes that do not have the label at *any* pin/variable by using `data !<Type>.<Value>`.
::: tip Example 
For the label type `Sensitivity` and its labels `Personal` and `Public`, one can select nodes processing personal data using the following **source selector**:
```
data Sensitivity.Personal
```
:::

<!---
FIXME: Different naming between PCM and DFD for the incoming pin/variable
--->
## Data List Selector
To select nodes based on multiple of their incoming labels on *any* pin/variable, one can use `data <Type1>.<Value1>,<Type2>.<Value2>,...`, where `<TypeX>` describes a label type of which one must be flowing into the node. 
`<ValueX>` must be a label of label type `<TypeX>` and one must flow into the selected node.
To match this selector, the node must only satisfy *one* of the `<Type>.<Value>` combinations in the list.
The selector can be inverted to match nodes that do not have any of the label at *any* pin/variable by using `data !<Type1>.<Value1>,<Type2>.<Value2>,...`.
::: tip Example 
For the label type `Sensitivity` and its labels `Personal` and `Public`, one can select nodes processing personal *or* public data using the following **source selector**:
```
data Sensitivity.Personal,Sensitivity.Public
```
:::



## Vertex Type Selector
Additionally, one might select nodes based on their type using the `vertex type <Type>` **source selector**.
The `<Type>` describes the model element the node must have to match the selector.
Note that is property is dependent on the type of model that you are analyzing.
The selector can be inverted to match nodes that do not have the specified type using `vertex type !<Type>`.
::: tip Example 
Assuming that you are working with a [Data Flow Diagram](dfd/intro.md), one might want to match all occurences where data is processed.
To do that, the constraint should match all processing elements present in the model.
```
vertex type PROCESSING
```
:::

<!--
FIXME: Different naming between PCM and DFDs for imcoming pin/variables
-->
## Data Name Selector
Lastly, one might select nodes based on the names of their incoming flows using `data named <Name>`.
The `<Name>` placeholder can be replaced by any name and matches any node that has a incoming flow/variable with that name.
The selector cannot be inverted.
::: tip Example 
If one might want to match secrets flowing into a node and flows/variables containing secrets are named "secrets", one might match them with the following **source selector**:
```
data named secrets
```
:::
