package com.psddev.cms.tool;

import com.psddev.dari.db.ObjectType;

import java.io.IOException;

public abstract class AbstractSearchResultField implements SearchResultField {

    @Override
    public boolean isDefault(ObjectType type) {
        return false;
    }

    @Override
    public void writeTableHeaderCellHtml(ToolPageContext page) throws IOException {
        page.writeStart("th");
            page.writeHtml(getDisplayName());
        page.writeEnd();
    }
}
