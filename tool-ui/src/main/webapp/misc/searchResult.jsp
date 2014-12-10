<%@ page session="false" import="

com.psddev.cms.db.WorkStream,
com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.page.ContentSearchAdvanced,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.PaginatedResult,
com.psddev.dari.util.StringUtils,
com.psddev.dari.util.UrlBuilder,

java.util.Iterator,
java.util.Map,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

Search search = new Search(wp);

if (!wp.param(boolean.class, "widget")) {
    search.writeResultHtml(null);

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
