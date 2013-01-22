<%@ page import="

com.psddev.cms.db.Section,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.Database,
com.psddev.dari.db.ObjectType,
com.psddev.dari.util.HtmlWriter,
com.psddev.dari.util.ObjectUtils,

java.util.Map,
java.util.UUID
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);
Map<String, Object> data = (Map<String, Object>) ObjectUtils.fromJson(wp.param(String.class, "data"));
ObjectType type = Database.Static.getDefault().getEnvironment().getTypeByName(ObjectUtils.to(String.class, data.get("_type")));
Section section = (Section) type.createObject(ObjectUtils.to(UUID.class, data.get("_id")));

section.getState().setValues(data);

HtmlWriter writer = new HtmlWriter(out);

section.writeLayoutPreview(writer);
%>
