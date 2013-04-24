<%@ page import="

com.psddev.cms.tool.ToolPageContext
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

wp.writeHeader();
    wp.writeStart("div", "class", "widget");
        wp.writeStart("h1",
                "class", "icon icon-action-preview");
            wp.writeHtml("oEmbed Preview");
        wp.writeEnd();

        wp.writeStart("iframe",
                "src", wp.cmsUrl("/content/oEmbedPreviewFrame.jsp", "url", wp.param(String.class, "url")),
                "style", wp.cssString(
                        "border-style", "none",
                        "width", "100%"));
        wp.writeEnd();
    wp.writeEnd();
wp.writeFooter();
%>
