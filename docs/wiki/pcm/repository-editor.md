# Usage of Repository Editor
The repository editor allows to specify the building blocks for creating a system.

## Contents of Editor
The editor consists of components, interfaces and data types, as well as of edges between these elements.
![Roles of component](/img/pcm/sirius-repository-component-roles.png) 
A component always has at least one edge to an interface that describes the provided services, a so-called provided role. 
As shown in the figure, this is shown by an accordingly labeled edge. 
A component can have edges to interfaces that describe required services, a so-called required role. 
This is also shown by an accordingly labeled edge.

Within the components, there are SEFFs (Service Effect Specifications) for every provided service, i.e. every signature of an interface that has been referenced as provided role.

## Opening the SEFF Editor
In order to open the graphical editor describing the SEFF of the component, it is possible to do any of the following:
- Double-click on the corresponding entry
- Right-click on the entry and select `Open` and the shown diagram entry afterwards

Please see the description of the [SEFF Editor wikipage](/wiki/pcm/seff-editor) for details on that particular editor
