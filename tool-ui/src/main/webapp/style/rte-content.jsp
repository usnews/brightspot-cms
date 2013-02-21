<%@ page import="

com.psddev.cms.tool.CmsTool,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.ObjectUtils
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

PageWriter writer = wp.getWriter();

writer.tag("!doctype html");
writer.start("html");

    writer.start("head");
        writer.start("title").end();
        writer.start("style", "type", "text/css");
            writer.css("*",
                    "font-family", "inherit",
                    "font-size", "100%",
                    "font-style", "inherit",
                    "font-weight", "inherit",
                    "text-decoration", "inherit",
                    "vertical-align", "inherit");

            writer.css("*:focus",
                    "outline-style", "none");

            writer.css("*:last-child",
                    "margin-bottom", "0");

            writer.css("a",
                    "color", "#29c",
                    "text-decoration", "underline");

            writer.css("b, strong",
                    "font-weight", "bold");

            writer.css("body",
                    "color", "#333",
                    "font-family", "'Helvetica Neue', 'Arial', sans-serif",
                    "font-size", "13px",
                    "font-style", "normal",
                    "font-weight", "normal",
                    "line-height", "16px",
                    "margin", "4px",
                    "padding", "0",
                    "vertical-align", "baseline",
                    "word-wrap", "break-word",
                    "-rte-loaded", "true");

            writer.css("body.imageEditor-textOverlayInput",
                    "word-wrap", "normal");

            writer.css("body.placeholder",
                    "color", "#888");

            writer.css("em, i",
                    "font-style", "italic");

            writer.css("ol, ul",
                    "margin", "0 0 15px 25px",
                    "padding", "0");

            writer.css("p",
                    "margin", "0 0 15px 0");

            writer.css("strike",
                    "text-decoration", "line-through");

            writer.css("sub",
                    "font-size", "0.83em",
                    "line-height", "0",
                    "vertical-align", "sub");

            writer.css("sup",
                    "font-size", "0.83em",
                    "line-height", "0",
                    "vertical-align", "super");

            writer.css("u",
                    "text-decoration", "underline");

            writer.css(".cms-textAlign-center",
                    "text-align", "center");

            writer.css(".cms-textAlign-left",
                    "text-align", "left");

            writer.css(".cms-textAlign-right",
                    "text-align", "right");

            writer.css(".enhancement",
                    "display", "block",
                    "height", "100px",
                    "margin", "1em 0",
                    "width", "100%");

            writer.css(".enhancement[data-alignment=left]",
                    "float", "left",
                    "margin-right", "1em",
                    "margin-top", "0",
                    "width", "300px");

            writer.css(".enhancement[data-alignment=right]",
                    "float", "right",
                    "margin-left", "1em",
                    "margin-top", "0",
                    "width", "300px");

            writer.css(".marker",
                    "height", "46px");

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
                    String css = c.getCss();
                    String cmsOnlyCss = c.getCmsOnlyCss();

                    wp.write(".cms-");
                    wp.write(groupName);
                    wp.write("-");
                    wp.write(c.getInternalName());
                    wp.write(" { ");

                    if (!ObjectUtils.isBlank(css)) {
                        wp.write(css);
                    }

                    if (!ObjectUtils.isBlank(cmsOnlyCss)) {
                        wp.write(cmsOnlyCss);
                    }

                    wp.write(" }\n");
                }
            }
        writer.end();
    writer.end();

    writer.start("body");
    writer.end();

writer.end();
%>
