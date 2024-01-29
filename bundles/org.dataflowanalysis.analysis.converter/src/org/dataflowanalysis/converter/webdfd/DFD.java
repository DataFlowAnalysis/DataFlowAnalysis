package org.dataflowanalysis.converter.webdfd;

import java.util.List;

public record DFD(Model model, List<Label>labelTypes) {}