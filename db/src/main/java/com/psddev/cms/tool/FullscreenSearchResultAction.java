package com.psddev.cms.tool;

import java.io.IOException;

import com.psddev.dari.util.UrlBuilder;

public class FullscreenSearchResultAction implements SearchResultAction {

    @Override
    public void writeHtml(
            ToolPageContext page,
            Search search,
            SearchResultSelection selection)
            throws IOException {

        if (selection == null) {
            page.writeStart("div", "class", "searchResult-singleAction");
                page.writeStart("a",
                        "class", "button",
                        "target", "_top",
                        "href", new UrlBuilder(page.getRequest()).
                                absolutePath(page.cmsUrl("/searchAdvancedFull")).
                                currentParameters().
                                parameter(Search.NAME_PARAMETER, null));
                    page.writeHtml("Fullscreen");
                page.writeEnd();
            page.writeEnd();
        }
    }
}
