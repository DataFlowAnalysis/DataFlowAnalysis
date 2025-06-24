# Destination Selectors 
A **destination selector** describes the destination of a data flow though the system. 
It selects nodes based on their vertex label or their vertex type.

## Vertex Selector
To select nodes based on their label, one can use `vertex <Type>.<Value>` where `<Type>` describes a label type that must be present at a given node and `<Value>` must describe a label value of the defined label type that must be present at the node. 
This selector can be inverted using `vertex !<Type>.<Value>`.
::: tip Example
For the label `Location` and its values `EU` and `nonEU`, one might want to match all flows originating outside of the EU.
For that, one might employ the following **source selector**:
```
vertex Location.nonEU
```
:::

## Vertex Type Selector
Additionally, one might select nodes based on their type using the `vertex type <Type>` **source selector**.
The `<Type>` describes the model element the node must have to match the selector.
Note that is property is dependent on the type of model that you are analyzing.
This selector can be inverted using `vertex type !<Type>`.
::: tip Example 
Assuming that you are working with a [Data Flow Diagram](/wiki/dfd/index.md), one might want to match all occurences where data is processed.
To do that, the constraint should match all processing elements present in the model.
```
vertex type PROCESSING
```
:::


