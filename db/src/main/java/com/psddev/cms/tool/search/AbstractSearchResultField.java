package com.psddev.cms.tool.search;

import java.io.IOException;

import com.psddev.cms.tool.SearchResultField;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.dari.db.ObjectType;

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
