<%@ page contentType="text/css" import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

CmsTool cms = wp.getCmsTool();
String defaultCss = cms.getDefaultTextOverlayCss();

if (!ObjectUtils.isBlank(defaultCss)) {
    wp.write("body.imageEditor-textOverlayInput { ");
    wp.write(defaultCss);
    wp.write(" }\n");
}

for (CmsTool.CssClassGroup group : cms.getTextCssClassGroups()) {
    String groupName = group.getInternalName();

    for (CmsTool.CssClass c : group.getCssClasses()) {
        wp.write(".cms-");
        wp.write(groupName);
        wp.write("-");
        wp.write(c.getInternalName());
        wp.write(" { ");
        wp.write(c.getCss());
        wp.write(" }\n");
    }
}
%>
