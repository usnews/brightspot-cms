<%@ page session="false" import="

java.util.Collection,

com.psddev.dari.util.ObjectUtils,
com.psddev.dari.util.TypeReference,
com.psddev.cms.tool.ToolPageContext
" %><%

new ToolPageContext(pageContext).writeSomeFormFields(
        request.getAttribute("object"),
        ObjectUtils.to(new TypeReference<Collection<String>>() { }, request.getAttribute("includeFields")),
        ObjectUtils.to(new TypeReference<Collection<String>>() { }, request.getAttribute("excludeFields")));
%>
