package com.psddev.cms.tool;

import java.util.LinkedHashSet;
import java.util.Set;

import com.psddev.cms.db.ToolEntity;
import com.psddev.dari.db.Record;

public class SearchResultSelection extends Record {

    @Indexed
    private Set<ToolEntity> entities;

    public Set<ToolEntity> getEntities() {
        if (entities == null) {
            entities = new LinkedHashSet<>();
        }
        return entities;
    }

    public void setEntities(Set<ToolEntity> entities) {
        this.entities = entities;
    }
}
