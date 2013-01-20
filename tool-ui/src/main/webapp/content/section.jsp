<%@ page import="

com.psddev.cms.db.Section,
com.psddev.cms.tool.PageWriter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectType,
com.psddev.dari.db.State,
com.psddev.dari.util.ObjectUtils,

java.util.HashSet,
java.util.Map,
java.util.Set,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

if (wp.param(UUID.class, "id") == null &&
        wp.param(UUID.class, "typeId") == null) {
    wp.include("/WEB-INF/search.jsp",
            "newJsp", "/content/section.jsp",
            "resultJsp", "/content/sectionResult.jsp",
            "validTypeClass", Section.class);
    return;
}

Set<ObjectType> sectionTypes = new HashSet<ObjectType>();
ObjectType sectionType = ObjectType.getInstance(Section.class);

sectionTypes.addAll(sectionType.findConcreteTypes());
sectionTypes.remove(sectionType);

Section section = (Section) wp.findOrReserve(sectionTypes);
PageWriter writer = wp.getWriter();
boolean saving = wp.isFormPost() && wp.param(String.class, "action-save") != null;

if (saving || wp.param(String.class, "action-select") != null) {
    if (saving) {
        wp.include("/WEB-INF/objectPost.jsp", "object", section);
    }

    String bodyId = wp.createId();

    writer.start("body");
        writer.start("div", "id", bodyId).end();
        writer.start("script", "type", "text/javascript");
            writer.write("if (typeof jQuery !== 'undefined') (function($, win, undef) {");
                writer.write("var $body = $('#" + bodyId + "');");
                writer.write("$body.popup('source').closest('.section').pageLayout('definition', ");
                writer.write(ObjectUtils.toJson(section.toDefinition()));
                writer.write(");");
                writer.write("$body.popup('close');");
            writer.write("})(jQuery, window);");
        writer.end();
    writer.end();

} else {
    Map<String, Object> data = (Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, "data"));

    if (data != null) {
        section.getState().setValues(data);
    }

    writer.start("h1");
        writer.typeLabel(section);
        writer.html(": ");
        writer.start("strong");
            writer.objectLabel(section);
        writer.end();
    writer.end();

    writer.start("p");
        writer.start("a",
                "class", "action-switch",
                "href", wp.url(null));
            writer.html("Change Section");
        writer.end();
    writer.end();

    wp.include("/WEB-INF/errors.jsp");

    writer.start("form",
            "method", "post",
            "enctype", "multipart/form-data",
            "action", wp.objectUrl("", section, "typeId", section.getState().getTypeId()));

        wp.include("/WEB-INF/objectForm.jsp", "object", section);

        writer.start("div", "class", "buttons");
            writer.tag("input",
                    "type", "submit",
                    "name", "action-save",
                    "value", "Save & Continue");
        writer.end();

    writer.end();
}
%>
