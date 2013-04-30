<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.IoUtils,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.Settings,
com.psddev.dari.util.StringUtils,

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

if (url == null ||
        !StringUtils.hex(StringUtils.hmacSha1(Settings.getSecret(), url.toString())).equals(wp.param(String.class, "hash"))) {
    response.sendError(404);
    return;
}

URLConnection urlConnection = url.openConnection();
InputStream urlInput = urlConnection.getInputStream();

try {
    for (Map.Entry<String, List<String>> entry : urlConnection.getHeaderFields().entrySet()) {
        String name = entry.getKey();

        if (!ObjectUtils.isBlank(name)) {
            for (String value : entry.getValue()) {
                if (!ObjectUtils.isBlank(value)) {
                    response.addHeader(name, value);
                }
            }
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
