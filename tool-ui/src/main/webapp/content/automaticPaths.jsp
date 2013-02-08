<%@ page import="

com.psddev.cms.db.Directory,
com.psddev.cms.db.Page,
com.psddev.cms.db.Template,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.Query,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.DependencyResolver,
com.psddev.dari.util.ObjectUtils,

java.util.Set,
java.util.UUID
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Object selected = wp.findOrReserve();
State selectedState = State.getInstance(selected);

Template template = null;
if (selected != null) {
    template = selectedState.as(Template.ObjectModification.class).getDefault();
}

if (selected == null) {
    return;
}

try {
    selectedState.beginWrites();
    wp.include("/WEB-INF/objectPost.jsp", "object", selected);

    String[] widgetNames = wp.params(selectedState.getId() + "/_widget");
    if (!ObjectUtils.isBlank(widgetNames)) {

        DependencyResolver<Widget> updateWidgets = new DependencyResolver<Widget>();
        for (Widget widget : wp.getTool().findPlugins(Widget.class)) {
            updateWidgets.addRequired(widget, widget.getUpdateDependencies());
        }

        for (Widget widget : updateWidgets.resolve()) {
            if (!"urls".equals(widget.getInternalName())) {
                for (String widgetName : widgetNames) {
                    if (widget.getInternalName().equals(widgetName)) {
                        widget.update(wp, selected);
                        break;
                    }
                }
            }
        }

        Page.Layout layout = (Page.Layout) request.getAttribute("layoutHack");
        if (layout != null) {
            ((Page) selected).setLayout(layout);
        }
    }

} finally {
    selectedState.endWrites();
}

template = selectedState.as(Template.ObjectModification.class).getDefault();
if (template == null) {
    return;
}

// --- Presentation ---

%><% for (Directory.Path path : template.makePaths(wp.getSite(), selected)) { %>
    <li><%= wp.h(path.getPath()) %> (<%= wp.h(path.getType()) %>)</li>
<% } %>
