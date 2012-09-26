<%@ page import="

com.psddev.cms.tool.ToolPageContext
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");

// --- Presentation ---

wp.include("/WEB-INF/objectHeading.jsp");
wp.include("/WEB-INF/objectVariation.jsp", "object", object);
wp.include("/WEB-INF/objectMessage.jsp");
wp.write("<form action=\"", wp.objectUrl("", object), "\"");
wp.write(" autocomplete=\"off\"");
wp.write(" enctype=\"multipart/form-data\"");
wp.write(" method=\"post\"");
wp.write(">");
wp.include("/WEB-INF/objectForm.jsp");
wp.include("/WEB-INF/objectActions.jsp");
wp.write("</form>");
%>
