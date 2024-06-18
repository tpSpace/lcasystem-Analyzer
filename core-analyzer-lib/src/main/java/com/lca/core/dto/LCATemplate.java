package com.lca.core.dto;

import com.lca.core.entity.Level;
import com.lca.core.entity.Scope;

public class LCATemplate {
    private String template;

    private Scope scope;

    private Level level;

    public LCATemplate(String template, Scope scope, Level level) {
        this.template = template;
        this.scope = scope;
        this.level = level;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
