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
    
    public SEFFFinderContext(Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> parameter, List<DataStore> dataStores) {
    	this.context = context;
    	this.callers = callers;
    	this.parameter = parameter;
    	this.dataStores = dataStores;
	}
    
    public SEFFFinderContext(SEFFFinderContext context) {
    	this.context = new ArrayDeque<>(context.getContext());
    	this.callers = new ArrayDeque<>(context.getCallers());
    	this.dataStores = new ArrayList<>(context.getDataStores());
    }
    
    public void updateParameterForCallerReturning(AbstractPCMActionSequenceElement<?> caller) {
    	if (caller instanceof CallingUserActionSequenceElement) {
    		CallingUserActionSequenceElement callingUserElement = (CallingUserActionSequenceElement) caller;
    		this.parameter = callingUserElement.getElement().getOperationSignature__EntryLevelSystemCall().getParameters__OperationSignature();
    	} else {
    		this.parameter = ((CallingSEFFActionSequenceElement) caller).getParameter();
    	}
    }
    
    public void updateParametersForCall(List<Parameter> passedVariables) {
    	this.parameter = passedVariables;
    }
    
    public void updateSEFFContext(Deque<AssemblyContext> newContext) {
    	this.context = newContext;
    }
    
    public AbstractPCMActionSequenceElement<?> getLastCaller() {
    	return this.callers.removeLast();
    }
    
    public void addCaller(AbstractPCMActionSequenceElement<?> caller) {
    	this.callers.add(caller);
    }
    
    public void addDataStore(DataStore dataStore) {
    	if (!this.dataStores.contains(dataStore)) {
    		this.dataStores.add(dataStore);
    	}
    }
    
    public Deque<AssemblyContext> getContext() {
		return context;
	}
    
    public Deque<AbstractPCMActionSequenceElement<?>> getCallers() {
		return callers;
	}
    
    public List<Parameter> getParameter() {
		return parameter;
	}
    
    public List<DataStore> getDataStores() {
		return dataStores;
	}
}
