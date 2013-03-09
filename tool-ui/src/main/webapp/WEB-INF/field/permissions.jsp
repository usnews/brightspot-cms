<%@ page import="

com.psddev.cms.db.Site,
com.psddev.cms.db.Workflow,
com.psddev.cms.tool.Area,
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.Widget,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.SparseSet,

java.io.IOException,
java.util.HashSet,
java.util.List,
java.util.Set
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);

State state = State.getInstance(request.getAttribute("object"));

ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();
String fieldValue = (String) state.getValue(fieldName);
SparseSet permissions = new SparseSet(ObjectUtils.isBlank(fieldValue) ? "+/" : fieldValue);

String inputName = (String) request.getAttribute("inputName");

if ((Boolean) request.getAttribute("isFormPost")) {

    permissions.clear();
    Set<String> denieds = new HashSet<String>();
    for (String permissionId : wp.params(inputName)) {

        if (permissionId == null) {
            permissionId = "";
        }

        String parent = wp.param(inputName + "." + permissionId);
        if ("all".equals(parent)) {
            permissions.add(permissionId);
            permissions.add(permissionId + "/");

        } else if ("some".equals(parent)) {
            permissions.add(permissionId);

        } else if ("no".equals(parent)) {
            if (permissionId.length() == 0) {
                break;
            } else {
                denieds.add(permissionId + "/");
            }

        } else {
            boolean isDenied = false;
            for (String denied : denieds) {
                if (permissionId.startsWith(denied)) {
                    isDenied = true;
                    break;
                }
            }
            if (!isDenied) {
                permissions.add(permissionId);
            }
        }
    }

    state.putValue(fieldName, permissions.toString());
    return;
}

List<Workflow> workflows = Query
        .from(Workflow.class)
        .sortAscending("name")
        .select();

// --- Presentation ---

%><div class="inputSmall permissions"><ul>

<li><% writeParent(wp, permissions, "Systems", ""); %>
<ul>

<li><% writeParent(wp, permissions, "Sites", "site"); %>
<ul>
<% for (Site site : Site.findAll()) { %>
    <li><% writeChild(wp, permissions, site, site.getPermissionId()); %></li>
<% } %>
</ul>
</li>

<li><% writeParent(wp, permissions, "Areas", "area"); %>
<ul>
<% for (Area top : wp.getCmsTool().findTopAreas()) { %>
    <% if (top.hasChildren()) { %>
        <li><% writeParent(wp, permissions, top, top.getPermissionId()); %>
            <ul>
                <% for (Area child : top.findChildren()) { %>
                    <li><% writeChild(wp, permissions, child, child.getPermissionId()); %></li>
                <% } %>
            </ul>
        </li>
    <% } else { %>
        <li><% writeChild(wp, permissions, top, top.getPermissionId()); %>
    <% } %>
<% } %>
</ul>
</li>

<li><% writeParent(wp, permissions, "Widgets", "widget"); %>
<ul>
<% for (Widget widget : wp.getCmsTool().findPlugins(Widget.class)) { %>
    <li><% writeChild(wp, permissions, widget, widget.getPermissionId()); %></li>
<% } %>
</ul>
</li>

<li><% writeParent(wp, permissions, "Workflows", "workflow"); %>
<ul>
</ul>

<li><% writeParent(wp, permissions, "Types", "type"); %>
<ul>
    <%
    for (ObjectType type : wp.getDatabase().readList(Query
            .from(ObjectType.class)
            .sortAscending("name"))) {
        if (!type.isAbstract()) {
            String typePermissionId = "type/" + type.getId().toString();
            String fieldPermissionIdPrefix = "type/" + type.getId().toString() + "/field";
            %>
            <li><% writeParent(wp, permissions, type, typePermissionId); %>
                <ul>
                    <li><% writeChild(wp, permissions, "Read", typePermissionId + "/read"); %>
                    <li><% writeChild(wp, permissions, "Write", typePermissionId + "/write"); %>
                    <% for (Workflow workflow : workflows) { %>
                        <li><% writeChild(wp, permissions, workflow, typePermissionId + "/" + workflow.getPermissionId()); %></li>
                    <% } %>
                    <li><% writeChild(wp, permissions, "Publish", typePermissionId + "/publish"); %>
                    <li><% writeParent(wp, permissions, "All Fields", fieldPermissionIdPrefix); %>
                        <%--ul>
                            <%
                            for (ObjectField typeField : type.getFields()) {
                                String fieldPermissionId = fieldPermissionIdPrefix + "/" + typeField.getInternalName();
                                %>
                                <li><% writeParent(wp, permissions, typeField, fieldPermissionId); %>
                                    <ul>
                                        <li><% writeChild(wp, permissions, "Read", fieldPermissionId + "/read"); %>
                                        <li><% writeChild(wp, permissions, "Write", fieldPermissionId + "/write"); %>
                                    </ul>
                                </li>
                            <% } %>
                        </ul--%>
                    </li>
                </ul>
            </li>
        <%
        }
    }
    %>
</ul>
</li>

</ul>
</li>

</ul></div><%!

private static void writeLabel(ToolPageContext wp, Object object) throws IOException {

    if (object instanceof String) {
        wp.write(wp.h(object));

    } else if (object instanceof ObjectField) {
        wp.write(wp.h(((ObjectField) object).getLabel()));

    } else {
        wp.write(wp.objectLabel(object));
    }
}

private static void writeParent(ToolPageContext wp, Set<String> permissions, Object object, String permissionId) throws IOException {

    String inputName = (String) wp.getRequest().getAttribute("inputName");
    boolean hasSelf = permissions.contains(permissionId);
    boolean hasChildren = permissions.contains(permissionId + "/");

    wp.write("<input name=\"");
    wp.write(wp.h(inputName));
    wp.write("\" type=\"hidden\" value=\"");
    wp.write(wp.h(permissionId));

    wp.write("\"><select id=\"");
    wp.write(wp.createId());
    wp.write("\" name=\"");
    wp.write(wp.h(inputName + "." + permissionId));
    wp.write("\">");

    wp.write("<option");
    if (hasSelf && hasChildren) {
        wp.write(" selected");
    }
    wp.write(" value=\"all\">All</option>");

    wp.write("<option");
    if (hasSelf && !hasChildren) {
        wp.write(" selected");
    }
    wp.write(" value=\"some\">Some</option>");

    wp.write("<option");
    if (!hasSelf && !hasChildren) {
        wp.write(" selected");
    }
    wp.write(" value=\"no\">No</option>");

    wp.write("</select> <label for=\"");
    wp.write(wp.getId());
    wp.write("\">");
    writeLabel(wp, object);
    wp.write("</label>");
}

private static void writeChild(ToolPageContext wp, Set<String> permissions, Object object, String permissionId) throws IOException {

    wp.write("<input");
    if (permissions.contains(permissionId)) {
        wp.write(" checked");
    }
    wp.write(" id=\"");
    wp.write(wp.createId());
    wp.write("\" name=\"");
    wp.write(wp.h(wp.getRequest().getAttribute("inputName")));
    wp.write("\" type=\"checkbox\" value=\"");
    wp.write(wp.h(permissionId));

    wp.write("\"> <label for=\"");
    wp.write(wp.getId());
    wp.write("\">");
    writeLabel(wp, object);
    wp.write("</label>");
}
%>
