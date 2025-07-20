# Glossary of Commonly Used Terms
## Data Flow Diagram
Describes the [Data Flow Diagram](/wiki/dfd#data-flow-diagram) (the ***metamodel***).
Sometimes describes the entire model of a system, including the [Data Dictionary](/wiki/dfd#data-dictionary) (the ***modelling framework***)

## Data Dictionary
A component of the [Data Flow Diagram Model](/wiki/dfd/) responsible for storing [Node Label](/wiki/glossary#node-label) or [Data Label](/wiki/glossary#data-label)

## Palladio Component Model (PCM)
An [Architecture Description Language](https://en.wikipedia.org/wiki/Architecture_description_language) describing software with several analyses to analyze software quality attributes.
Can be used as an input model for the analysis.
For more information see: [Palladio Component Model](/wiki/pcm/)

## Characteristic Type 
see [Label Type](/wiki/glossary#label-type)

## Characteristic Value 
see [Label (Value)](/wiki/glossary#label-value)

## Label Type
A Property of a [Node](/wiki/glossary#node) or [Data Flow](/wiki/glossary#data-flow) with one ore more [Label (Values)](/wiki/glossary#label-value)
For more information see: [Data Flow Diagram](/wiki/dfd/index#data-dictionary)

## Label (Value)
A value of a [Label Type](/wiki/glossary#label-type) describing a property of a [Node](/wiki/glossary#node) or [Data Flow](/wiki/glossary#data-flow)
For more information see: [Data Flow Diagram](/wiki/dfd/index#data-dictionary)

## Source Selector
A component of the [Analysis Constraint DSL](/wiki/dsl/) that describes properties that a [Node](/wiki/glossary#node) or [Data Flow](/wiki/glossary#data-flow) needs to have to be matched by the constraint
For more information see: [Source Selector](/wiki/dsl/source)

## Destination Selector
A component of the [Analysis Constraint DSL](/wiki/dsl/) that describes properties that a [Node](/wiki/glossary#node) or [Data Flow](/wiki/glossary#data-flow) need to have to be matched by the constraint.
For more information see: [Destination Selector](/wiki/dsl/destination)

## Conditional Selector
A component of the [Analysis Constraint DSL](/wiki/dsl/) that matches [Nodes](/wiki/glossary#node) based on [DSL Variables](/wiki/dsl/variables).
For more information on variables see: [DSL Variables](/wiki/dsl/variables).
For more information on conditional selectors see: [Conditional Selectors](/wiki/dsl/conditional)

## Vertex
A vertex describes an element of a [Transpose Flow Graph](/wiki/glossary#transpose-flow-graph) that corresponds to exactly one element in the model.
Data may flow into and out of the vertex, their properties available as [Data Label](/docs/glossary#data-label).
Additionally properties of the vertex are available as [Vertex Label](/docs/glossary#vertex-node-label).
For more information see: [Data Flow Analysis](/wiki/analysis/).

Not to be confused with a [Node](/wiki/glossary#node) that is an element of a [Data Flow Diagram](/wiki/dfd#data-flow-diagram)

## Node 
A node describes an element of an [Data Flow Diagram](/wiki/dfd#data-flow-diagram).

Not to be confused with a [Vertex](/wiki/glossary#vertex) that is part of an [Transpose Flow Graph](/wiki/glossary#transpose-flow-graph)

## Source 
A Source is a special [Vertex](/wiki/glossary#vertex) that data does not flow into. 
It only has outgoing data.
For more information see: [Data Flow Analysis](/wiki/analysis/)

## Sink
A Sink is a special [Vertex](/wiki/glossary#vertex) that data does not flow out of.
It only has incoming data and clearly defines a [Transpose Flow Graph](/wiki/glossary#transpose-flow-graph) with its previous nodes.
For more information see: [Data Flow Analysis](/wiki/analysis/)

## Data Flow
A Data Flow describes a flow from one [Node](/wiki/glossary#node) to another [Node](/wiki/glossary#node).
It may has certain properties after [Label Propagation](/wiki/analysis/label-propagation)

## Transpose Flow Graph 
A Transpose Flow Graph describes a unique flow though the software system and can be identified using a [Sink](/wiki/glossary#sink)

## Vertex / Node Label
A Vertex or Node Label is a [Label Type](/wiki/glossary#label-type) that is applied to a [Node](/wiki/glossary#node).
It can be used in a [DSL Constraint](/wiki/dsl/) to match vertices using their vertex/node label

## Data Label
A Data Label is a [Label Type](/wiki/glossary#label-type) that is applied to a [Data Flow](/wiki/glossary#data-flow).
It can be used in a [DSL Constraint](/wiki/dsl/) to match vertices using their vertex/node label

