package com.psddev.cms.db;

import com.psddev.dari.db.Record;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ContentSection extends ScriptSection {

    private Set<ObjectType> contentTypes;
    @Types({ Content.class })
    private Record content;

    public Set<ObjectType> getContentTypes() {
        if (contentTypes == null) {
            contentTypes = new TreeSet<ObjectType>();
        }
        return contentTypes;
    }

    public void setContentTypes(Set<ObjectType> types) {
        this.contentTypes = types;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = (Record) content;
    }

    @Override
    public Map<String, Object> toDefinition() {
        Map<String, Object> map = super.toDefinition();
        List<String> contentTypeIds = new ArrayList<String>();
        for (ObjectType type : getContentTypes()) {
            contentTypeIds.add(type.getId().toString());
        }
        map.put("contentTypes", contentTypeIds);
        Object content = getContent();
        if (content != null) {
            State contentState = State.getInstance(content);
            map.put("content", contentState.getId().toString());
            map.put("contentTypeLabel", contentState.getType().getLabel());
            map.put("contentLabel", contentState.getLabel());
        }
        return map;
    }
}
