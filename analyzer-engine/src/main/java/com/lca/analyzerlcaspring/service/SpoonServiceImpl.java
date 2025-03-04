package com.lca.analyzerlcaspring.service;

import com.lca.analyzerlcaspring.config.PathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;

@Service
public class SpoonServiceImpl implements SpoonService {
    Logger LOGGER = LoggerFactory.getLogger(SpoonServiceImpl.class);

    @Override
    public CtModel buildSpoonModel(int studentAssignmentId) {
        String path = PathConfig.getAssignmentPath(studentAssignmentId);
        SpoonAPI spoonAPI = new Launcher();

        LOGGER.info("Start building Spoon model at path: {}", path);
        spoonAPI.addInputResource(path);
        CtModel ctModel = spoonAPI.buildModel();
        LOGGER.info("Finish building Spoon model: {}", ctModel.isBuildModelFinished());
        return ctModel;
    }
}
