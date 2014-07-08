<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/common/taglibs.jsp" %>
<!DOCTYPE html>
<html lang="en">
    <head prefix="og: http://ogp.me/ns#">

        <%-- Output elements from PageStage implementors --%>
        <cms:render value="${stage.headNodes}" />

        <%-- Load Dari's grid layout resources --%>
        <% request.setAttribute("com.psddev.dari.util.HtmlGrid.gridPaths", null); %>
    </head>
    <body>

        <cms:layout class="layout-page-container">

            <cms:render area="page">
                <cms:render value="${mainContent}" />
            </cms:render>

        </cms:layout>

    </body>
</html>
