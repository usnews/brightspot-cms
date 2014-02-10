<%@ page session="false" import="

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
    public void renderBeforeItem(Object item) throws IOException {
        page.writeStart("a",
                "data-objectId", State.getInstance(item).getId(),
                "href", page.objectUrl("/content/enhancement.jsp", item, "reference", page.param(String.class, "reference")),
                "target", "_parent");
    }

    @Override
    public void renderAfterItem(Object item) throws IOException {
        page.writeEnd();
    }
}.render();
%>
