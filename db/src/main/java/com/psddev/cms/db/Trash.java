package com.psddev.cms.db;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.db.ObjectType;

/** Represents a deleted object that can be restored later. */
public class Trash extends Record {

    private @Indexed Date deleteDate;
    private @Indexed ToolUser deleteUser;
    private @Indexed ObjectType objectType;
    private @Indexed UUID objectId;
    private Map<String, Object> objectValues;

    /** Creates a blank instance. */
    protected Trash() {
    }

    /** Creates an instance based on the given {@code object}. */
    public Trash(ToolUser user, Object object) {
        State objectState = State.getInstance(object);
        getState().setDatabase(objectState.getDatabase());
        this.deleteDate = new Date();
        this.deleteUser = user;
        this.objectType = objectState.getType();
        this.objectId = objectState.getId();
        this.objectValues = objectState.getValues();
    }

    /** Returns the date when the object was deleted. */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /** Returns the user that deleted the object. */
    public ToolUser getDeleteUser() {
        return deleteUser;
    }

    /** Returns the deleted object. */
    public Object getObject() {
        if (objectType == null) {
            return null;
        } else {
            Object object = objectType.createObject(objectId);
            State state = State.getInstance(object);
            if (objectValues != null) {
                state.getValues().putAll(objectValues);
            }
            return object;
        }
    }
}
