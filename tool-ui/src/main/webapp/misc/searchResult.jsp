<%@ page import="

com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Search search = new Search(wp);

// --- Presentation ---

new SearchResultRenderer(wp, search).render();
%>
