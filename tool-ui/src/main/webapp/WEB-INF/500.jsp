<%@ page import="

com.psddev.cms.tool.ToolPageContext,

java.util.ArrayList,
java.util.Collections,
java.util.List
" %><%

// --- Presentation ---

ToolPageContext wp = new ToolPageContext(pageContext);
wp.include("/WEB-INF/header.jsp");
wp.write("<div class=\"error message\">");
wp.write("<p>There was an unexpected error!</p>");

Throwable error = (Throwable) request.getAttribute("javax.servlet.error.exception");
if (error != null) {

    List<Throwable> causes = new ArrayList<Throwable>();
    for (; error != null; error = error.getCause()) {
        causes.add(error);
    }
    Collections.reverse(causes);

    wp.write("<ul class=\"exception\">");
    for (Throwable cause : causes) {
        wp.write("<li>");
        wp.write(wp.h(cause.getClass().getName()));
        wp.write(": ");
        wp.write(wp.h(cause.getMessage()));
        wp.write("<ul class=\"stackTrace\">");
        for (StackTraceElement e : cause.getStackTrace()) {
            wp.write("<li>", wp.h(e), "</li>");
        }
        wp.write("</ul></li>");
    }
    wp.write("</ul>");
}

wp.write("</div>");
wp.include("/WEB-INF/footer.jsp");
%>
