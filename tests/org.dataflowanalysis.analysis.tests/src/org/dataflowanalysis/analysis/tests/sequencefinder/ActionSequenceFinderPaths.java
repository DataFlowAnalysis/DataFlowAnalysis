package org.dataflowanalysis.analysis.tests.sequencefinder;

import java.util.List;

import org.dataflowanalysis.analysis.core.pcm.seff.CallingSEFFActionSequenceElement;
import org.dataflowanalysis.analysis.core.pcm.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.core.pcm.user.CallingUserActionSequenceElement;
import org.dataflowanalysis.analysis.core.pcm.user.UserActionSequenceElement;

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
