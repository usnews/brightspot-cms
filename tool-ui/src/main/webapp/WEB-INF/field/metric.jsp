<%@ page session="false" import="

com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Metric,
com.psddev.dari.db.ObjectField,
com.psddev.dari.db.State
"%><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
State state = State.getInstance(request.getAttribute("object"));
ObjectField field = (ObjectField) request.getAttribute("field");
String fieldName = field.getInternalName();

Metric metric = (Metric) state.getValue(fieldName);

String inputName = ((String) request.getAttribute("inputName")) + "/";

double metricValue = 0d;
boolean metricEmpty = false;
if (metric != null) {
    if (metric.isEmpty()) {
        metricEmpty = true;
    } else {
        metricValue = metric.getValue();
    }
}

// --- Presentation ---
%>

<div class="inputSmall">
    <%if (metricEmpty) {%>
        None
    <%} else {%>
        <%=metricValue%>
    <%}%>
</div>
