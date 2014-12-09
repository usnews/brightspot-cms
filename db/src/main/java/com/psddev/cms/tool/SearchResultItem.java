package com.psddev.cms.tool;

import java.io.IOException;

import com.psddev.dari.db.State;

public class SearchResultItem {

    public void writeCheckboxHtml(ToolPageContext page, Object item) throws IOException {
        page.writeElement("input",
                "type", "checkbox",
                "name", "id",
                "value", State.getInstance(item).getId());
    }

    public void writeBeforeHtml(ToolPageContext page, Object item) throws IOException {
        page.writeStart("a",
                "href", page.toolUrl(CmsTool.class, "/content/edit.jsp",
                        "id", State.getInstance(item).getId(),
                        "search", page.url("", Search.NAME_PARAMETER, null)),
                "data-objectId", State.getInstance(item).getId(),
                "target", "_top");
    }

    public void writeAfterHtml(ToolPageContext page, Object item) throws IOException {
        page.writeEnd();
    }
}
