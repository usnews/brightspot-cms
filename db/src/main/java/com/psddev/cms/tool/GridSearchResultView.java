package com.psddev.cms.tool;

import java.io.IOException;
import java.util.Collection;

import com.psddev.dari.db.ObjectType;

public class GridSearchResultView extends ListSearchResultView {

    @Override
    public String getIconName() {
        return "th-large";
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
