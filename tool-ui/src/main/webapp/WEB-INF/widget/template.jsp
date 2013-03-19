<%@ page import="

com.psddev.cms.db.Template,
com.psddev.cms.tool.JspWidget,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.State,

java.util.List,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = JspWidget.getOriginal(wp);
State objectState = State.getInstance(object);
Template.ObjectModification objectTemplateMod = objectState.as(Template.ObjectModification.class);

if (!Template.Static.findUsedTypes(wp.getSite()).contains(objectState.getType())) {
    return;
}

UUID objectId = objectState.getId();
String namePrefix = objectId + "/template/";
String defaultName = objectId + "default";

if (JspWidget.isUpdating(wp)) {
    objectTemplateMod.setDefault(Database.Static.findById(objectState.getDatabase(), Template.class, wp.uuidParam(defaultName)));
    return;
}

List<Template> usableTemplates = Template.Static.findUsable(object);

Template objectTemplate = objectTemplateMod.getDefault();
if (objectTemplate == null && usableTemplates.size() == 1) {
    objectTemplate = usableTemplates.get(0);
}

// --- Presentation ---

%><select name="<%= wp.h(defaultName) %>" style="width: 100%;">
    <option>- AUTOMATIC -</option>
    <% for (Template template : usableTemplates) { %>
        <option<%= template.equals(objectTemplate) ? " selected" : "" %> value="<%= template.getId() %>"><%= wp.objectLabel(template) %></option>
    <% } %>
</select>
