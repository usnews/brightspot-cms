<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.IoUtils,
com.psddev.dari.util.ObjectUtils,

java.io.InputStream,
java.net.URL,
java.net.URLConnection,
java.util.List,
java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

URL url = wp.param(URL.class, "url");
URLConnection urlConnection = url.openConnection();
InputStream urlInput = urlConnection.getInputStream();

try {
    for (Map.Entry<String, List<String>> entry : urlConnection.getHeaderFields().entrySet()) {
        String name = entry.getKey();
        for (String value : entry.getValue()) {
            response.addHeader(name, value);
        }
    }

    IoUtils.copy(urlInput, response.getOutputStream());

} finally {
    urlInput.close();
}

if (true) {
    return;
}
%>
