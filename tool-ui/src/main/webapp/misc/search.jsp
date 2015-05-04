<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.SearchResultSelection,
com.psddev.cms.tool.Search,

java.util.ArrayList,
java.util.Collections,
java.util.List,
java.util.Map,

com.psddev.dari.db.Query" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

ToolUser user = wp.getUser();
Map<String, String> savedSearches = user.getSavedSearches();

wp.writeStart("div", "class", "toolSearchSaved");
    wp.writeStart("h2");
        wp.writeHtml("Saved Searches");
    wp.writeEnd();

    if (savedSearches.isEmpty()) {
        wp.writeStart("div", "class", "message");
            wp.writeHtml("No saved searches yet.");
        wp.writeEnd();

    } else {
        List<String> savedSearchNames = new ArrayList<String>(savedSearches.keySet());

        Collections.sort(savedSearchNames, String.CASE_INSENSITIVE_ORDER);

        wp.writeStart("ul", "class", "links");
            for (String savedSearchName : savedSearchNames) {
                String savedSearch = savedSearches.get(savedSearchName);

                wp.writeStart("li");
                    wp.writeStart("a",
                            "href", wp.url(null) + "?" + savedSearch);
                        wp.writeHtml(savedSearchName);
                    wp.writeEnd();
                wp.writeEnd();
            }
        wp.writeEnd();
    }

    Map<String, String> savedSelectionIds = user.getSavedSelections();
    wp.writeStart("h2");
    wp.writeHtml("Saved Selections");
    wp.writeEnd();

    if (savedSelectionIds.isEmpty()) {
        wp.writeStart("div", "class", "message");
        wp.writeHtml("No saved selections yet.");
        wp.writeEnd();

    } else {
        List<String> savedSelectionNames = new ArrayList<String>(savedSelectionIds.keySet());

        Collections.sort(savedSelectionNames, String.CASE_INSENSITIVE_ORDER);

        wp.writeStart("ul", "class", "links");
        for (String savedSelectionName : savedSelectionNames) {
            String savedSelectionId = savedSelectionIds.get(savedSelectionName);

            SearchResultSelection selection = Query.from(SearchResultSelection.class).where("_id = ?", savedSelectionId).first();

            if (selection == null) {
                continue;
            }

            Search search = new Search();
            search.setAdditionalPredicate(selection.createItemsQuery().getPredicate().toString());

            wp.writeStart("li");
            wp.writeStart("a",
                    "href", wp.url(null) + "?" + search);
            wp.writeHtml(savedSelectionName);
            wp.writeEnd();
            wp.writeEnd();
        }
        wp.writeEnd();
    }

wp.writeEnd();

wp.include(
        "/WEB-INF/search.jsp",
        "name", "toolHeader",
        "newJsp", "/content/edit.jsp",
        "newTarget", "_top",
        "resultJsp", "/misc/searchResult.jsp");

%>
<script type="text/javascript">
    if (typeof jQuery !== 'undefined') (function(win, $, undef) {
        var $headerInput = $('.toolSearch :text');
        if (!$headerInput.val()) {
            $headerInput.val($('.frame[name="miscSearch"] .searchFilters .searchInput :text').val()).trigger('input');
            $headerInput[0].select();
        }
    })(window, jQuery);
</script>
