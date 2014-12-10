package com.psddev.cms.tool;

import java.io.IOException;

public class SaveSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection == null) {
            page.writeStart("a",
                    "class", "button icon icon-action-search",
                    "target", "toolUserSaveSearch",
                    "href", page.cmsUrl("/toolUserSaveSearch",
                            "search", page.url("", Search.NAME_PARAMETER, null)));
                page.writeHtml("Save Search");
            page.writeEnd();
        }
    }
}
