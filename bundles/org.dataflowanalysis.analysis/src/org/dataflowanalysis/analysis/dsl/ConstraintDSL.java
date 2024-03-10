package org.dataflowanalysis.analysis.dsl;

public class ConstraintDSL {
    /*
     * Different states of the DSL Objects:
     * ConstraintDSL (ofData -> FirstSelectorData, ofNode -> FirstSelectorNode)
     *  - ofData
     *  - ofNode
     * FirstSelectorData (ofNode -> FirstSelectorNode, neverFlows() -> SecondSelector)
     *  - withLabel
     *  - withoutLabel
     * FirstSelectorNode (ofData -> FirstSelectorData, neverFlows() -> SecondSelector)
     *  - withCharacteristic
     *  - withoutCharacteristic
     *  - withType
     *  - withAttribute TODO: This option can be used for different attributes (e.g. Calling, Forwarding)
     * SecondSelector (toNode -> SecondSelectorNode)
     *  - toNode
     * SecondSelectorNode (create() -> !)
     *  - withLabel
     *  - withoutLabel
     *  - create
     */

    public FirstDSLNodeSelector ofNode() {
        return new FirstDSLNodeSelector();
    }

    public FirstDSLDataSelector ofData() {
        return new FirstDSLDataSelector();
    }
}
