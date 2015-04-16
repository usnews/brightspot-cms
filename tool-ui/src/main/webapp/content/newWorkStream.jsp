<%@ page session="false" import="
com.psddev.cms.tool.ToolPageContext,
com.psddev.cms.tool.page.CreateWorkStream" %>

<% CreateWorkStream.reallyDoService(new ToolPageContext(pageContext));%>
