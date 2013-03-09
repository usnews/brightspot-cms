<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

String pageId = wp.createId();

Object object = wp.findOrReserve();
State state = State.getInstance(object);

if (wp.isFormPost()) {
    try {
        wp.include("/WEB-INF/objectPost.jsp", "object", object);
        wp.publish(object);
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/objectHeading.jsp", "object", object); %>

<p style="position: absolute; right: 15px; top: 8px;"><a class="icon icon-pencil" href="<%= wp.objectUrl("/content/edit.jsp", object) %>" target="_blank">Edit in Full</a></p>

<form action="<%= wp.objectUrl("", object) %>" enctype="multipart/form-data" id="<%= pageId %>" method="post">
    <% wp.include("/WEB-INF/errors.jsp"); %>
    <% wp.include("/WEB-INF/objectForm.jsp", "object", object); %>
    <div class="buttons">
        <button class="action action-save">Save</button>
    </div>
</form>
