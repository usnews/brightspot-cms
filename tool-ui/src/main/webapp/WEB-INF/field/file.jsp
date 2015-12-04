<%@ page import="com.psddev.cms.tool.page.StorageItemField, com.psddev.cms.tool.ToolPageContext, com.psddev.cms.tool.page.content.field.FileField" %>
<%

    // Toggle between legacy StorageItemField and new StorageItemField
    ToolPageContext wp = new ToolPageContext(pageContext);
    if (wp.getCmsTool().isEnableFrontEndUploader()) {
        FileField.processField(wp);
    } else {
        com.psddev.cms.tool.page.StorageItemField.processField(wp);
    }
%>
