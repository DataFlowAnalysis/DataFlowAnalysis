# Analysis DSL
Constraints in our Data Flow Analysis can be expressed in our textual DSL:
They commonly consist of two parts:
A set of *source selectors* and a set of *destination selectors*.
Additionally, one can define some relationships between the two selectors using *conditional selectors*.

## Source Selectors
A **source selector** describes the origin of a data flow through the system.
It can select nodes based on their node label or data labels flowing into the node.

### Vertex Selector
To select nodes based on their label, one can use `vertex <Type>.<Value>` where `<Type>` describes a label type that must be present at a given node and `<Value>` must describe a label value of the defined label type that must be present at the node. 
The selector can be inverted to using `vertex !<Type>.<Value>`.
::: tip Example
For the label `Location` and its values `EU` and `nonEU`, one might want to match all flows originating outside of the EU.
For that, one might employ the following **source selector**:
```
vertex Location.nonEU
```
:::

### Data Selector
To select nodes based on their incoming labels on *any* pin/variable, one can use `data <Type>.<Value>`, where `<Type>` describes a label type that must be folowing into the node. 
`<Value>` must be a label of label type `<Type>` and must flow into the selected node.
The selector can be inverted to match nodes that do not have the label at *any* pin/variable by using `data !<Type>.<Value>`.
::: tip Example 
For the label type `Sensitivity` and its labels `Personal` and `Public`, one can select nodes processing personal data using the following **source selector**:
```
data Sensitivity.Personal
```
:::

### Data List Selector
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



### Vertex Type Selector
Additionally, one might select nodes based on their type using the `vertex type <Type>` **source selector**.
The `<Type>` describes the model element the node must have to match the selector.
Note that is property is dependent on the type of model that you are analyzing.
The selector can be inverted to match nodes that do not have the specified type using `vertex type !<Type>`.
::: tip Example 
Assuming that you are working with a [Data Flow Diagram](/wiki/dfd/), one might want to match all occurences where data is processed.
To do that, the constraint should match all processing elements present in the model.
```
vertex type PROCESSING
```
:::

### Data Name Selector
Lastly, one might select nodes based on the names of their incoming flows using `data named <Name>`.
The `<Name>` placeholder can be replaced by any name and matches any node that has a incoming flow/variable with that name.
The selector cannot be inverted.
::: tip Example 
If one might want to match secrets flowing into a node and flows/variables containing secrets are named "secrets", one might match them with the following **source selector**:
```
data named secrets
```
:::



## Destination Selectors 
A **destination selector** describes the destination of a data flow though the system. 
It selects nodes based on their vertex label or their vertex type.

### Vertex Selector
To select nodes based on their label, one can use `vertex <Type>.<Value>` where `<Type>` describes a label type that must be present at a given node and `<Value>` must describe a label value of the defined label type that must be present at the node. 
This selector can be inverted using `vertex !<Type>.<Value>`.
::: tip Example
For the label `Location` and its values `EU` and `nonEU`, one might want to match all flows originating outside of the EU.
For that, one might employ the following **source selector**:
```
vertex Location.nonEU
```
:::

### Vertex Name Selector 
A node can be selected according to it's name in the model, using the `named <Name>` selector, where `<Name>` is the name the vertex should have.
The specified name cannot contain any spaces.
Additionally, this selector can be inverted using `!named <Name>`.
::: tip Example 
To match all vertices **exactly** named "Database", one might use the following selector: 
```
named Database
```
:::

### Vertex Type Selector
Additionally, one might select nodes based on their type using the `vertex type <Type>` **source selector**.
The `<Type>` describes the model element the node must have to match the selector.
Note that is property is dependent on the type of model that you are analyzing.
This selector can be inverted using `vertex type !<Type>`.
::: tip Example 
Assuming that you are working with a [Data Flow Diagram](/wiki/dfd/), one might want to match all occurences where data is processed.
To do that, the constraint should match all processing elements present in the model.
```
vertex type PROCESSING
```
:::





## Conditional Selectors
Conditional selectors use the values of [Variables](/wiki/dsl/index#variables) to create additional constraints for nodes to fulfil:
The constraints can check whether variables are present and whether the intersection of variable values is empty 

### Present Selector
The present selector checks whether the value of a variable is present and matches the node, if it has at least one value.
A present selector is written as `present $<VariableName>` where `<VariableName>` is the name of the variable, without the `$`.
The selector can be inverted to match nodes when the variable is not present by `present !$<VariableName>`.

::: tip Example
Assuming a variable `$Roles` exists, one can check whether the variable is *not* present using the following **conditional selector**:
```
present !$Roles
```
:::

### Empty Selector
The empty selector selects nodes based on the value of the set operation that follows the selector. 
Currently only intersections between the values of two variables are supported:
To check whether the intersection between the values of two variables is empty one can use the following selector: 
`empty intersection($<VariableName1>, $<VariableName2>)`.
Both `$<VariableName1>` and `$<VariableName2>` must be variables defined by selectors of the constraint. 

::: tip Example 
If one wants to check whether a node is allowed to access the data, and the allowed access levels of the node is defined by `$GrantedRoles` while the access levels to access the data at the node is defined as `$AllowedRoles` one can express this constraint in the following selector: 
```
empty intersection($GrantedRoles, $AllowedRoles)
```
Please note that if either `$GrantedRoles` or `$AllowedRoles` is empty this selector will match.
If this is not desired behavior one can add the following to ensure both variables have values: 
```
present $GrantedRoles present $AllowedRoles
```
:::



## Variables
A variable describes a variable component in selecting nodes and can be used to compare values within one selector or between them.
They are used in conjuction with [Conditional Selectors](/wiki/dsl/index#conditional-selectors).

A variable may be defined using the `$` (Dollar) sign.
::: tip Example 
If one wants to define a variable foo, you can do so like this:
```
$foo
```
:::

A variable may be used in place of a label or label type in any [Source Selector](/wiki/dsl/index#source) or [Destination Selector](/wiki/dsl/index#destination).
::: tip Example 
This example assumes the Location of a node is modelled using a label type `Location` with labels `EU` and `nonEU`, applied to each node as a node label.
If one wants to create a variable "Location" containing the Location of a node, one can write the following:
```
vertex Location.$Location
```

For label types `Encryption` and `Authenticated` both with label `Set`, one can store the label types that are set in a variable called "Features" using the following selector:
```
vertex $Features.Set
```
:::
