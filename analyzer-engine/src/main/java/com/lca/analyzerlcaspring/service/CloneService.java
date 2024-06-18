package com.lca.analyzerlcaspring.service;

public interface CloneService {
    void cloneRepo(int assignmentId, String assignmentUrl) throws Exception;
    void cleanRepo(int assignmentId) throws Exception;
}
