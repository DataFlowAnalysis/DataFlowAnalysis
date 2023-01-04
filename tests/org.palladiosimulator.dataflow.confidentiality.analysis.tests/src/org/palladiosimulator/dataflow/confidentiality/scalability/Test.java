package org.palladiosimulator.dataflow.confidentiality.scalability;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;

public class Test {
	public static void main(String[] args) {
		Resource res = new XMLResourceImpl(URI.createFileURI("file://./test"));
		Repository repo = RepositoryFactory.eINSTANCE.createRepository();
		
	}
}
