package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder;

import java.util.List;
import java.util.UUID;

import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class InterfaceBuilder {
	private OperationInterface operationInterface;
	
	
	private InterfaceBuilder() {
		this.operationInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
		this.operationInterface.setId(UUID.randomUUID().toString());
	}

	
	public InterfaceBuilder builder() {
		return new InterfaceBuilder();
	}
	
	public InterfaceBuilder setName(String name) {
		operationInterface.setEntityName(name);
		return this;
	}
	
	public InterfaceBuilder addOperation(String name) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(operationInterface);
		return this;
	}
	
	public InterfaceBuilder addOperation(String name, List<Parameter> parameter) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(operationInterface);
		signature.getParameters__OperationSignature().addAll(parameter);
		return this;
	}
	
	public InterfaceBuilder addOperation(String name, List<Parameter> parameter, DataType returnType) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(operationInterface);
		signature.getParameters__OperationSignature().addAll(parameter);
		signature.setReturnType__OperationSignature(returnType);
		return this;
	}
	
	public OperationInterface build() {
		return this.operationInterface;
	}
}
