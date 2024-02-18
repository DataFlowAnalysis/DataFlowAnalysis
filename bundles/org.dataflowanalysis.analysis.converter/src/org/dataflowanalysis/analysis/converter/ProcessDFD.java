package org.dataflowanalysis.analysis.converter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import org.dataflowanalysis.analysis.converter.webdfd.*;
import org.dataflowanalysis.dfd.datadictionary.*;
import org.dataflowanalysis.dfd.dataflowdiagram.*;
import org.dataflowanalysis.dfd.dataflowdiagram.Process;

public class ProcessDFD {

    private Map<Pin, String> mapInputPinToFlowName = new HashMap<>();

    public DFD parse(String dfdFile, String ddFile) {
        // Init and get resources for dfd, dd model instances
        ResourceSet rs = new ResourceSetImpl();
        rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
        rs.getPackageRegistry().put(dataflowdiagramPackage.eNS_URI, dataflowdiagramPackage.eINSTANCE);

        Resource dfdResource = rs.getResource(URI.createFileURI(dfdFile), true);
        Resource ddResource = rs.getResource(URI.createFileURI(ddFile), true);

        DataFlowDiagram dfd = (DataFlowDiagram) dfdResource.getContents().get(0);
        DataDictionary dd = (DataDictionary) ddResource.getContents().get(0);

        return parse(dfd, dd);

    }

    public DFD parse(DataFlowDiagram dfd, DataDictionary dd) {
        List<Child> children = new ArrayList<>();
        List<WebLabelType> labelTypes = new ArrayList<>();

        for (LabelType labelType : dd.getLabelTypes()) {
            List<Value> values = new ArrayList<>();
            for (Label label : labelType.getLabel()) {
                values.add(new Value(label.getId(), label.getEntityName()));
            }
            labelTypes.add(new WebLabelType(labelType.getId(), labelType.getEntityName(), values));
        }

        for (Flow flow : dfd.getFlows()) {
            String id = flow.getId();
            String type = "edge:arrow";
            String sourceId = flow.getSourcePin().getId();
            String targetId = flow.getDestinationPin().getId();
            String text = flow.getEntityName();
            mapInputPinToFlowName.put(flow.getDestinationPin(), text);
            children.add(new Child(text, null, null, id, type, sourceId, targetId, new ArrayList<>()));
        }

        for (Node node : dfd.getNodes()) {
            String text = node.getEntityName();
            String id = node.getId();
            String type;
            if (node instanceof Process) {
                type = "node:function";
            } else if (node instanceof Store) {
                type = "node:storage";
            } else if (node instanceof External) {
                type = "node:input-output";
            } else {
                type = "error";
            }

            List<WebLabel> labels = new ArrayList<>();
            for (Label label : node.getProperties()) {
                String labelTypeId = ((LabelType) label.eContainer()).getId();
                String labelId = label.getId();
                labels.add(new WebLabel(labelTypeId, labelId));
            }

            List<Port> ports = new ArrayList<>();

            for (Pin pin : node.getBehaviour().getInPin()) {
                ports.add(new Port(null, pin.getId(), "port:dfd-input", new ArrayList<>()));
            }

            Map<Pin, List<AbstractAssignment>> mapPinToAssignments = mapping(node);

            for (Pin pin : node.getBehaviour().getOutPin()) {
                String behaviour = createBehaviourString(mapPinToAssignments.get(pin));
                ports.add(new Port(behaviour, pin.getId(), "port:dfd-output", new ArrayList<>()));
            }

            children.add(new Child(text, labels, ports, id, type, null, null, new ArrayList<>()));
        }

        return new DFD(new Model("graph", "root", children), labelTypes);
    }

    public Map<Pin, List<AbstractAssignment>> mapping(Node node) {
        Map<Pin, List<AbstractAssignment>> mapPinToAssignments = new HashMap<>();

        for (AbstractAssignment assignment : node.getBehaviour().getAssignment()) {
            if (mapPinToAssignments.containsKey(assignment.getOutputPin())) {
                mapPinToAssignments.get(assignment.getOutputPin()).add(assignment);
            } else {
                List<AbstractAssignment> list = new ArrayList<>();
                list.add(assignment);
                mapPinToAssignments.put(assignment.getOutputPin(), list);
            }
        }
        return mapPinToAssignments;
    }

    public String createBehaviourString(List<AbstractAssignment> abstractAssignments) {
        StringBuilder builder = new StringBuilder();
        if (abstractAssignments != null) {
            for (AbstractAssignment abstractAssignment : abstractAssignments) {
                if (abstractAssignment instanceof ForwardingAssignment) {
                    for (Pin inPin : abstractAssignment.getInputPins()) {
                        builder.append("forward ").append(mapInputPinToFlowName.get(inPin)).append("\n");
                    }
                } else {
                    Assignment assignment = (Assignment) abstractAssignment;
                    String value = getTermValue(assignment.getTerm()) ? "TRUE" : "FALSE";

                    for (Label label : assignment.getOutputLabels()) {
                        try {
                            builder.append("set ").append(((LabelType) label.eContainer()).getEntityName()).append(".").append(label.getEntityName())
                                    .append(" = ").append(value).append("\n");
                        } catch (IllegalArgumentException ex) {
                            System.out.println(
                                    "Caution!! WebEditor cant handle complex Assignments yet. Only TRUE or NOT(TRUE) supported. Everything else ignored");
                        }
                    }
                }
            }
            return builder.toString().trim();
        }
        return null;

    }

    // Currently only supports True or False
    public boolean getTermValue(Term term) throws IllegalArgumentException {
        if (term instanceof TRUE)
            return true;
        if (term instanceof NOT) {
            return !getTermValue(((NOT) term).getNegatedTerm());
        }
        throw new IllegalArgumentException();
    }
}
