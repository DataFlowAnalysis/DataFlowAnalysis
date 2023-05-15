package org.palladiosimulator.dataflow.confidentiality.analysis.sequencefinder;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.DatabaseActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.SEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.user.CallingUserActionSequenceElement;

public final class ActionSequenceFinderPaths {
	public static final List<List<Class<?>>> onlineShopPaths = 
			List.of(List.of(CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class, 
							CallingUserActionSequenceElement.class,
							
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class),
					List.of(CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class, 
							CallingUserActionSequenceElement.class,
							
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class));
	
	public static final List<List<Class<?>>> internationalOnlineShopPaths = 
			List.of(List.of(
					CallingUserActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class, 
					CallingUserActionSequenceElement.class,
					
					CallingUserActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class, 
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
		            CallingSEFFActionSequenceElement.class, 
		            CallingUserActionSequenceElement.class));
	
	public static final List<List<Class<?>>> travelPlannerPaths = 
			List.of(List.of(
					CallingUserActionSequenceElement.class, 
					DatabaseActionSequenceElement.class,
					CallingUserActionSequenceElement.class,
					
                    CallingUserActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    DatabaseActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    CallingUserActionSequenceElement.class,
                    
                    CallingUserActionSequenceElement.class,
                    DatabaseActionSequenceElement.class,
                    CallingUserActionSequenceElement.class,
                    
                    CallingUserActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    SEFFActionSequenceElement.class, 
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    CallingUserActionSequenceElement.class),
            List.of(CallingUserActionSequenceElement.class, 
            		DatabaseActionSequenceElement.class,
            		CallingUserActionSequenceElement.class));
	
	
	private ActionSequenceFinderPaths() {
		// Utility class
	}
}
