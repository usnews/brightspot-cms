<%@ page import="

com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.List
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = wp.findOrReserve();
Widget widget = Database.Static.findById(wp.getDatabase(), Widget.class, wp.uuidParam("widgetId"));

if (wp.isFormPost()) {
    try {
        widget.update(wp, object);
        wp.publish(object);
        wp.redirect("");
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1 class="icon-<%= widget.getIconName() %>"><%= wp.objectLabel(widget) %></h1>
<form action="<%= wp.url("") %>" method="post">
    <%= widget.display(wp, object) %>
    <div class="buttons">
        <input type="submit" value="Save">
    </div>
</form>

<% wp.include("/WEB-INF/footer.jsp"); %>
