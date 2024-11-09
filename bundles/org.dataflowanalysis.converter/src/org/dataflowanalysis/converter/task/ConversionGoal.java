package org.dataflowanalysis.converter.task;

public enum ConversionGoal {
    DFD("Data Flow Diagram"),
    WEB_EDITOR("WebEditor");

    private String name;

    ConversionGoal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
