<%@ page import="

com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

java.io.IOException
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Search search = new Search(wp);

// --- Presentation ---

new SearchResultRenderer(wp, search) {

    @Override
    protected void renderBeforeItem(Object item) throws IOException {
        ToolPageContext wp = getToolPageContext();
        wp.write("<a data-objectId=\"");
        wp.write(State.getInstance(item).getId());
        wp.write("\" href=\"");
        wp.write(wp.objectUrl("/content/enhancement.jsp", item));
        wp.write("\" target=\"_parent\">");
    }

    @Override
    protected void renderAfterItem(Object item) throws IOException {
        ToolPageContext wp = getToolPageContext();
        wp.write("</a>");
    }
}.render();
%>
