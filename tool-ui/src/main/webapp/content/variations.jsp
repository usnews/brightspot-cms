<%@ page session="false" import="

com.psddev.cms.db.Variation,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,

java.util.Iterator,
java.util.List,
java.util.Set,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

List<Variation> variations = Variation.Static.getApplicable(ObjectType.getInstance(wp.param(UUID.class, "typeId")));
PageWriter writer = wp.getWriter();
UUID variationId = wp.param(UUID.class, "variationId");

wp.include("/WEB-INF/header.jsp");

writer.start("h1").html("Variations").end();

writer.start("ul", "class", "links");

    writer.start("li", "class", variationId == null ? "selected" : null);
        writer.start("a",
                "href", wp.returnUrl("variationId", null),
                "target", "_top");
            writer.html("Original");
        writer.end();
    writer.end();

    for (Variation variation : variations) {
        writer.start("li", "class", variation.getId().equals(variationId) ? "selected" : null);
            writer.start("a",
                    "href", wp.returnUrl("variationId", variation.getId()),
                    "target", "_top");
                writer.objectLabel(variation);
            writer.end();
        writer.end();
    }

writer.end();

wp.include("/WEB-INF/footer.jsp");
%>
