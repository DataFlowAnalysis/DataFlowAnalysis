# Defining Variable Usages for Confidentiality
Variable usages allow to define characteristics of variables, which can essentially be parameters and return values. 
The types of characteristics, which can be used, are given by a data dictionary.
The specification of characteristics is done using assignments

## Prerequisites
In order to specify variable usages that can be used to reason about confidentiality, a data dictionary has to exist and contain characteristic types. 
This is because the characteristics are typed by the characteristic types in the data dictionary.

It is also necessary that the action holding the variable usage is already part of a control or usage flow. 
This means, the action has to be connected by the flow edges. 
Otherwise, determining the parameters and return values that can be used is not possible.

## Creation of Characterizations
![Tool to create variable characterizations for confidentiality](/img/pcm/sirius-variablecharacterization-confidentiality.png) 
![Dialog to edit assignments](/img/pcm/sirius-variablecharacterization-confidentiality-assignmentsdialog.png) 
A characterization is a sequence of assignments. 
To create such an assignment, the tool `Confidentiality Variable Characterisation` shown in the figure must be selected from the palette and added by clicking into the corresponding variable usage. 
Afterwards, the dialog shown in the second figure opens and it is possible to write a sequence of assignments. 
The assignments have to follow the [Syntax of Assignments](#syntax-of-assignments) described later. 
After confirming the dialog, the assignments will be created and shown within the variable usage in the graphical editor.

## Editing Existing Characterizations
![Properties view allowing to edit assignments](/img/pcm/properties-view-assignments.png) 
To edit existing characterizations, one needs to use the properties view. 
First, select any assignment within the variable usage. 
After that, select the `Assignments` tab in the properties view that is also shown in the figure of this section. 
After pressing the `Edit` button, the dialog appears and the assignments can be edited. 
After confirming the dialog, all assignments are replaced by the assignments that were just specified in the dialog.

## Editing Support of Editor
![Editing support in assignments editor](/img/pcm/sirius-variablecharacterization-confidentiality-assignmentsdialog-editingsupport.png) 
The dialog provides editing support by code completion proposals and validation messages as shown in the above figure.

The code completion proposals suggest keywords as well as elements that can be referenced. 
These suggestions only work if the action that contains the variable usage and therefore also the assignments is correctly integrated into the usage or control flow.

## Syntax of Assignments
Assignments assign truth values to triples of variable, characteristic type and value. 
If the value is `true`, the given value of the given characteristic type is available on the variable. 
If there is no assignment for a triple or the truth value is `false`, the given value of the given characteristic type is not available on the variable. 
If there are multiple assignments to the same triple, only the last assignment is effective.

The syntax of an assignment is `variable.characteristicType.value := Term`, where `Term` is a term that yields a truth value. 
The `variable` is always the variable defined by the variable usage. 
The `characteristicType` is one particular characteristic type from the data dictionary. 
The `value` is one value from the enumeration that defined the value range of the characteristic type. 
The supported types of terms are the following:
| Term Type                 | Syntax                            | Example           |
| ------------------------- | --------------------------------- | ----------------- |
| Constant                  | true                              | true              |
| Constant                  | false                             | false             |
| Negation                  | !Term                             | !false            |
| Binary Logic              | Term & Term                       | false & true      |
| Binary Logic              | Term \| Term                      | false \| true     |
| Characteristic Reference  | variable.characteristicType.value | RETURN.color.red  | 

A characteristic reference on the right hand side of the assignment can refer to available variables. 
Such variables typically are parameters or return values. 
By referring to other variables, the assignments specify a propagation of characteristics.

To simplify specifications, it is possible to omit the characteristic type and the value from characteristic references on the left hand side als well as on the right hand side of an assignment. 
To omit an element, the asterisk `*` can be used in the particular location. 
If the characteristic type is ommited, the value needs to be ommited as well. 
Additionally, elements on the right hand side of an assignment can only be ommited if these elements are ommited on the left hand side of the assignment as well.

Assignments containing omissions will be instantiated with appropriate values during runtime. 
If you omit a value but specify a characteristic type having `n` values, there will be virtually `n` assignments. 
In assignment `i` for `0 <= i < n` there will be the value with index `i` be inserted in all places, in which a value has been omitted. 
If the characteritic type and the value have been omitted, there will be an assignment for every tuple of characteristic type and values of this characteristic type.

#### Example
To illustrate the effect of omissions, let's assume there are the two characteristic types `ForegroundColor` and `BackgroundColor`, which use the same enumeration specifying the colors `Red`, `Blue` and `Green`. 
The variable `out` is specified and can refer to a variable `in`. 
The following examples show how omissions are handled for this particular example.

If only the value is omited, ensure that the used characteristic types have compatible value ranges. 
The value ranges are compatible, if the characteristic types refer to the same enumeration. 
This implies that the value ranges are the same. 
The meaning of the following example is that the foreground color of the output variable shall have the background color of the input variable.

```
out.ForegroundColor.* := in.BackgroundColor.*
```
becomes
```
out.ForegroundColor.Red := in.BackgroundColor.Red
out.ForegroundColor.Blue := in.BackgroundColor.Blue
out.ForegroundColor.Green := in.BackgroundColor.Green
```
If you omit the characteristic type and the value on the left hand side of the assignment, you either have to omit both on the right hand side of the assignment or specify both. In the following example, the output variable shall have exactly the same characteristics as the input variable but only if the foreground color of the input variable is red.
```
out.*.* := in.*.* & in.Foreground.Red
```
becomes
```
out.ForegroundColor.Red := in.ForegroundColor.Red & in.Foreground.Red
out.ForegroundColor.Blue := in.ForegroundColor.Blue & in.Foreground.Red
out.ForegroundColor.Green := in.ForegroundColor.Green & in.Foreground.Red
out.BackgroundColor.Red := in.BackgroundColor.Red & in.Foreground.Red
out.BackgroundColor.Blue := in.BackgroundColor.Blue & in.Foreground.Red
out.BackgroundColor.Green := in.BackgroundColor.Green & in.Foreground.Red
```
If you specify multiple assignments for the same variable (as shown in the example before), you have to use a simplified syntax. In case of the example shown before, the correct syntax would have to be like in the following excerpt.
```
out. {
  ForegroundColor.Red := in.ForegroundColor.Red & in.Foreground.Red
  ForegroundColor.Blue := in.ForegroundColor.Blue & in.Foreground.Red
  ForegroundColor.Green := in.ForegroundColor.Green & in.Foreground.Red
  BackgroundColor.Red := in.BackgroundColor.Red & in.Foreground.Red
  BackgroundColor.Blue := in.BackgroundColor.Blue & in.Foreground.Red
  BackgroundColor.Green := in.BackgroundColor.Green & in.Foreground.Red
}
```
