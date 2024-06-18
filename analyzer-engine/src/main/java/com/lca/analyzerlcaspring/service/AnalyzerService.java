package com.lca.analyzerlcaspring.service;

import spoon.reflect.CtModel;

public interface AnalyzerService {
    void generateQuestion(CtModel ctModel, int studentAssignmentId);
}
