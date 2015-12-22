package com.psddev.cms.tool.widget;

import java.io.IOException;

import com.psddev.cms.tool.Search;
import com.psddev.cms.tool.SearchResultSelection;
import com.psddev.cms.tool.ToolPageContext;
import com.psddev.cms.tool.search.MixedSearchResultView;
import com.psddev.dari.db.Query;
import com.psddev.dari.util.ObjectUtils;

public class SelectionsWidget extends AbstractPaginatedResultWidget<SearchResultSelection> {

    @Override
    String getTitle(ToolPageContext page) throws IOException {
        return page.localize(SelectionsWidget.class, "title");
    }

    @Override
    Query<SearchResultSelection> getQuery(ToolPageContext page) {
        return Query.from(SearchResultSelection.class).where("");
    }

    @Override
    public void writeResultsItem(ToolPageContext page, SearchResultSelection selection) throws IOException {

        Search search = new Search();
        search.setAdditionalPredicate(selection.createItemsQuery().getPredicate().toString());
        search.setLimit(10);

        page.writeStart("td");

            page.writeStart("a",
                    "target", "_top",
                    "href", page.cmsUrl("/searchAdvancedFull",
                        "search", ObjectUtils.toJson(search.getState().getSimpleValues()),
                        "view", MixedSearchResultView.class.getCanonicalName()));
                page.writeObjectLabel(selection);

            page.writeEnd();

        page.writeEnd();
    }
}
