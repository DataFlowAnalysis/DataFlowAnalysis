# DFDAnalyse

## Idee
Bisher ließ sich die Vertraulichkeitsanalyse [^Analyse] nur mit Palladiomodellen [^Palldio] nutzen. Um die Nutzung ohne Kentnnisse von Palladio zu unterstützen wurde ein unabhängiges Metamodell [^DFDMetamodell] für Datenflussdiagramme entwickelt. DFD-Metamodel implementiert zwei Metamodelle, die zum Aufbau von Datenflussdiagrammen nach [^1] dienen sollen. 
Dieser Branch erweitert die Analyse so, dass Instanzen dieser Metamodelle eingelesen werden können. Durch eine Umwandlung in ActionSequences lässt sich dann die Analyse wie gewohnt aufrufen.

## Umsetzung
Der DFDLoader lädt die Modelle und reicht sie an die Analyse. Danach wird durch den DFDMapper die Datenlflussdiagramme in DFDActionSequences umgewandlet. Dafür wurde ein Algorithmus implementiert, der alle Stränge im DFD findet.
Im evaluateDataFlows werden dann die Assignments der Behaviour ausgewertet und die DataFlowVariables entsprechend befüllt. Die DataFlowVariables, die aus einem Namen und einer weiteren Liste an Characteristics bestehen, werden über die Auswertung der Assignments des Vorgängerknoten gefüllt. Characteristics entspricht einem Label aus den DFDs.

## Nutzung

    analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
    analysis.initializeAnalysis();

    var sequences = analysis.findAllSequences();
    evaluatedSequences = analysis.evaluateDataFlows(sequences);

Danach lässt sich die Analyse ganz normal aufrufen.


[^Analyse]: https://github.com/PalladioSimulator/Palladio-Addons-DataFlowConfidentiality-Analysis
[^Palldio]: https://github.com/PalladioSimulator
[^DFDMetamodell]: https://github.com/Model-Based-Data-Protection-Assessments/DFD-Metamodel/tree/main
[^1]: SEIFERMANN, Stephan, et al. A Unified Model to Detect Information Flow and Access Control Violations in Software Architectures. SECRYPT, 2021, 21. Jg., S. 26-37.
