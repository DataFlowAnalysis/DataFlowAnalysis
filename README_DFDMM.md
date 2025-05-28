# DFD-Metamodel

## Idee
Bisher ließ sich die Vertraulichkeitsanalyse [^Analyse] nur mit Palladiomodellen [^Palldio] nutzen. Um die Nutzung ohne Kentnnisse von Palladio zu unterstützen wurde hier ein unabhängiges Metamodell für Datenflussdiagramme entwickelt. DFD-Metamodel implementiert zwei Metamodelle, die zum Aufbau von Datenflussdiagrammen nach [^1] dienen sollen. 

## DataFlowDiagram
Der Einstieg erfolgt über ein DataFlowDiagram, das eine Liste an Knoten und Kanten besitzt. Kanten sind über Flows dargestellt, da sie einen Datenfluss repräsentieren. Jeder Flow hat einen Destination- und SourceNode, um die Richtung des Datenflusses anzuzeigen. Knoten können entweder externe Entitäten sein (dh. Nutzer oder externe Systeme, die Daten empfangen oder senden können) oder zur Beschreibung der Systemstrutkur genutzt werden (Store und Process). Jeder Fluss hat Pins, um Input und Output der Daten festzulegen.
Knoten haben Eigenschaften in Form von Labels und ein Verhalten. Um dieses wiederverwenden zu können und Duplikation zu vermeiden, wird das Behaviour in einem eigenen Metamodell modelliert: im DataDictionary.

## DataDictionary
In einem DataDictonairy werden dann eine Menge an Verhalten und LabelTypes mit den jeweiligen Instanziierungen durch Label gespeichert. So kann ein einziges DataDictionary für mehrere DFDs wiederverwendet werden.
Ein Verhalten kann mehrere Assignments haben, die durch Terme ausgedrückt werden. Ein Term wird rekursiv aufgebaut. Es gibt die klassischen binären Operatoren: and und or, die dann jeweils zwei weitere Terme verknüpfen. Außerdem gibt es NOT zum Negieren eines Terms und die Möglichkeit Referenzen auf Label zu geben. So lässt sich über logische Ausdrücke, Konstante und Referenzen eine Menge an outputLables berechnen.     
Pins werden innerhalb eines Flusses zur Weitergabe der Label genutzt. Der aufrufende Knoten gibt die Label über den inputPin weiter. Dh. Die Ids der InputPin und des outputPin des vorherigen Knoten müssen also übereinstimmen. Ein Fluss transportiert also alle Label, die am outputpin des Quellknoten anliegen und gibt sie weiter an den inputPin des Zielknoten. Diese Label werden durch den Term im Knoten dann „verarbeitet“. 

## Anleitung Nutzung
1. Das Repo klonen.
2. Die Modelle mit Eclipse öffnen.
3. Eine neue Konfiguartion unter Eclipse Application anlegen und diese ausführen: Es öffnet sich eine weitere Eclipse Instanz
4. Unter New und Other nach dem Modell suchen (z.B. dataflowdiagram Model) und dieses auswählen
5. Name vergeben
6. Model Object wählen: quasi der Einstiegspunkt in das Diagramm
7. Über die "Selection"-View das Diagramm erstellen

[^Analyse]: https://github.com/PalladioSimulator/Palladio-Addons-DataFlowConfidentiality-Analysis
[^Palldio]: https://github.com/PalladioSimulator
[^1]: SEIFERMANN, Stephan, et al. A Unified Model to Detect Information Flow and Access Control Violations in Software Architectures. SECRYPT, 2021, 21. Jg., S. 26-37.
