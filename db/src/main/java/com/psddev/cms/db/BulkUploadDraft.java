package com.psddev.cms.db;

import com.psddev.dari.db.Modification;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.State;

@BulkUploadDraft.FieldInternalNamePrefix("cms.bulkUpload.")
public class BulkUploadDraft extends Modification<Object> {

    private Boolean container;

    @Indexed(visibility = true)
    private String containerId;

    public boolean isContainer() {
        return Boolean.TRUE.equals(container);
    }

    public void setContainer(boolean container) {
        this.container = container ? Boolean.TRUE : null;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @Override
    protected void afterSave() {
        State state = getState();

        if (!state.isVisible() ||
                !isContainer()) {
            return;
        }

        for (Object item : Query.
                fromAll().
                where("cms.bulkUpload.containerId = ?", state.getId().toString()).
                selectAll()) {
            State itemState = State.getInstance(item);

            itemState.as(BulkUploadDraft.class).setContainerId(null);
            itemState.saveImmediately();
        }

        setContainer(false);
        saveImmediately();
    }

    @Override
    protected void afterDelete() {
        State state = getState();

        if (state.isVisible()) {
            return;
        }

        for (Object item : Query.
                fromAll().
                where("cms.bulkUpload.containerId = ?", state.getId().toString()).
                selectAll()) {
            State.getInstance(item).deleteImmediately();
        }
    }
}
