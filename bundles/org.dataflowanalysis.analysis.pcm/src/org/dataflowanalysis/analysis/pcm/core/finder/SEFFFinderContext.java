package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.pcm.flowgraph.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.flowgraph.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.flowgraph.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;

public class SEFFFinderContext {
    private Deque<AssemblyContext> context;
    private Deque<AbstractPCMVertex<?>> callers;
    private List<Parameter> parameter;
    
    /**
     * Initializes a new SEFF Finder context with the given assembly context, Collection of callers, List of available parameter, and all discovered data stores
     * @param context Assembly context present at the SEFF element
     * @param callers List of callers that called the SEFF element
     * @param parameter List of parameters present at the SEFF element
     * @param dataStores List of data stores that were discovered while finding sequences
     */
    public SEFFFinderContext(Deque<AssemblyContext> context, Deque<AbstractPCMVertex<?>> callers, List<Parameter> parameter) {
    	this.context = context;
    	this.callers = callers;
    	this.parameter = parameter;
	}
    
    /**
     * Duplicate SEFF Finder Context given an existing context
     * @param context SEFF Finder Context that should be duplicated
     */
    public SEFFFinderContext(SEFFFinderContext context) {
    	this.context = new ArrayDeque<>(context.getContext());
    	this.callers = new ArrayDeque<>(context.getCallers());
    	this.parameter = new ArrayList<>(context.getParameter());
    }
    
    /**
     * Update parameter that are passed to the called function for a given calling PCM element
     * @param caller Calling PCM element, for which parameter shall be updated
     */
    public void updateParameterForCallerReturning(AbstractVertex<?> caller) {
    	if (caller instanceof CallingUserPCMVertex) {
    		CallingUserPCMVertex callingUserElement = (CallingUserPCMVertex) caller;
    		this.parameter = callingUserElement.getReferencedElement().getOperationSignature__EntryLevelSystemCall().getParameters__OperationSignature();
    	} else {
    		this.parameter = ((CallingSEFFPCMVertex) caller).getParameter();
    	}
    }
    
    public void replaceCallers(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
    	Deque<AbstractPCMVertex<?>> newCallers = new ArrayDeque<>();
    	while (!this.callers.isEmpty()) {
    		AbstractPCMVertex<?> element = this.callers.pop();
    		AbstractPCMVertex<?> mappedElement = isomorphism.getOrDefault(element, element);
    		newCallers.addLast(mappedElement);
    	}
    	this.callers = newCallers;
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
    public AbstractPCMVertex<?> getLastCaller() {
    	return this.callers.removeLast();
    }
    
    /**
     * Adds a new caller to the SEFF Finder context to be saved as the topmost element in the collection of callers
     * @param caller Added caller to the finder context
     */
    public void addCaller(AbstractPCMVertex<?> caller) {
    	this.callers.add(caller);
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
    public Deque<AbstractPCMVertex<?>> getCallers() {
		return callers;
	}
    
    /**
     * Returns the currently saved parameter, that are available for the current SEFF element
     * @return
     */
    public List<Parameter> getParameter() {
		return parameter;
	}
}
