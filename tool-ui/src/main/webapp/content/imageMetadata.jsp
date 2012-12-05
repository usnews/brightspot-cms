<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.StorageItem,
com.psddev.dari.util.TypeReference,

java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

Object object = Query.findById(Object.class, wp.uuidParam("id"));

Map<String, Map<String, Object>> metadata = null;

if (object != null) {
    State state = State.getInstance(object);
    StorageItem item = ObjectUtils.to(StorageItem.class, state.getValue(wp.param("field")));

    metadata = ObjectUtils.to(
            new TypeReference<Map<String, Map<String, Object>>>() { },
            item.getMetadata());

    if (metadata == null) {
        metadata = ObjectUtils.to(
                new TypeReference<Map<String, Map<String, Object>>>() { },
                State.getInstance(object).getValue(wp.param("field") + ".metadata"));
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Metadata</h1>

<% if (metadata == null) { %>
    <p>Metadata is not available!</p>

<% } else { %>
    <% for (Map.Entry<String, Map<String, Object>> e1 : metadata.entrySet()) { %>
        <h2><%= wp.h(e1.getKey()) %></h1>
        <table class="table-striped"><tbody>
            <% for (Map.Entry<String, Object> e2 : e1.getValue().entrySet()) { %>
                <tr>
                    <th><%= wp.h(e2.getKey()) %></th>
                    <td><%= wp.h(e2.getValue()) %></td>
                </tr>
            <% } %>
        </tbody></table>
    <% } %>
<% } %>

<% wp.include("/WEB-INF/footer.jsp"); %>
