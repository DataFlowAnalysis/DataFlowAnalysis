package org.dataflowanalysis.analysis.converter.microsecend;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public abstract class MicroSecEndProcess {
    @JsonProperty("name")
    protected String name;
    @JsonProperty("stereotypes")
    protected List<String> stereotypes;
    @JsonProperty("tagged_values")
    @JsonDeserialize(using = TaggedValuesDeserializer.class)
    protected Map<String, List<String>> taggedValues;

    public MicroSecEndProcess() {

    }

    public MicroSecEndProcess(String name, List<String> stereotypes, Map<String, List<String>> taggedValues) {
        this.name = name;
        this.stereotypes = stereotypes;
        this.taggedValues = taggedValues;
    }

    public String name() {
        return name;
    }

    public List<String> stereotypes() {
        return stereotypes;
    }

    public Map<String, List<String>> taggedValues() {
        return taggedValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MicroSecEndProcess other = (MicroSecEndProcess) obj;
        return this.name.equals(other.name) && this.stereotypes.equals(other.stereotypes) && this.taggedValues.equals(other.taggedValues);
    }
}