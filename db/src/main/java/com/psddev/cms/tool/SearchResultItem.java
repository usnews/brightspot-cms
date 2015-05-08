package com.psddev.cms.tool;

import java.io.IOException;

import com.psddev.cms.db.ToolUser;
import com.psddev.dari.db.State;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.StringUtils;

public class SearchResultItem {

    public void writeCheckboxHtml(ToolPageContext page, Search search, Object item) throws IOException {
        ToolUser user = page.getUser();

        if (user != null && user.getCurrentSearchResultSelection() == null) {
            user.resetCurrentSelection();
        }

        String url = page.toolUrl(CmsTool.class, "/searchResultActions",
                "search", ObjectUtils.toJson(search.getState().getSimpleValues()),
                SearchResultActions.ITEM_ID_PARAMETER, State.getInstance(item).getId());

        page.writeElement("input",
                "type", "checkbox",
                "name", "id",
                "value", State.getInstance(item).getId(),
                "data-frame-target", "searchResultActions",
                "data-frame-check", StringUtils.addQueryParameters(url, SearchResultActions.ACTION_PARAMETER, SearchResultActions.ACTION_ADD),
                "data-frame-uncheck", StringUtils.addQueryParameters(url, SearchResultActions.ACTION_PARAMETER, SearchResultActions.ACTION_REMOVE));
    }

    public void writeBeforeHtml(ToolPageContext page, Search search, Object item) throws IOException {
        page.writeStart("a",
                "href", page.objectUrl("/content/edit.jsp", item,
                        "search", ObjectUtils.toJson(search.getState().getSimpleValues())),
                "data-objectId", State.getInstance(item).getId(),
                "target", "_top");
    }

    public void writeAfterHtml(ToolPageContext page, Search search, Object item) throws IOException {
        page.writeEnd();
    }
}
