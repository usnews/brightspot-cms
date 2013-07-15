package com.psddev.cms.db;

import java.util.Date;
import java.util.UUID;

import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;

public class WorkflowLog extends Record {

    @Indexed
    @ToolUi.Hidden
    private UUID objectId;

    private transient Object object;

    @Indexed
    @ToolUi.Hidden
    private Date date;

    @ToolUi.Hidden
    private String transition;

    @ToolUi.Hidden
    private String oldWorkflowState;

    @ToolUi.Hidden
    private String newWorkflowState;

    @ToolUi.Hidden
    private String userId;

    @ToolUi.Placeholder("Optional Comment")
    private String comment;

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
        this.object = null;
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

    /**
     * Returns the object.
     *
     * @return May be {@code null}.
     */
    public Object getObject() {
        if (object == null) {
            object = Query.from(Object.class).where("_id = ?", getObjectId()).first();
        }

        return object;
    }

    /**
     * Sets the object.
     *
     * @param object May be {@code null}.
     */
    public void setObject(Object object) {
        if (object != null) {
            setObjectId(State.getInstance(object).getId());
        }

        this.object = object;
    }

    /**
     * Returns the tool user that initiated the workflow transition.
     *
     * @return May be {@code null}.
     */
    public ToolUser getUser() {
        return Query.from(ToolUser.class).where("_id = ?", getUserId()).first();
    }

    /**
     * Returns the name of the user that initiated the workflow transition.
     *
     * @return Never blank.
     */
    public String getUserName() {
        ToolUser user = getUser();
        String name = user != null ? user.getLabel() : getUserId();

        return ObjectUtils.isBlank(name) ? "Unknown User" : name;
    }
}
