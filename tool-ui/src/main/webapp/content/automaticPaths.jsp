<%@ page import="

com.psddev.cms.db.Directory,
com.psddev.cms.db.Preview,
com.psddev.cms.db.ToolUser,
com.psddev.cms.db.ToolUserAction,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,

java.util.Date,
java.util.List,
java.util.Map,
java.util.UUID
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

Preview preview = new Preview();
ToolUser user = wp.getUser();
UUID currentPreviewId = user.getCurrentPreviewId();
Map<String, Object> selectedMap = selectedState.getSimpleValues();

preview.getState().setId(currentPreviewId);
preview.setCreateDate(new Date());
preview.setObjectType(selectedState.getType());
preview.setObjectId(selectedState.getId());
preview.setObjectValues(selectedMap);
preview.setSite(wp.getSite());
preview.save();
user.saveAction(request, selected);

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
