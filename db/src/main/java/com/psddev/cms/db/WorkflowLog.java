package com.psddev.cms.db;

import java.util.Date;
import java.util.UUID;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;

public class WorkflowLog extends Record {

    @Indexed
    private UUID objectId;

    @Indexed
    private Date date;

    private String transition;
    private String oldWorkflowState;
    private String newWorkflowState;
    private String userId;
    private String comment;

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    public String getOldWorkflowState() {
        return oldWorkflowState;
    }

    public void setOldWorkflowState(String oldWorkflowState) {
        this.oldWorkflowState = oldWorkflowState;
    }

    public String getNewWorkflowState() {
        return newWorkflowState;
    }

    public void setNewWorkflowState(String newWorkflowState) {
        this.newWorkflowState = newWorkflowState;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ToolUser getUser() {
        return Query.from(ToolUser.class).where("_id = ?", getUserId()).first();
    }

    public String getUserName() {
        ToolUser user = getUser();
        return user != null ? user.getLabel() : getUserId();
    }
}
