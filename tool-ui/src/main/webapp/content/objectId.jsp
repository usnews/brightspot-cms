<%@ page import="

com.psddev.cms.tool.ToolPageContext
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

wp.include(
        "/WEB-INF/search.jsp",
        "newJsp", "/content/objectIdEdit.jsp",
        "resultJsp", "/content/objectIdResult.jsp");
%>
