<%@ page import="

com.psddev.cms.tool.ToolFilter,
com.psddev.cms.tool.ToolPageContext
" %><%

// --- logic ---
ToolPageContext wp = new ToolPageContext(pageContext);
ToolFilter.logOut(response);
wp.redirect("/");
%>
