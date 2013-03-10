<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

java.util.Date
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.include("/WEB-INF/objectSave.jsp")) {
} else if (wp.tryDelete(request.getAttribute("object"))) {
}
%>
