# Variables
A variable describes a variable component in selecting nodes and can be used to compare values within one selector or between them.
They are used in conjuction with [Conditional Selectors](/docs/wiki/dsl/conditional).

A variable may be defined using the `$` (Dollar) sign.
::: tip Example 
If one wants to define a variable foo, you can do so like this:
```
$foo
```
:::

A variable may be used in place of a label or label type in any [Source Selector](/docs/wiki/dsl/source) or [Destination Selector](/docs/wiki/dsl/destination).
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
