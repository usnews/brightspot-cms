package com.psddev.cms.db;

import com.psddev.dari.util.ObjectUtils;

public class WorkflowState {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;

        } else if (other instanceof WorkflowState) {
            return ObjectUtils.equals(getName(), ((WorkflowState) other).getName());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
