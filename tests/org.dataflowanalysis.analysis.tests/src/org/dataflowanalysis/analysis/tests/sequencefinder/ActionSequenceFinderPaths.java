package org.dataflowanalysis.analysis.tests.sequencefinder;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.UserActionSequenceElement;

public final class ActionSequenceFinderPaths {
	public static final List<List<Class<?>>> onlineShopPaths = 
			List.of(List.of(UserActionSequenceElement.class,
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class,
							
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class,
							UserActionSequenceElement.class),
					List.of(UserActionSequenceElement.class,
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class,
							
							CallingUserActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class, 
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingSEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							SEFFActionSequenceElement.class,
							CallingUserActionSequenceElement.class,
							UserActionSequenceElement.class));
	
	public static final List<List<Class<?>>> internationalOnlineShopPaths = 
			List.of(List.of(UserActionSequenceElement.class,
					CallingUserActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingUserActionSequenceElement.class,
					
					CallingUserActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					CallingSEFFActionSequenceElement.class, 
					CallingSEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
		            CallingSEFFActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
		            CallingUserActionSequenceElement.class,
		            UserActionSequenceElement.class));
	
	public static final List<List<Class<?>>> travelPlannerPaths = 
			List.of(List.of(
					UserActionSequenceElement.class,
					CallingUserActionSequenceElement.class, 
					SEFFActionSequenceElement.class,
					SEFFActionSequenceElement.class,
					CallingUserActionSequenceElement.class,
					
                    CallingUserActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingUserActionSequenceElement.class,
                    
                    CallingUserActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    CallingUserActionSequenceElement.class,
                    
                    CallingUserActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    SEFFActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    CallingSEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class, 
                    CallingUserActionSequenceElement.class,
                    UserActionSequenceElement.class),
            List.of(UserActionSequenceElement.class,
            		CallingUserActionSequenceElement.class, 
                    SEFFActionSequenceElement.class,
                    SEFFActionSequenceElement.class,
            		CallingUserActionSequenceElement.class,
            		UserActionSequenceElement.class));
	
	
	private ActionSequenceFinderPaths() {
		// Utility class
	}
}
