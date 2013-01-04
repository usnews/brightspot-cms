<%@ page import="

com.psddev.cms.db.Page,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.State,
com.psddev.dari.util.DependencyResolver,
com.psddev.dari.util.ObjectUtils,

java.util.List,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);

String[] widgetNames = wp.params(state.getId() + "/_widget");
if (!ObjectUtils.isBlank(widgetNames)) {

    DependencyResolver<Widget> updateWidgets = new DependencyResolver<Widget>();
    for (Widget widget : wp.getTool().findPlugins(Widget.class)) {
        updateWidgets.addRequired(widget, widget.getUpdateDependencies());
    }

    for (Widget widget : updateWidgets.resolve()) {
        for (String widgetName : widgetNames) {
            if (widget.getInternalName().equals(widgetName)) {
                widget.update(wp, object);
                break;
            }
        }
    }

    Page.Layout layout = (Page.Layout) request.getAttribute("layoutHack");
    if (layout != null) {
        ((Page) object).setLayout(layout);
    }
}
%>
