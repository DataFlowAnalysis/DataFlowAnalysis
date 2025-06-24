# Analysis DSL
Constraints in our Data Flow Analysis can be expressed in our textual DSL:
They commonly consist of two parts:
A set of *source selectors* and a set of *destination selectors*.
Additionally, one can define some relationships between the two selectors using *conditional selectors*.

[Source Selectors](source.md) select the origin of a data flow based on their node label, incoming data, type or incoming names of data. 

[Desintation Selectors](destination.md) selects the destination of a data flow based on their node label.

[Conditional Selectors](conditional.md) selects nodes based on [Variables](variables.md) defined in the previous two selectors.
