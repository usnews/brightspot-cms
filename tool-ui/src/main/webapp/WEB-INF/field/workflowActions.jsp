<%@ page import="

com.psddev.cms.db.Workflow2,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils,

java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Workflow2 workflow = (Workflow2) request.getAttribute("object");
String inputName = (String) request.getAttribute("inputName");

if (Boolean.TRUE.equals(request.getAttribute("isFormPost"))) {
    workflow.setActions((Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, inputName)));
    return;
}

wp.writeStart("div", "class", "inputLarge");
    wp.writeStart("textarea",
            "class", "workflow",
            "name", inputName,
            "data-state-final", "Published",
            "data-state-initial", "New",
            "data-state-label", "Status");
        wp.writeHtml(ObjectUtils.toJson(workflow.getActions()));
    wp.writeEnd();
wp.writeEnd();
%>
