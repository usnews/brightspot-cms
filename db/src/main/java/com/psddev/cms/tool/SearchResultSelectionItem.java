package com.psddev.cms.tool;

import java.util.UUID;

import com.psddev.dari.db.Record;

public class SearchResultSelectionItem extends Record {

    @Indexed
    private UUID selectionId;

    @Indexed
    private UUID itemId;

    public UUID getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(UUID selectionId) {
        this.selectionId = selectionId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}
