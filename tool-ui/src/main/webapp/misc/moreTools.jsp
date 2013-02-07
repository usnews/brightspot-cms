<%@ page import="

com.psddev.cms.db.Guide,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.util.JspUtils,
com.psddev.dari.util.StringUtils,

java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

List<Guide> guides = Query.from(Guide.class).selectAll();

if (wp.requireUser()) {
    return;
}

// --- Presentation ---

%><h1>More Tools</h1>
<% if (guides != null && !guides.isEmpty()) { %>
<p>
	Production Guides:<br>
	<% for (Guide guide : guides) {
	  wp.write("<li><a target=\"_blank\" href=\"", wp.url("/content/guide.jsp", "guideId", guide.getId()), "\">", guide.getTitle(), "</a></li>");
	 } %>
</p>
<% } %>
<p>
    Bookmarklet:<br>
    <a href="javascript:<%= StringUtils.encodeUri("(function(){document.body.appendChild(document.createElement('script')).src='" + JspUtils.getAbsolutePath(application, request, "/content/bookmarklet.jsp") + "';}());") %>">Brightspot</a>
</p>
