package com.psddev.cms.db;

import com.psddev.dari.db.Database;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Unpublished object or unsaved changes to an existing object. */
public class Draft extends Record {

    private @Indexed DraftStatus status;
    private @Indexed Schedule schedule;
    private String name;
    private @Indexed ToolUser owner;
    private @Indexed @Required ObjectType objectType;
    private @Indexed @Required UUID objectId;
    private Map<String, Object> objectChanges;

    /** Returns the status. */
    public DraftStatus getStatus() {
        return status;
    }

    /** Sets the status. */
    public void setStatus(DraftStatus status) {
        this.status = status;
    }

    /** Returns the schedule. */
    public Schedule getSchedule() {
        return schedule;
    }

    /** Sets the schedule. */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the owner. */
    public ToolUser getOwner() {
        return owner;
    }

    /** Sets the owner. */
    public void setOwner(ToolUser owner) {
        this.owner = owner;
    }

    /** Returns the originating object's type. */
    public ObjectType getObjectType() {
        return objectType;
    }

    /** Sets the originating object's type ID. */
    public void setObjectType(ObjectType type) {
        this.objectType = type;
    }

    /** Returns the originating object's ID. */
    public UUID getObjectId() {
        return objectId;
    }

    /** Sets the originating object's ID. */
    public void setObjectId(UUID objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns the map of all the values to be changed on the originating
     * object.
     */
    public Map<String, Object> getObjectChanges() {
        if (objectChanges == null) {
            objectChanges = new LinkedHashMap<String, Object>();
        }
        return objectChanges;
    }

    /**
     * Sets the map of all the values to be changed on the originating
     * object.
     */
    public void setObjectChanges(Map<String, Object> values) {
        this.objectChanges = values;
    }

    /**
     * Returns a copy of the originating object with all the changes
     * applied.
     */
    public Object getObject() {

        ObjectType type = getObjectType();
        if (type == null) {
            return null;

        } else {
            UUID id = getObjectId();
            Object object = Database.Static.findById(getState().getDatabase(), Object.class, id);
            if (object == null) {
                object = type.createObject(id);
            }

            Map<String, Object> changes = getObjectChanges();
            if (changes != null) {
                State.getInstance(object).getValues().putAll(changes);
            }

            return object;
        }
    }

    /** Sets all the field values based on the given {@code object}. */
    public void setObject(Object object) {

        State state = State.getInstance(object);
        getState().setDatabase(state.getDatabase());
        setObjectType(state.getType());
        setObjectId(state.getId());

        Object oldObject = Database.Static.findById(state.getDatabase(), Object.class, state.getId());
        Map<String, Object> values = state.getValues();

        if (oldObject == null) {
            setObjectChanges(values);

        } else {
            Map<String, Object> changes = getObjectChanges();
            changes.clear();

            Map<String, Object> oldValues = State.getInstance(oldObject).getValues();
            for (Map.Entry<String, Object> e : values.entrySet()) {
                String key = e.getKey();
                Object value = e.getValue();
                // if (!ObjectUtils.equals(value, oldValues.remove(key))) {
                    changes.put(key, value);
                // }
            }

            /*
            for (String key : oldValues.keySet()) {
                changes.put(key, null);
            }
            */
        }
    }
}
