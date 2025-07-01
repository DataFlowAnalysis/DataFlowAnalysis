# Working with the Data Flow Analysis Online Editor
Using the Online Editor is simple:

The Panel in the top right contains all model elements that can be placed:
The first three elements place [Nodes](/wiki/dfd/index#data-flow-diagram) in the editor using your mouse. 
The fourth tool creates flows between the input and output ports of a node. 
The fifth and sixth tool creates those input or output ports for nodes by clicking on the node they should be placed on. 

The `Label Types` panel below is responsible for creating [Label Types](/wiki/dfd/index#data-dictionary) and their [Label (Values)](/wiki/dfd/index#data-dictionary).
[Label Types](/wiki/dfd/index#data-dictionary) can be created using the `+ Label Type` button and deleted by using the icon right of their name.
[Label (Values)](/wiki/dfd/index#data-dictionary) can be created using the `+ Value` below the corresponding [Label Type](/wiki/dfd/index#data-dictionary).
They can be renamed by clicking their name field, and deleted using the icon right of their name as well.

Applying a [Label (Value)](/wiki/dfd/index#data-dictionary) to nodes can be done using drag and drop:
Simply find the [Label (Value)](/wiki/dfd/index#data-dictionary) and drag it ontop a node.

The behavior of a output port for a node can be edited by double clicking the output port in question.
For information on how to use the assignments see the [Assignment Guide](/wiki/webeditor/assignments)

Constraints can be entered using the panel on the botton right:
A new constraint can be created using `+ Constraint` and edited in the text field on the left:
A guide on how to write constraints can be found in the [Constraint Guide](/wiki/dsl).
The constraint can be run using the green run button in the bottom left corner. 
Vertices violating the defined constraints will be colored and more information is available by hovering over the element in question.

Additionally, the settings panel can be found on the bottom left:
The theme setting switches between light and dark mode, while the layout method allows different autolayouting of the diagram. 
The `Hide Edge Labels` hides the name of edges, to reduce visual clutter for larger models.
Using `Simplify Node Names` one can force short non-descriptive names for nodes, useful for getting a very high-level overview of the data flows though the system.
The `Read Only` toggle is useful for demonstration purposes to disallow editing of the model. 
It still allows moving nodes around.

Finally, the command palette can be used to load, save, layout and fit diagrams while using the editor.
Furthermore, it can be used to load out default Online Editor model

Keyboard Shortcuts can be found on our [Shortcuts Page](/wiki/webeditor/shortcuts)
