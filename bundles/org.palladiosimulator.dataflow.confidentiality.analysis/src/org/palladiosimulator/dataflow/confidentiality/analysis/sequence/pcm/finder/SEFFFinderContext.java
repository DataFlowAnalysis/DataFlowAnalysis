package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DataStore;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;

public class SEFFFinderContext {
    private Deque<AssemblyContext> context;
    private Deque<AbstractPCMActionSequenceElement<?>> callers;
    private List<Parameter> parameter;
    private List<DataStore> dataStores;
    
    /**
     * Initializes a new SEFF Finder context with the given assembly context, Collection of callers, List of available parameter, and all discovered data stores
     * @param context Assembly context present at the SEFF element
     * @param callers List of callers that called the SEFF element
     * @param parameter List of parameters present at the SEFF element
     * @param dataStores List of data stores that were discovered while finding sequences
     */
    public SEFFFinderContext(Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> parameter, List<DataStore> dataStores) {
    	this.context = context;
    	this.callers = callers;
    	this.parameter = parameter;
    	this.dataStores = dataStores;
	}
    
    /**
     * Duplicate SEFF Finder Context given an existing context
     * @param context SEFF Finder Context that should be duplicated
     */
    public SEFFFinderContext(SEFFFinderContext context) {
    	this.context = new ArrayDeque<>(context.getContext());
    	this.callers = new ArrayDeque<>(context.getCallers());
    	this.parameter = new ArrayList<>(context.getParameter());
    	this.dataStores = new ArrayList<>(context.getDataStores());
    }
    
    /**
     * Update parameter that are passed to the called function for a given calling PCM element
     * @param caller Calling PCM element, for which parameter shall be updated
     */
    public void updateParameterForCallerReturning(AbstractPCMActionSequenceElement<?> caller) {
    	if (caller instanceof CallingUserActionSequenceElement) {
    		CallingUserActionSequenceElement callingUserElement = (CallingUserActionSequenceElement) caller;
    		this.parameter = callingUserElement.getElement().getOperationSignature__EntryLevelSystemCall().getParameters__OperationSignature();
    	} else {
    		this.parameter = ((CallingSEFFActionSequenceElement) caller).getParameter();
    	}
    }
    
    /**
     * Update stored parameters directly by setting passed variables to a function
     * @param passedVariables New variables present in the finder context
     */
    public void updateParametersForCall(List<Parameter> passedVariables) {
    	this.parameter = passedVariables;
    }
    
    /**
     * Update assembly contexts with a new provided collection of assembly contexts
     * @param newContext Collection of new assembly contexts
     */
    public void updateSEFFContext(Deque<AssemblyContext> newContext) {
    	this.context = newContext;
    }
    
    /**
     * Returns the last called PCM element and removes it from the saved callers
     * @return Last called PCM element
     */
    public AbstractPCMActionSequenceElement<?> getLastCaller() {
    	return this.callers.removeLast();
    }
    
    /**
     * Adds a new caller to the SEFF Finder context to be saved as the topmost element in the collection of callers
     * @param caller Added caller to the finder context
     */
    public void addCaller(AbstractPCMActionSequenceElement<?> caller) {
    	this.callers.add(caller);
    }
    
    /**
     * Add a new known data store to the context
     * @param dataStore New known data store
     */
    public void addDataStore(DataStore dataStore) {
    	if (!this.dataStores.contains(dataStore)) {
    		this.dataStores.add(dataStore);
    	}
    }
    
    /**
     * Returns the currently saved assembly contexts, that the current SEFF element is executed in
     * @return Returns the collection of saved assembly contexts
     */
    public Deque<AssemblyContext> getContext() {
		return context;
	}
    
    /**
     * Returns the currently saved callers, that called the current SEFF element
     * @return Returns the collection of saved callers
     */
    public Deque<AbstractPCMActionSequenceElement<?>> getCallers() {
		return callers;
	}
    
    /**
     * Returns the currently saved parameter, that are available for the current SEFF element
     * @return
     */
    public List<Parameter> getParameter() {
		return parameter;
	}
    
    /**
     * Returns the list of data stores, that are currently known
     * @return List of available data stores
     */
    public List<DataStore> getDataStores() {
		return dataStores;
	}
}
