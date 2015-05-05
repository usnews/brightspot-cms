package com.psddev.cms.tool;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.psddev.cms.db.ToolEntity;
import com.psddev.dari.db.Query;
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

    public Query<Object> createItemsQuery() {

        Set<UUID> itemIds = new HashSet<>();

        for (SearchResultSelectionItem item : Query.
                from(SearchResultSelectionItem.class).
                where("selectionId = ?", getId()).
                selectAll()) {

            itemIds.add(item.getItemId());
        }

        return Query.fromAll().where("_id = ?", itemIds);
    }

    /**
     * Clear the SearchResultSelection by deleting all of the SearchResultSelectionItem that point to it.
     */
    public void clear() {
        Query.
            from(SearchResultSelectionItem.class).
            where("selectionId = ?", getId()).
            deleteAll();
    }

    /**
     * Adds the Object with itemId to this SearchResultSelection.
     * @param itemId the id of the Object to be added.
     */
    public boolean addItem(UUID itemId) {

        SearchResultSelectionItem item = Query.
                from(SearchResultSelectionItem.class).
                where("selectionId = ?", getId()).
                and("itemId = ?", itemId).
                first();

        if (item == null) {

            item = new SearchResultSelectionItem();

            item.setSelectionId(getId());
            item.setItemId(itemId);
            item.save();
            return true;
        }

        return false;
    }

    /**
     * Removes the Object with itemId from this SearchResultSelection.
     * @param itemId the id of the Object to be removed.
     */
    public boolean removeItem(UUID itemId) {

        SearchResultSelectionItem item = Query.
            from(SearchResultSelectionItem.class).
            where("selectionId = ?", getId()).
            and("itemId = ?", itemId).first();

        if (item == null) {
            return false;
        }

        item.delete();
        return true;
    }

    public int size() {
        return Long.valueOf(createItemsQuery().count()).intValue();
    }
}
