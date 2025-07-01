# Conditional Selectors
Conditional selectors use the values of [Variables](/docs/wiki/dsl/variables) to create additional constraints for nodes to fulfil:
The constraints can check whether variables are present and whether the intersection of variable values is empty 

## Present Selector
The present selector checks whether the value of a variable is present and matches the node, if it has at least one value.
A present selector is written as `present $<VariableName>` where `<VariableName>` is the name of the variable, without the `$`.
The selector can be inverted to match nodes when the variable is not present by `present !$<VariableName>`.

::: tip Example
Assuming a variable `$Roles` exists, one can check whether the variable is *not* present using the following **conditional selector**:
```
present !$Roles
```
:::

## Empty Selector
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
