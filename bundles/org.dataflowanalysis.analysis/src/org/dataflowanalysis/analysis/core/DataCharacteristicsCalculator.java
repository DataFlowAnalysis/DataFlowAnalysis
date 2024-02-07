package org.dataflowanalysis.analysis.core;

import java.util.List;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;

public interface DataCharacteristicsCalculator {
  /**
   * Evaluate a Variable Characterization with the current Variables and update the internal state of the characteristics calculator.
   * This method should be called for each Variable Characterization (e.g. Stoex)
   * <p>
   * For easier use, the state of characteristics at a given sequence element, is managed and updated by calling this method.
   * The final DataflowVariables for an element are accessed with {@link getCalculatedVariables}.
   *
   * @param variableCharacterisation
   *            Variable Characterization at the Sequence Element
   */
  public void evaluate(ConfidentialityVariableCharacterisation variableCharacterisation);

  /**
   * Returns the list of DataFlowVariables that were calculated according to the
   * VariableCharacterizations provided
   *
   * @return List of DataFlowVariables after evaluating
   */
  List<DataFlowVariable> getCalculatedCharacteristics();
}
