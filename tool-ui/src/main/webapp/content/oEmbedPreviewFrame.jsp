<%@ page import="

com.psddev.cms.db.OEmbed,
com.psddev.cms.tool.ToolPageContext,

java.util.Map
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
OEmbed oEmbed = new OEmbed();

oEmbed.setUrl(wp.param(String.class, "url"));

Map<String, Object> oEmbedResponse = oEmbed.getResponse();

if (oEmbedResponse == null) {
    wp.writeStart("div", "class", "message message-error");
        wp.writeHtml("Preview not available!");
    wp.writeEnd();

} else {
    wp.write(oEmbedResponse.get("html"));
}
%>
