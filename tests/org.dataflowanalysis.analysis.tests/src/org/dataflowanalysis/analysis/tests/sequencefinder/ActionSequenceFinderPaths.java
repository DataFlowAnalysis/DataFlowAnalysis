package org.dataflowanalysis.analysis.tests.sequencefinder;

import java.util.List;

import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;

public final class ActionSequenceFinderPaths {
    public static final List<List<Class<?>>> onlineShopPaths = List.of(
            List.of(UserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    CallingUserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    CallingUserPCMVertex.class, UserPCMVertex.class),
            List.of(UserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    CallingUserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, CallingUserPCMVertex.class, UserPCMVertex.class));

    public static final List<List<Class<?>>> internationalOnlineShopPaths = List
            .of(List.of(UserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    CallingUserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, CallingSEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    CallingSEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class, UserPCMVertex.class));

    public static final List<List<Class<?>>> travelPlannerPaths = List.of(
            List.of(UserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class,
                    CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class,
                    SEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class,
                    CallingUserPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class,
                    CallingUserPCMVertex.class, SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class,
                    SEFFPCMVertex.class, CallingSEFFPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class,
                    UserPCMVertex.class),
            List.of(UserPCMVertex.class, CallingUserPCMVertex.class, SEFFPCMVertex.class, SEFFPCMVertex.class, CallingUserPCMVertex.class,
                    UserPCMVertex.class));

    private ActionSequenceFinderPaths() {
        // Utility class
    }
}
