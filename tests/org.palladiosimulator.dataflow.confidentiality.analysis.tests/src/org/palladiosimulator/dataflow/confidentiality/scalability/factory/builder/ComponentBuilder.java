package org.palladiosimulator.dataflow.confidentiality.scalability.factory.builder;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.ImplementationComponentType;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class ComponentBuilder {
	private Repository repository;
	private ImplementationComponentType component;
	
	private ComponentBuilder(ImplementationComponentType component, Repository repository) {
		this.component = component;
		this.repository = repository;
		this.component.setId(UUID.randomUUID().toString());
		this.component.setRepository__RepositoryComponent(repository);
		this.repository.getComponents__Repository().add(component);
	}
	
	public static ComponentBuilder basicComponent(Repository repository) {
		BasicComponent component = RepositoryFactory.eINSTANCE.createBasicComponent();
		return new ComponentBuilder(component, repository);
	}
	
	public ComponentBuilder setName(String name) {
		this.component.setEntityName(name);
		return this;
	}
	
	public ComponentBuilder provideInterface(OperationInterface operationInterface) {
		OperationProvidedRole operationProvidedRole = RepositoryFactory.eINSTANCE.createOperationProvidedRole();
		operationProvidedRole.setProvidedInterface__OperationProvidedRole(operationInterface);
		component.getProvidedRoles_InterfaceProvidingEntity().add(operationProvidedRole);
		return this;
	}

}