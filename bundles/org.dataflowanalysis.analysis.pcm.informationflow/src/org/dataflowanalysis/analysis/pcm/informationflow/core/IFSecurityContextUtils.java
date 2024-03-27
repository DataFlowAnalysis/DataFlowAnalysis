package org.dataflowanalysis.analysis.pcm.informationflow.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dataflowanalysis.analysis.core.DataFlowVariable;

/**
 * A utility class for managing security contexts. A security context is
 * constructed in multiple layers. The topmost layer represents the active
 * security context. For each branch entered a new layer should be added. When
 * returning from the branch this layer should be removed. Note that the
 * CharacteristicValue of the topmost layer should never be lower than an other
 * layer.
 *
 */
public class IFSecurityContextUtils {

	private static final String SECURITY_CONTEXT_LAYER_PREFIX = "security_context_layer_";
	private static final String SECURITY_CONTEXT_LAYER_REGEX = "^" + SECURITY_CONTEXT_LAYER_PREFIX + "(?<number>\\d*)"
			+ "$";

	private IFSecurityContextUtils() {
	}

	/**
	 * Adds a new layer to the security context from the given dataFlowVariables.
	 * Note, the added layer does not have a CaracteristicValue.
	 * 
	 * @param dataFlowVariables the given dataFlowVariables
	 * @return the given dataFlowVariables with the modified security context
	 */
	public static List<DataFlowVariable> addSecurityContextLayer(List<DataFlowVariable> dataFlowVariables) {

		List<DataFlowVariable> updatedVariables = new ArrayList<>(dataFlowVariables);

		DataFlowVariable newActiveSecurityContext = new DataFlowVariable(
				getNameNextSecurityContextLayer(dataFlowVariables), new ArrayList<>());
		updatedVariables.add(newActiveSecurityContext);

		return updatedVariables;
	}

	/**
	 * Removes the topmost layer of the security context from the given
	 * dataFlowVariables.
	 * 
	 * @param dataFlowVariables the given dataFlowVariables
	 * @return a list with the topmost security context layer removed
	 */
	public static List<DataFlowVariable> removeSecurityContextLayer(List<DataFlowVariable> dataFlowVariables) {
		List<DataFlowVariable> updatedVariables = new ArrayList<>(dataFlowVariables);

		Optional<DataFlowVariable> activeSecurityContext = getActiveSecurityContext(dataFlowVariables);
		if (activeSecurityContext.isPresent()) {
			updatedVariables.remove(activeSecurityContext.get());
		}

		return updatedVariables;
	}

	/**
	 * Returns the active security context of the given dataFlowVariables if
	 * present.
	 * 
	 * @param dataFlowVariables the given DataFlowVariables
	 * @return the security context if present
	 */
	public static Optional<DataFlowVariable> getActiveSecurityContext(List<DataFlowVariable> dataFlowVariables) {
		return getTopLayer(dataFlowVariables);
	}

	public static String getNameNextSecurityContextLayer(List<DataFlowVariable> dataFlowVariables) {
		int nextLayerIndex = getTopLayerIndex(dataFlowVariables) + 1;
		return SECURITY_CONTEXT_LAYER_PREFIX + nextLayerIndex;
	}

	/**
	 * Returns the DataFlowVariable of the given layer from the given
	 * dataFlowVariables if present.
	 * 
	 * @param dataFlowVariables the given DataFlowVariables
	 * @param layer             the given layer
	 * @return the DataFlowVariable of the given layer
	 */
	public static Optional<DataFlowVariable> getSecurityContextLayer(List<DataFlowVariable> dataFlowVariables,
			int layer) {
		for (var dataFlowVariable : dataFlowVariables) {
			if (dataFlowVariable.getVariableName().equals(SECURITY_CONTEXT_LAYER_PREFIX + layer)) {
				return Optional.of(dataFlowVariable);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns all security context layers in the given dataFlowVariables.
	 * 
	 * @param dataFlowVariables the given dataFlowVariables
	 * @return all security context layers
	 */
	public static List<DataFlowVariable> getAllSecurityContextLayers(List<DataFlowVariable> dataFlowVariables) {
		List<DataFlowVariable> securityContexts = new ArrayList<>();
		Pattern securityLayerPattern = Pattern.compile(SECURITY_CONTEXT_LAYER_REGEX);
		for (var dataFlowVariable : dataFlowVariables) {
			Matcher matcher = securityLayerPattern.matcher(dataFlowVariable.getVariableName());
			if (matcher.matches()) {
				securityContexts.add(dataFlowVariable);
			}
		}
		return securityContexts;
	}

	/**
	 * Removes all security context layers from the given dataFlowVariables.
	 * 
	 * @param dataFlowVariables the given dataFlowVariables
	 * @return a List with all security context layers removed
	 */
	public static List<DataFlowVariable> removeSecurityContextLayers(List<DataFlowVariable> dataFlowVariables) {
		List<DataFlowVariable> variables = new ArrayList<>(dataFlowVariables);
		variables.removeAll(getAllSecurityContextLayers(dataFlowVariables));
		return variables;
	}

	private static Optional<DataFlowVariable> getTopLayer(List<DataFlowVariable> dataFlowVariables) {
		return getSecurityContextLayer(dataFlowVariables, getTopLayerIndex(dataFlowVariables));
	}

	private static int getTopLayerIndex(List<DataFlowVariable> dataFlowVariables) {
		int maxLayer = -1;
		Pattern securityLayerPattern = Pattern.compile(SECURITY_CONTEXT_LAYER_REGEX);
		for (var dataFlowVariable : dataFlowVariables) {
			Matcher matcher = securityLayerPattern.matcher(dataFlowVariable.getVariableName());
			if (matcher.matches()) {
				maxLayer = Math.max(maxLayer, Integer.parseInt(matcher.group("number")));
			}
		}
		return maxLayer;
	}

}
