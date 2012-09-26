package com.psddev.cms.db;

import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Represents a preview of an object. */
public class Preview extends Record {

    @Indexed
    private Date createDate;

    @Indexed
    private ObjectType objectType;

    @Indexed
    private UUID objectId;

    private Map<String, Object> objectValues;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date date) {
        this.createDate = date;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType type) {
        this.objectType = type;
    }

    public UUID getObjectId() {
        return objectId;
    }

    public void setObjectId(UUID id) {
        this.objectId = id;
    }

    public Map<String, Object> getObjectValues() {
        if (objectValues == null) {
            objectValues = new LinkedHashMap<String, Object>();
        }
        return objectValues;
    }

    public void setObjectValues(Map<String, Object> values) {
        this.objectValues = values;
    }

    /** Returns an object to be previewed. */
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
