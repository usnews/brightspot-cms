<%@ page import="

com.psddev.cms.db.History,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

History history = Query.findById(History.class, wp.uuidParam("id"));
if (wp.isFormPost()) {
    try {
        history.setName(wp.param("name"));
        history.save();
    } catch (Exception ex) {
        wp.getErrors().add(ex);
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Edit History</h1>
<% wp.include("/WEB-INF/errors.jsp"); %>
<form action="<%= wp.url("") %>" method="post">
    <div class="input">
        <label for="<%= wp.createId() %>">Name</label>
        <input id="<%= wp.getId() %>" name="name" type="text" value="<%=
                wp.h(history.getName()) %>" />
    </div>
    <div class="buttons">
        <input type="submit" value="Save" />
    </div>
</form>
