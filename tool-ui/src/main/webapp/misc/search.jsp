<%@ page session="false" import="

com.psddev.dari.db.Query,
com.psddev.dari.util.UrlBuilder,

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.SearchResultSelection,

java.util.ArrayList,
java.util.Collections,
java.util.List,
java.util.Map, com.psddev.dari.util.ObjectUtils, com.psddev.cms.tool.Search, java.util.UUID, java.util.Set" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

ToolUser user = wp.getUser();
Map<String, String> savedSearches = user.getSavedSearches();

wp.writeStart("div", "class", "toolSearchSaved");

    // Saved searches

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

    // Saved selections

    Map<String, String> savedSelections = user.getSavedSelections();
    wp.writeStart("h2");
    wp.writeHtml("Saved Selections");
    wp.writeEnd();

    if (savedSelections.isEmpty()) {
        wp.writeStart("div", "class", "message");
        wp.writeHtml("No saved selections yet.");
        wp.writeEnd();

    } else {
        List<String> selectionNames = new ArrayList<String>(savedSelections.keySet());

        Collections.sort(selectionNames, String.CASE_INSENSITIVE_ORDER);

        wp.writeStart("ul", "class", "links");
        for (String selectionName : selectionNames) {
            String selectionId = savedSelections.get(selectionName);

            SearchResultSelection selection = Query.from(SearchResultSelection.class).where("_id = ?", selectionId).first();

            if (selection == null) {
                continue;
            }

            wp.writeStart("li");
            wp.writeStart("a",
                    "target", "searchResultActions",
                    "href", new UrlBuilder(request).
                            currentScheme().
                            currentHost().
                            path(wp.cmsUrl("/searchResultActions")).
                            parameter("search", ObjectUtils.toJson(new Search(wp, (Set<UUID>) null).getState().getSimpleValues())).
                            parameter("action", "activate").
                            parameter("selectionId", selectionId));
            wp.writeHtml(selectionName);
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
