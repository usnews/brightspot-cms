package com.psddev.cms.tool;

import com.psddev.dari.db.ObjectType;

import java.io.IOException;

public class TaxonSearchResultRenderer extends SearchResultRenderer {

    public TaxonSearchResultRenderer(ToolPageContext page, com.psddev.cms.tool.Search search) throws IOException {

        super(page, search);

        ObjectType selectedType = search.getSelectedType();
    }
}
