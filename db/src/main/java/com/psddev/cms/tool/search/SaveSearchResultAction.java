package com.psddev.cms.tool.search;

import java.io.IOException;

import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;

public class SaveSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection != null) {
            return;
        }

        page.writeStart("div", "class", "searchResult-action-simple");
            page.writeStart("a",
                    "class", "button",
                    "target", "toolUserSaveSearch",
                    "href", page.cmsUrl("/toolUserSaveSearch",
                            "search", page.url("", Search.NAME_PARAMETER, null)));
                page.writeHtml(page.localize(SaveSearchResultAction.class, "action.saveSearch"));
            page.writeEnd();
        page.writeEnd();
    }
}
