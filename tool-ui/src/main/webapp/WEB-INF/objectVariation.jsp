<%@ page session="false" import="

com.psddev.cms.db.Variation,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.ObjectType,
com.psddev.dari.db.Query,
com.psddev.dari.db.State,

java.util.Iterator,
java.util.List,
java.util.Set,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Object object = request.getAttribute("object");
State state = State.getInstance(object);

if (state.isNew()) {
    return;
}

ObjectType type = state.getType();
List<Variation> variations = Variation.Static.getApplicable(type);

if (variations.isEmpty()) {
    return;
}

UUID selectedId = wp.param(UUID.class, "variationId");
Variation selected = null;

for (Variation variation : variations) {
    if (variation.getId().equals(selectedId)) {
        selected = variation;
        break;
    }
}

PageWriter writer = wp.getWriter();

writer.start("a",
        "class", "icon icon-object-variation",
        "target", "objectVariation-" + state.getId(),
        "href", wp.returnableUrl("/content/variations.jsp",
                "typeId", type != null ? type.getId() : null,
                "variationId", selected != null ? selected.getId() : null));
    if (selected != null) {
        writer.objectLabel(selected);
    } else {
        writer.html("Original");
    }
writer.end();
%>
