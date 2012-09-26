<%@ page import="

com.psddev.cms.db.*,
com.psddev.dari.db.*,
com.psddev.dari.util.*,
com.psddev.dari.util.*,
java.io.*,
java.util.*

" %><pre><%

WebPageContext wp = new WebPageContext(pageContext);
for (Directory directory : Query.from(Directory.class).sortAscending("path").select()) {
    if (Query.from(Object.class).where(Directory.PATHS_FIELD + " ^= ?", directory.getRawPath()).first() == null) {
        wp.write(directory.getPath(), "\n");
        wp.getWriter().flush();
        directory.delete();
    }
}
wp.write("DONE!");
%></pre>
