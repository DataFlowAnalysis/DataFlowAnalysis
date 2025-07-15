# Usage of Graphical Editors
There are some general mechanisms that apply to all graphical editors for particular models. 
For specific aspects of specific graphical editors follow these links to the corresponding model: 
- [Allocation](/wiki/pcm/allocation-editor)
- [Assembly](/wiki/pcm/assembly-editor)
- [Data Dictionary](/wiki/pcm/data-dictionary-editor)
- [Repository](/wiki/pcm/repository-editor)
- [Resource Enviornment](/wiki/pcm/resource-editor)
- [SEFFs](/wiki/pcm/seff-editor)
- [Usage Model](/wiki/pcm/usage-editor)

Information on how to use the confidentiality extensions can be found in the [Confidentiality Modeling Wiki](/wiki/pcm/confidentiality-usages)

## Opening Graphical Editors 
![Model Explorer with expanded allocation model](/img/pcm/model-explorer.png) 
There are multiple ways to open the graphical editor for a certain model.
The simplest way is to expand the model contents in the Model Explorer up to the second level as shown in the figure.
On the second level, there will be an entry for the graphical diagram and name usually indicates that it is a diagram. 
By doing a double-click on that entry, a graphical editor will open. 
If such an entry does not exist, there is no graphical diagram for the particular model.

## Creating Elements
![Partial palette of repository model](/img/pcm/sirius-palette.png) 
Every editor contains a palette on the right hand side consisting of multiple tools that you can use to create elements. In order to create an element, select it and click in the appropriate location within the diagram. If you cannot click at a certain location, the element cannot be created there. You should choose another location to create it.
By holding the `CTRL` key while clicking in the diagram, the selected tool will stay selected. 
This is useful if you create a large amount of elements of the same type. 
The selected tool can be disabled using `ESC` at any time

## Viewing Semantic Properties of Diagram Elements
![Properties view showing semantic properties of an allocation](/img/pcm/properties-view-semantic-properties.png) 
Often, relevant properties can be changed by inline-editing within the editor. 
However, some properties of elements are only available via the so-called Properties view. 
To show the properties for an element, it needs to be selected in the editor, then go to the properties view.
The contents of the view depend on the selected object. 
On the left side of the view, there is a list of tabs grouping various properties. 
Again, the shown tabs can vary but there will always be a *Semantic* tab as shown in the figure. 
This tab shows the properties of the selected element as they are represented in the underlying model. 
Many of the listed properties can be changed.
:::warning
   Please be aware that there are usually no sanity checks for the changes you do in the properties view. An invalid change can even break parts of the graphical editor or can render your model useless. Therefore, only do changes in the properties view if you are asked to do so or you are completely sure about your changes.
:::
