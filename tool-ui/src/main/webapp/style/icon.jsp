<%@ page contentType="text/css" import="

java.util.List,
java.util.Set,

com.psddev.cms.tool.ToolPageContext
" %><%

// --- logic ---
ToolPageContext wp = new ToolPageContext(pageContext);
String prefix = request.getServletPath().replace(".jsp", "") + "/";
for (String path : (Set<String>) getServletContext().getResourcePaths(prefix)) {
    if (path.endsWith(".png")) {
        String file = path.substring(prefix.length());
        String selector = ".icon-" + file.substring(0, file.length() - 4);

        wp.write(selector);
        wp.write(" { background-image: url(icon/", file, ");");
        wp.write(" background-position: left center;");
        wp.write(" background-repeat: no-repeat; padding-left: 20px; }\n");

        wp.write(".widget > h1", selector, ":before");
        wp.write(" { content: url(icon/", file, "); float: left;");
        wp.write(" margin: 2px 6px 0 0; }\n");

        wp.write("input[type=submit].link", selector);
        wp.write(" { background-image: url(icon/", file, ");");
        wp.write(" background-position: left center;");
        wp.write(" background-repeat: no-repeat; padding-left: 20px; }\n");
    }
}
%>
