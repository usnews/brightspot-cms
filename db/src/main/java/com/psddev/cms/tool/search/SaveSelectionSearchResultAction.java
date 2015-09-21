package com.psddev.cms.tool.search;

import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;

import java.io.IOException;

public class SaveSelectionSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection == null) {
            return;
        }

        page.writeStart("div", "class", "searchResult-action-simple");
            page.writeStart("a",
                    "class", "button",
                    "target", "toolUserSaveSearch",
                    "href", page.cmsUrl("/toolUserSaveSelection",
                            "selectionId", selection.getId().toString()));
                if (page.getUser().isSavedSearchResultSelection(page.getUser().getCurrentSearchResultSelection())) {
                    page.writeHtml(page.localize(SaveSelectionSearchResultAction.class, "action.editSelection"));
                } else {
                    page.writeHtml(page.localize(SaveSelectionSearchResultAction.class, "action.saveSelection"));
                }
            page.writeEnd();
        page.writeEnd();
    }
}
