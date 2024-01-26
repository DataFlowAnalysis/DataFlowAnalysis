package org.dataflowanalysis.dfd2json.dfd;

import java.util.List;

public record DFD(Model model, List<Label>labelTypes) {}