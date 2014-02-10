<%@ page session="false" import="

com.psddev.cms.db.ToolUser,
com.psddev.cms.tool.ToolPageContext,

org.joda.time.DateTimeZone
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
ToolUser user = (ToolUser) request.getAttribute("object");
String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    user.setTimeZone(wp.param(String.class, inputName));
    return;
}

wp.writeStart("div", "class", "inputSmall");
    wp.writeStart("select",
            "name", inputName,
            "id", wp.getId(),
            "data-searchable", true);
        wp.writeStart("option", "value", "");
            wp.writeHtml("Default (");
            wp.writeHtml(timeZoneIdToLabel(DateTimeZone.getDefault().getID()));
            wp.writeHtml(")");
        wp.writeEnd();

        for (String tzId : DateTimeZone.getAvailableIDs()) {
            wp.writeStart("option",
                    "selected", tzId.equals(user.getTimeZone()) ? "selected" : null,
                    "value", tzId);
                wp.writeHtml(timeZoneIdToLabel(tzId));
            wp.writeEnd();
        }
    wp.writeEnd();
wp.writeEnd();
%><%!

private static String timeZoneIdToLabel(String timeZoneId) {
    return timeZoneId.replace("/", " / ").replace('_', ' ');
}
%>
