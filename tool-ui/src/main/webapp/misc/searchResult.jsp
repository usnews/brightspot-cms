<%@ page import="

com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.PaginatedResult,

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

} else {
    HtmlWriter writer = new HtmlWriter(wp.getWriter());
    PaginatedResult<?> result = search.getResult();
    UUID id = wp.param(UUID.class, "id");
    Object selected = null;

    for (Iterator<?> i = result.getItems().iterator(); i.hasNext(); ) {
        State item = State.getInstance(i.next());

        if (item.getId().equals(id)) {
            selected = item;
            break;
        }
    }

    writer.start("ul", "class", "pagination");
        writer.start("li");
            writer.start("a",
                    "class", "action-result",
                    "href", wp.url("", "widget", null),
                    "target", "searchResult");
                writer.html("Search Result");
            writer.end();
        writer.end();
    writer.end();
}
%>
