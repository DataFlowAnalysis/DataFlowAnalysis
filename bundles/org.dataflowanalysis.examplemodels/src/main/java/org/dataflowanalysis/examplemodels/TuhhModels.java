package org.dataflowanalysis.examplemodels;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TuhhModels {

    private static final Map<String, List<Integer>> TUHH_MODELS = ImmutableMap.<String, List<Integer>>builder()
            .put("anilallewar", List.of(0, 6, 7, 8, 9, 11, 12, 18))
            .put("apssouza22", List.of(0, 2, 4, 6, 7, 8, 12, 18))
            .put("callistaenterprise", List.of(0, 2, 6, 11, 18))
            .put("ewolff", List.of(5, 10, 12, 18))
            .put("ewolff-kafka", List.of(0, 3, 4, 5, 6, 7, 8, 9, 18))
            .put("fernandoabcampos", List.of(18))
            .put("georgwittberger", List.of(0, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 18))
            .put("jferrater", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18))
            .put("koushikkothagal", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("mdeket", List.of(5))
            .put("mudigal-technologies", List.of(0, 2, 4, 5, 7, 8, 11, 18))
            .put("rohitghatol", List.of(10, 12, 18))
            .put("spring-petclinic", List.of(0, 2, 3, 5, 6, 7, 8, 9, 18))
            .put("sqshq", List.of(0, 6, 7, 8, 9, 10, 11, 12, 18))
            .put("yidongnan", List.of(0, 2, 3, 4, 5, 6, 7, 8, 9, 18))
            .build();

    /**
     * Returns a deep copy of the {@code TUHH_MODELS} map.
     */
    public static Map<String, List<Integer>> getTuhhModels() {
        Map<String, List<Integer>> deepCopy = new HashMap<>();

        for (var entry : TUHH_MODELS.entrySet()) {
            deepCopy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return deepCopy;
    }
}
