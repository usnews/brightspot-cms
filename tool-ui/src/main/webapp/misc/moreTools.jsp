<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.JspUtils,
com.psddev.dari.util.StringUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

// --- Presentation ---

%><h1>More Tools</h1>
<p>
    Bookmarklet:<br>
    <a href="javascript:<%= StringUtils.encodeUri("(function(){document.body.appendChild(document.createElement('script')).src='" + JspUtils.getAbsolutePath(application, request, "/content/bookmarklet.jsp") + "';}());") %>">Brightspot</a>
</p>
