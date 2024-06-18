package com.lca.analyzerlcaspring.service;

import spoon.reflect.CtModel;

public interface SpoonService {
    CtModel buildSpoonModel(int studentAssignmentId);
}
