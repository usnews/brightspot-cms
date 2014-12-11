package com.psddev.cms.tool;

import java.io.IOException;
import java.util.Map;

import com.psddev.dari.util.ObjectUtils;

public class WorkStreamSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection != null) {
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
                    "href", page.cmsUrl("/content/newWorkStream.jsp",
                            "search", ObjectUtils.toJson(search.getState().getSimpleValues()),
                            "incompleteIfMatching", hasMissing),
                    "target", "newWorkStream");
                page.writeHtml("New Work Stream");
            page.writeEnd();
        page.writeEnd();
    }
}
