package com.lca.core;

import com.lca.core.dto.LCAGenerated;
import com.lca.core.dto.LCATemplate;
import spoon.reflect.CtModel;

import java.util.List;

public interface CustomAnalyzer {
    List<LCAGenerated> analyze(CtModel ctModel, int studentAssignmentId);
    String getAnalyzerName();
    List<LCATemplate> getLCATemplateList();
}
