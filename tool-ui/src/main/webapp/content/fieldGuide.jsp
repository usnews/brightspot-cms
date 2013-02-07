<%@ page
    import="com.psddev.cms.db.GuideType,
    com.psddev.cms.db.PageFilter,
    com.psddev.cms.tool.ToolPageContext,
    com.psddev.cms.db.Content,
    com.psddev.dari.db.ReferentialText,
    com.psddev.dari.db.ObjectField,
    com.psddev.dari.db.State,
    java.util.AbstractList"%>
<%@ taglib prefix="cms" uri="http://psddev.com/cms"%>
<jsp:useBean id="description" class="com.psddev.dari.db.ReferentialText"
    scope="request" />
<%
    // --- Logic ---

    ToolPageContext wp = new ToolPageContext(pageContext);
    if (wp.requirePermission("area/dashboard")) {
        return;
    }

    Object selected = wp.findOrReserve();
    State state = State.getInstance(selected);
    String fieldName = wp.param("field");

    if (state != null) {
        ObjectField field = state.getField(fieldName);
        ReferentialText fieldDesc = GuideType.Static
                .getFieldDescription(state, fieldName);

        // --- Presentation ---
%>
<%
    wp.include("/WEB-INF/header.jsp");
%>

<h1><%=field.getLabel()%></h1>

<%
    if (field.isRequired()) {
            wp.write("<li>Required</li>");
        }
        if (field.getMaximum() != null) {
            wp.write("<li>Maximum value: ", field.getMaximum(), "</li>");
        }
        if (field.getMinimum() != null) {
            wp.write("<li>Minimum value: ", field.getMinimum(), "</li>");
        }

        if (field.getDefaultValue() != null) {
            String defaultVal = "";
            Object defaultValObj = field.getDefaultValue();
            if (defaultValObj instanceof Number || defaultValObj instanceof String || defaultValObj instanceof Character || defaultValObj instanceof Boolean) {
                defaultVal = defaultValObj.toString();
                wp.write("<li>Default value: ", defaultVal, "</li>");
            } else {
                wp.write("<li>Default value: (complex type)</li");
                     // Would like to replace above w/ability to display link to content id (if applicable)
                    //defaultVal = "<a href=\"wp.objectUrl(\"/content/edit.jsp\", field.getDefaultValue())\" target=\"_blank\">(Content) field.getDefaultValue().getLabel()</a>";
            }

        }

        if (fieldDesc != null && !fieldDesc.isEmpty()) {
            request.setAttribute("description", fieldDesc);
%><div class="guideFieldDescription">
    <cms:render value="${description}" />
</div>
<%
    }
%>

<%
    wp.include("/WEB-INF/footer.jsp");
    }

%>
