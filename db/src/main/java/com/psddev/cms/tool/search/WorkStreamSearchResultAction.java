package com.psddev.cms.tool.search;

import java.io.IOException;
import java.util.Map;

import com.psddev.cms.db.WorkStream;
import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultAction;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.page.CreateWorkStream;
import com.psddev.dari.util.ObjectUtils;

public class WorkStreamSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection != null && selection.createItemsQuery().hasMoreThan(0)) {

                page.writeStart("div", "class", "searchResult-action-simple");
                    page.writeStart("a",
                            "class", "button",
                            "href", page.cmsUrl(CreateWorkStream.PATH,
                                    "query", ObjectUtils.toJson(selection.createItemsQuery().getState().getSimpleValues()),
                                    "selectionId", selection.getId()),
                            "target", "newWorkStream");
                        page.writeHtml(page.localize(WorkStream.class, "action.newType"));
                    page.writeEnd();
                page.writeEnd();
            return;
        }

        boolean hasMissing = false;

        for (Map<String, String> value : search.getFieldFilters().values()) {
            if (ObjectUtils.to(boolean.class, value.get("m"))) {
                hasMissing = true;
                break;
            }
        }

        page.writeStart("div", "class", "searchResult-action-simple");
            page.writeStart("a",
                    "class", "button",
                    "href", page.cmsUrl(CreateWorkStream.PATH,
                            "search", ObjectUtils.toJson(search.getState().getSimpleValues()),
                            "incompleteIfMatching", hasMissing),
                    "target", "newWorkStream");
                page.writeHtml(page.localize(WorkStream.class, "action.newType"));
            page.writeEnd();
        page.writeEnd();
    }
}
