package com.psddev.cms.tool.search;

import java.io.IOException;
import java.util.Collection;

import com.psddev.cms.tool.Search;
import com.psddev.dari.db.ObjectType;

public class GridSearchResultView extends ListSearchResultView {

    @Override
    public String getIconName() {
        return "th-large";
    }

    @Override
    public boolean isInfiniteScroll(Search search) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Grid";
    }

    @Override
    public boolean isSupported(Search search) {
        ObjectType selectedType = search.getSelectedType();

        return selectedType != null && selectedType.getPreviewField() != null;
    }

    @Override
    public boolean isPreferred(Search search) {
        return isSupported(search);
    }

    @Override
    protected void writeItemsHtml(Collection<?> items) throws IOException {
        writeImagesHtml(items);
    }
}
