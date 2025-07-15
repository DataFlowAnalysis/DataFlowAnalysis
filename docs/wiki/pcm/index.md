# Data Flow Analysis with Palladio
The Palladio Component Model is an [Archtecture Description Language](https://en.wikipedia.org/wiki/Architecture_description_language) for modeling software systems.
The model contains various models used to describe the system from different viewpoints.

For more information on the base models see [our small summary](/wiki/pcm/models) or the [Palladio Component Model Wiki](https://sdq.kastel.kit.edu/wiki/Palladio_Component_Model)

## Confidentiality Extension
For our analysis to gain information about the data flows in the system, the Palladio Extension extends existing models and introduces two additional models:

For information on how to use the provided editors for both PCM and the Confidentiality Extension, see the [Editor Section](/wiki/pcm/editors)

### Data Dictionary
The `.pddc` model describe the existing [Characteristic Types](/wiki/glossary#characteristic-type) and their [Characteristic Values](/wiki/glossary#characteristic-value) in enums.
Those are defined using the `enum` keyword and an identifier and list the different possible values of that type.
An enum is defined as a [Characteristic Type](/wiki/glossary#characteristic-type) with a given name using the `enumCharacteristicType` keyword followed by the name of the [Characteristic Type](/wiki/glossary#characteristic-type) and the keyword `using` followed by the name of the enum.

### NodeCharacteristics
The `.nodecharacteristics` files contains the node characteristics defined for some objects in the model. They fit into the following categories:
| Name              | Annotated Element     | 
| ----------------- | --------------------- |
| Usage Assignee    | Usage Scenario        | 
| Resource Assignee | Resource Container    |
| Assembly Assignee | Assembly Context      |

### Extensions
In addition to the two models, a new variable usage has been introduced that can be used within EntryLevelSystemCalls, ExternalCalls and SetVariableActions:
In these confidentiality variable usages, one can use an expression to describe how data is processed within that node.
The left side must be an expression of `<VariableName>.<CharacteristicType>.<CharacteristicValue>` while the right side must be a boolean or another expression.
A `*` in those assignments match all [Characteristic Types](/wiki/glossary#characteristic-type) or [Characteristic Values](/wiki/glossary#characteristic-value).
The variable names used thoughout are determined by the names of the parameters of functions defined in the model and the name of the variables referenced in Variable Usage elements.
