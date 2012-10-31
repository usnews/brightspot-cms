<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils,

java.io.IOException,
java.util.Arrays,
java.util.List,
java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<div id="toolContent" class="noNav noAside layout">
    <% renderBox(wp, ObjectUtils.to(Map.class, Arrays.asList(
        "orientation", "horizontal",
        "sections", Arrays.asList(

            ObjectUtils.to(Map.class, Arrays.asList(
                "orientation", "vertical",
                "flex", 3,
                "sections", Arrays.asList(
                    "/misc/siteMap.jsp",
                    "/misc/recentActivity.jsp"
                )
            )),

            ObjectUtils.to(Map.class, Arrays.asList(
                "orientation", "vertical",
                "flex", 2,
                "sections", Arrays.asList(
                    "/misc/pageBuilder.jsp",
                    "/misc/scheduledEvents.jsp",
                    "/misc/unpublishedDrafts.jsp"
                )
            ))

        )
    ))); %>
</div>

<% wp.include("/WEB-INF/footer.jsp"); %><%!

private void renderBox(ToolPageContext wp, Object section) throws IOException {
    if (section instanceof Map) {
        Map<String, Object> sections = (Map<String, Object>) section;
        wp.write("<div class=\"section container ");
        wp.write(sections.get("orientation"));
        Number flex = (Number) sections.get("flex");
        wp.write(" \" data-flex=\"", flex != null ? flex : 1, "\">");
        for (Object b : (List<Object>) sections.get("sections")) {
            renderBox(wp, b);
        }
        wp.write("</div>");
    } else {
        wp.write("<div class=\"widget section record frame\">");
        wp.write("<a class=\"positioned\" href=\"", wp.url((String) section), "\">", wp.h(section), "</a>");
        wp.write("</div>");
    }
}
%>
