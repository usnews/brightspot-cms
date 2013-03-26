<%@ page import="

com.psddev.cms.db.Directory,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

java.util.List
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

Object selected = wp.findOrReserve();

if (selected == null) {
    return;
}

State selectedState = State.getInstance(selected);
Directory.ObjectModification dirData = selectedState.as(Directory.ObjectModification.class);

try {
    selectedState.beginWrites();
    wp.include("/WEB-INF/objectPost.jsp", "object", selected);
    wp.updateUsingAllWidgets(selected);

} finally {
    selectedState.endWrites();
}

List<Directory.Path> automaticPaths = (List<Directory.Path>) selectedState.getExtras().get("cms.automaticPaths");
boolean manual = Directory.PathsMode.MANUAL.equals(dirData.getPathsMode());

if ((automaticPaths != null && !automaticPaths.isEmpty()) || manual) {
    String automaticName = selectedState.getId() + "/directory.automatic";

    wp.writeStart("div", "class", "widget-urlsAutomatic");
        wp.writeStart("p");
            wp.tag("input",
                    "type", "hidden",
                    "name", automaticName,
                    "value", true);

            wp.tag("input",
                    "type", "checkbox",
                    "id", wp.createId(),
                    "name", automaticName,
                    "value", true,
                    "checked", manual ? null : "checked");

            wp.writeHtml(" ");

            wp.writeStart("label", "for", wp.getId());
                wp.writeHtml("Generate Permalink?");
            wp.writeEnd();
        wp.writeEnd();

        if (!manual) {
            wp.writeStart("ul");
                for (Directory.Path p : automaticPaths) {
                    wp.writeStart("li");
                        wp.writeHtml(p.getPath());
                        wp.writeHtml(" (");
                        wp.writeHtml(p.getType());
                        wp.writeHtml(")");
                    wp.writeEnd();
                }
            wp.writeEnd();
        }
    wp.writeEnd();
}
%>
