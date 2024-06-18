package com.lca.analyzerlcaspring.config;

public class PathConfig {
    public static String getAssignmentPath(int studentAssignmentID){
        return "src/main/resources/gitclone/assignment-" + studentAssignmentID + "/";
    }
}
