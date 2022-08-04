package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class PCMQueryUtils {

    private static final Logger logger = Logger.getLogger(PCMQueryUtils.class);

    private PCMQueryUtils() {
        // Utility class
    }

    public static Optional<Start> getStartActionOfScenarioBehavior(UsageScenario usageScenario) {
        List<Start> candidates = usageScenario.getScenarioBehaviour_UsageScenario()
            .getActions_ScenarioBehaviour()
            .stream()
            .filter(it -> it instanceof Start)
            .map(it -> (Start) it)
            .toList();

        if (candidates.size() > 1) {
            logger.warn(String.format("UsageScenario %s contains more than one start action.",
                    usageScenario.getEntityName()));
        }

        return candidates.stream()
            .findFirst();
    }

}
