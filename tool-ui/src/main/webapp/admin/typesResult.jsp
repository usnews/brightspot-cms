<%@ page import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.util.ObjectUtils,

java.util.ArrayList,
java.util.LinkedHashMap,
java.util.List,
java.util.Map
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
ObjectType selected = (ObjectType) wp.findOrReserve(ObjectType.class);

List<ObjectType> abstractTypes = new ArrayList<ObjectType>();
List<ObjectType> embeddedTypes = new ArrayList<ObjectType>();
List<ObjectType> internalTypes = new ArrayList<ObjectType>();
List<ObjectType> modificationTypes = new ArrayList<ObjectType>();
List<ObjectType> usableTypes = new ArrayList<ObjectType>();

Query<ObjectType> query = Query.from(ObjectType.class).sortAscending("name");
String queryString = wp.param("query");
if (!ObjectUtils.isBlank(queryString)) {
    query.where("name ^=[c] ?", queryString);
}

for (ObjectType type : wp.getDatabase().readList(query)) {

    if (type.getSuperClassNames().contains("com.psddev.dari.db.Modification")) {
        modificationTypes.add(type);

    } else if (type.isAbstract()) {
        abstractTypes.add(type);

    } else if (type.isEmbedded()) {
        embeddedTypes.add(type);

    } else {
        String className = type.getObjectClassName();
        if (!ObjectUtils.isBlank(className)
                && (className.startsWith("com.psddev.cms")
                || className.startsWith("com.psddev.dari"))) {
            internalTypes.add(type);
        } else {
            usableTypes.add(type);
        }
    }
}

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<%
Map<String, List<ObjectType>> typesMap = new LinkedHashMap<String, List<ObjectType>>();
typesMap.put("Usable", usableTypes);
typesMap.put("Embedded", embeddedTypes);
typesMap.put("Internal", internalTypes);
typesMap.put("Abstract", abstractTypes);
typesMap.put("Modifications", modificationTypes);

boolean hasTypes = false;
for (List<ObjectType> types : typesMap.values()) {
    if (!types.isEmpty()) {
        hasTypes = true;
        break;
    }
}

if (hasTypes) {
    for (Map.Entry<String, List<ObjectType>> entry : typesMap.entrySet()) {
        List<ObjectType> types = entry.getValue();
        if (!types.isEmpty()) {
            %>
            <h2><%= wp.h(entry.getKey()) %></h2>
            <ul class="links">
                <% for (ObjectType type : types) { %>
                    <li<%= type.equals(selected) ? " class=\"selected\"" : "" %>>
                        <a href="<%= wp.objectUrl("/admin/types.jsp", type, "query", wp.param("query")) %>" target="_parent"><%
                            wp.write(wp.objectLabel(type));
                            for (ObjectType duplicate : types) {
                                if (!duplicate.equals(type)
                                        && ObjectUtils.equals(duplicate.getLabel(), type.getLabel())) {
                                    wp.write("<br><small>(");
                                    wp.write(wp.h(type.getInternalName(), "N/A"));
                                    wp.write(")</small>");
                                    break;
                                }
                            }
                        %></a>
                    </li>
                <% } %>
            </ul>
            <%
        }
    }

} else {
    %>
    <div class="warning message">
        <p>No matching items!</p>
    </div>
<% } %>

<% wp.include("/WEB-INF/footer.jsp"); %>
