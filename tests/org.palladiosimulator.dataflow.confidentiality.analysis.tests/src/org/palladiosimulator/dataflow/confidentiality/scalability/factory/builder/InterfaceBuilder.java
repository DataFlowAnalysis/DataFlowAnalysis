package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.palladiosimulator.pcm.repository.DataType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class InterfaceBuilder {
	private Repository repository;
	private OperationInterface operationInterface;
	
	
	private InterfaceBuilder(Repository repository) {
		this.repository = repository;
		this.operationInterface = RepositoryFactory.eINSTANCE.createOperationInterface();
		this.operationInterface.setId(UUID.randomUUID().toString());
		this.operationInterface.setRepository__Interface(repository);
		this.repository.getInterfaces__Repository().add(operationInterface);
	}

	
	public static InterfaceBuilder builder(Repository repository) {
		return new InterfaceBuilder(repository);
	}
	
	public InterfaceBuilder setName(String name) {
		operationInterface.setEntityName(name);
		return this;
	}
	
	public OperationSignature addOperation(String name) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(this.operationInterface);
		return signature;
	}
	
	public OperationSignature addOperation(String name, Map<String, DataType> parameterData) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(operationInterface);
		for(Entry<String, DataType> entry : parameterData.entrySet()) {
			Parameter param =  RepositoryFactory.eINSTANCE.createParameter();
			param.setParameterName(entry.getKey());
			param.setDataType__Parameter(entry.getValue());
			signature.getParameters__OperationSignature().add(param);
		}
		return signature;
	}
	
	public OperationSignature addOperation(String name, List<Parameter> parameter, DataType returnType) {
		OperationSignature signature = RepositoryFactory.eINSTANCE.createOperationSignature();
		signature.setId(UUID.randomUUID().toString());
		signature.setEntityName(name);
		signature.setInterface__OperationSignature(operationInterface);
		signature.getParameters__OperationSignature().addAll(parameter);
		signature.setReturnType__OperationSignature(returnType);
		return signature;
	}
	
	public OperationInterface build() {
		return this.operationInterface;
	}
}
