# Specifying a Data Dictionary
The data dictionary holds the characteristic types including their value ranges. 
These types are used for specifying characteristics of nodes and data transmitted through the system.

## Dictionary creation
![Palladio toolbar](/img/pcm/palladio-toolbar.png) 
In order to create a dictionary, use the Palladio toolbar located in the top of the tooling window that is shown in the figure. 
To create a dictionary, press the button named `DD` next to the usage model button.

![Dialog for creating a data dictionary](/img/pcm/dialog-dd-creation.png) 
In the upcoming dialog shown in the figure, select the project into which the data dictionary should be put. 
The file name of the dictionary can be chosen freely but has to end with `.pddc`. 
A validation message in the top of the dialog reports if the file name is valid.

After pressing `OK`, the dictionary is created and a textual editor for that dictionary will be opened.

## Editing Support of Editor
![Editing support in dictionary editor](/img/pcm/xtext-ddc-editingsupport.png) 
The editor provides editing support by code completion proposals, syntax highlighting and validation messages as shown in the figure.
The code completion proposals suggest keywords as well as elements that can be referenced. 
Validation messages indicate if entries are valid.

## Syntax of Dictionary
Every dictionary starts with the keyword `dictionary` followed by an `id` keyword and a unique identifier. 
There is usually no reason to change the generated identifier. 
After the identifier, there can be multiple enumerations and characteristic types.

Enumerations start with the keyword `enum` followed by a name. 
The actual values contained in the enumeration are enclosed by curly brackets. 
Individual keywords are separated by at least one whitespace character such as a single whitespace or a line break. 
An enumeration holding the colors red, green and blue would look like follows:
```
enum Colors {
  Red
  Green
  Blue
}
```

Enum characteristic types use enumerations to specify their possible values. 
Such a characteristic type starts with the keyword `enumCharacteristicType` followed by a name for the type. 
The used enumeration is specified by the keyword `using` followed by the name of the enumeration. 
A enum characteristic type describing background colors would look like follows:
```
enumCharacteristicType BackgroundColor using Colors
```

::: warning
Avoid to change the order of characteristic types or deleting a characteristic type after a characteristic type has been used in a system. 
Otherwise, the internal references between the system and the dictionary will break and everything that made use of a characteristic type needs to be recreated.
Instead of removing a characteristic type, change the name or don't use the characteristc type anymore. 
New characteristic types can always be added.
:::

