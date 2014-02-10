<%@ page session="false" import="

com.psddev.cms.db.Page,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State,

com.psddev.dari.util.ObjectUtils,

java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

Page selected = (Page) request.getAttribute("object");
Page.Layout layout = selected.getLayout();
if (layout == null) {
    layout = new Page.Layout();
}

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {
    layout = Page.Layout.fromDefinition(selected, (Map<String, Object>) ObjectUtils.fromJson(wp.param(inputName)));
    request.setAttribute("layoutHack", layout);
    selected.setLayout(layout);
    return;
}

// --- Presentation ---

%><div class="inputLarge">
    <textarea class="pageLayout" data-pageid="<%= selected.getId() %>" id="<%= wp.getId() %>" name="<%= wp.h(inputName) %>"><%= wp.h(ObjectUtils.toJson(layout.toDefinition(), true)) %></textarea>
</div>
