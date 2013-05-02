package com.psddev.cms.db;

public class WorkflowTransition {

    private String name;
    private WorkflowState source;
    private WorkflowState target;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
