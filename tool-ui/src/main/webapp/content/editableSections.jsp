<%@ page import="

com.psddev.cms.db.ContentSection,
com.psddev.cms.db.Page,
com.psddev.cms.db.Section,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StringUtils,

java.util.Collections,
java.util.List,
java.util.regex.Matcher
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

Page selected = null;
Matcher idMatcher = StringUtils.getMatcher(wp.returnUrl(), "(?:\\?|&amp;)id=([^&]+)");
if (idMatcher.find()) {
    selected = Query.findById(Page.class, ObjectUtils.asUuid(idMatcher.group(1)));
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Editable Sections</h1>
<ul class="links">
    <li><a href="<%= wp.returnUrl("sectionId", null) %>" target="_top">Layout</a></li>
    <%
    for (Section section : selected instanceof Page
            ? ((Page) selected).findSections()
            : Collections.<Section>emptyList()) {
        if (section instanceof ContentSection) {
            Object object = ((ContentSection) section).getContent();
            if (object != null) {
                wp.write("<li><a href=\"");
                wp.write(wp.returnUrl("sectionId", section.getId()));
                wp.write("\" target=\"_top\">Section: ");
                wp.write(wp.objectLabel(section, "Unnamed"));
                wp.write(" (");
                wp.write(wp.objectLabel(State.getInstance(object).getType()));
                wp.write(")</a></li>");
            }
        }
    }
    %>
</ul>

<% wp.include("/WEB-INF/footer.jsp"); %>
