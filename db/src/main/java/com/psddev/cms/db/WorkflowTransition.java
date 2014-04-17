package com.psddev.cms.db;

public class WorkflowTransition {

    private String name;
    private String displayName;
    private WorkflowState source;
    private WorkflowState target;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public WorkflowState getSource() {
        return source;
    }

    public void setSource(WorkflowState source) {
        this.source = source;
    }

    public WorkflowState getTarget() {
        return target;
    }

    public void setTarget(WorkflowState target) {
        this.target = target;
    }
}
