package com.lca.core.dto;

import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;

public class LCAGenerated {
    private String generatedText;

    private String helpText;

    private Level level;

    private Scope scope;

    private int studentAssignmentId;

    public LCAGenerated(String generatedText, String helpText, Level level, Scope scope, int studentAssignmentId) {
        this.generatedText = generatedText;
        this.helpText = helpText;
        this.level = level;
        this.scope = scope;
        this.studentAssignmentId = studentAssignmentId;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public int getStudentAssignmentId() {
        return studentAssignmentId;
    }

    public void setStudentAssignmentId(int studentAssignmentId) {
        this.studentAssignmentId = studentAssignmentId;
    }
}
