<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ValidationException,

com.psddev.dari.util.ObjectUtils,

java.util.ArrayList,
java.util.Collections,
java.util.List
" %><%

// --- Presentation ---

ToolPageContext wp = new ToolPageContext(pageContext);
List<Throwable> errors = wp.getErrors();
if (ObjectUtils.isBlank(errors)) {
    return;
}

wp.write("<div class=\"error message\"><ul>");
for (Throwable error : errors) {
    wp.write("<li>");

    if (error instanceof ValidationException) {
        wp.write("Please fix the field errors below and try again.");

    } else if (error instanceof IllegalArgumentException) {
        wp.write(wp.h(error.getMessage()));

    } else {
        List<Throwable> causes = new ArrayList<Throwable>();
        for (; error != null; error = error.getCause()) {
            causes.add(error);
        }
        Collections.reverse(causes);

        wp.write("There was an unexpected error!");
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

    wp.write("</li>");
}

wp.write("</ul></div>");
%>
