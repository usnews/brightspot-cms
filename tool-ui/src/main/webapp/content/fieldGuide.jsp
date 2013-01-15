<%@ page
	import="com.psddev.cms.db.GuideType,com.psddev.cms.db.PageFilter,com.psddev.cms.tool.ToolPageContext,com.psddev.dari.db.ReferentialText,com.psddev.dari.db.ObjectField,com.psddev.dari.db.State"%>
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

<h1>
	Field:
	<%=field.getLabel()%></h1>

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
			wp.write("<li>Default value: ", field.getDefaultValue(), "</li>");
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
