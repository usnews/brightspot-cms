<%@ page import="

com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

java.io.IOException
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Search search = new Search(wp);

// --- Presentation ---

new SearchResultRenderer(wp, search) {

    @Override
    protected void renderBeforeItem(Object item) throws IOException {
        writer.start("a",
                "data-objectId", State.getInstance(item).getId(),
                "href", page.objectUrl("/content/enhancement.jsp", item),
                "target", "_parent");
    }

    @Override
    protected void renderAfterItem(Object item) throws IOException {
        writer.end();
    }
}.render();
%>
