<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.IoUtils,
com.psddev.dari.util.ObjectUtils,

java.io.InputStream,
java.net.URL
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requirePermission("misc")) {
    return;
}

URL url = wp.param(URL.class, "url");
InputStream urlInput = url.openStream();

try {
    response.setContentType(ObjectUtils.getContentType(url.toString()));
    IoUtils.copy(urlInput, response.getOutputStream());

} finally {
    urlInput.close();
}
%>
