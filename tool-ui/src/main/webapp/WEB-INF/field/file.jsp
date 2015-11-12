<%@ page import="com.psddev.cms.tool.page.StorageItemField, com.psddev.cms.tool.ToolPageContext" %>
<%

    // Toggle between legacy StorageItemField and new StorageItemField
    ToolPageContext wp = new ToolPageContext(pageContext);
    if (wp.getCmsTool().isEnableFrontEndUploader()) {
        com.psddev.cms.tool.page.content.field.StorageItemField.processField(wp);
    } else {
        com.psddev.cms.tool.page.StorageItemField.processField(wp);
    }
%>
