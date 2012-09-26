<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
Object selected = Query.findById(Object.class, wp.uuidParam("id"));

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Raw Data</h1>
<pre><%= wp.h(ObjectUtils.toJson(State.getInstance(selected).getJsonObject(), true)) %></pre>

<% wp.include("/WEB-INF/footer.jsp"); %>
