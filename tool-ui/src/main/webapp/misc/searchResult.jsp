<%@ page import="

com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,
com.psddev.dari.util.StringUtils,

java.util.Iterator,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

Search search = new Search(wp);

if (!wp.param(boolean.class, "widget")) {
    new SearchResultRenderer(wp, search).render();

    boolean hasMissing = false;

    for (String value : search.getFieldFilters().values()) {
        if ("missing".equals(value)) {
            hasMissing = true;
            break;
        }
    }

    wp.writeStart("div", "class", "buttons");
        wp.writeStart("a",
                "class", "icon icon-object-workStream",
                "href", wp.cmsUrl("/content/newWorkStream.jsp",
                        "search", ObjectUtils.toJson(search.getState().getSimpleValues()),
                        "incompleteIfMatching", hasMissing),
                "target", "newWorkStream");
            wp.writeHtml("New Work Stream");
        wp.writeEnd();
    wp.writeEnd();

} else {
    HtmlWriter writer = new HtmlWriter(wp.getWriter());
    String url = wp.url("/misc/savedSearch.jsp");
    String queryString = request.getQueryString();

    if (queryString != null) {
        url += "?" + queryString;
    }

    writer.start("a",
            "class", "action action-search",
            "href", StringUtils.addQueryParameters(url, "widget", null),
            "target", "miscSavedSearch");
        writer.html("Search Result");
    writer.end();
}
%>
