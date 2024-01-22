package org.palladiosimulator.dataflow.confidentiality.analysis.sequencefinder;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.CallingSEFFVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.DatabaseVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.SEFFVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.user.CallingUserVertex;

public final class ActionSequenceFinderPaths {
	public static final List<List<Class<?>>> onlineShopPaths = 
			List.of(List.of(CallingUserVertex.class, 
							SEFFVertex.class,
							CallingSEFFVertex.class,
							SEFFVertex.class,
							SEFFVertex.class,
							CallingSEFFVertex.class,
							SEFFVertex.class, 
							CallingUserVertex.class,
							
							CallingUserVertex.class, 
							SEFFVertex.class,
							SEFFVertex.class,
							CallingSEFFVertex.class, 
							SEFFVertex.class,
							CallingSEFFVertex.class,
							CallingUserVertex.class),
					List.of(CallingUserVertex.class, 
							SEFFVertex.class,
							CallingSEFFVertex.class,
							SEFFVertex.class,
							SEFFVertex.class,
							CallingSEFFVertex.class,
							SEFFVertex.class, 
							CallingUserVertex.class,
							
							CallingUserVertex.class, 
							SEFFVertex.class,
							SEFFVertex.class,
							CallingSEFFVertex.class, 
							SEFFVertex.class,
							SEFFVertex.class,
							CallingSEFFVertex.class,
							CallingUserVertex.class));
	
	public static final List<List<Class<?>>> internationalOnlineShopPaths = 
			List.of(List.of(
					CallingUserVertex.class, 
					SEFFVertex.class,
					CallingSEFFVertex.class,
					SEFFVertex.class, 
					SEFFVertex.class,
					CallingSEFFVertex.class,
					SEFFVertex.class, 
					CallingUserVertex.class,
					
					CallingUserVertex.class, 
					SEFFVertex.class,
					CallingSEFFVertex.class,
					SEFFVertex.class,
					CallingSEFFVertex.class, 
					CallingSEFFVertex.class,
					SEFFVertex.class,
		            CallingSEFFVertex.class, 
		            CallingUserVertex.class));
	
	public static final List<List<Class<?>>> travelPlannerPaths = 
			List.of(List.of(
					CallingUserVertex.class, 
					DatabaseVertex.class,
					CallingUserVertex.class,
					
                    CallingUserVertex.class, 
                    SEFFVertex.class,
                    CallingSEFFVertex.class,
                    SEFFVertex.class,
                    CallingSEFFVertex.class,
                    DatabaseVertex.class,
                    CallingSEFFVertex.class,
                    SEFFVertex.class, 
                    CallingSEFFVertex.class,
                    SEFFVertex.class, 
                    CallingUserVertex.class,
                    
                    CallingUserVertex.class,
                    DatabaseVertex.class,
                    CallingUserVertex.class,
                    
                    CallingUserVertex.class, 
                    SEFFVertex.class,
                    CallingSEFFVertex.class,
                    SEFFVertex.class, 
                    SEFFVertex.class, 
                    CallingSEFFVertex.class,
                    SEFFVertex.class, 
                    CallingUserVertex.class),
            List.of(CallingUserVertex.class, 
            		DatabaseVertex.class,
            		CallingUserVertex.class));
	
	
	private ActionSequenceFinderPaths() {
		// Utility class
	}
}
