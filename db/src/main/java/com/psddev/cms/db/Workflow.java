package com.psddev.cms.db;

import com.psddev.dari.db.Record;

/** Valid paths between {@linkplain DraftStatus draft statuses}. */
public class Workflow extends Record {

    @Indexed(unique = true)
    @Required
    private String name;

    @Indexed
    private DraftStatus source;

    @Indexed
    private DraftStatus target;

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the source. */
    public DraftStatus getSource() {
        return source;
    }

    /** Sets the source. */
    public void setSource(DraftStatus source) {
        this.source = source;
    }

    /** Returns the target. */
    public DraftStatus getTarget() {
        return target;
    }

    /** Sets the target. */
    public void setTarget(DraftStatus target) {
        this.target = target;
    }

    /**
     * Returns the unique ID that represents this workflow for use in
     * permissions.
     */
    public String getPermissionId() {
        return "workflow/" + getId().toString();
    }
}
