<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@include file="/WEB-INF/common/taglibs.jsp" %>
<!DOCTYPE html>
<html lang="en">
    <head prefix="og: http://ogp.me/ns#">

        <%-- Output elements from PageStage implementors --%>
        <cms:render value="${stage.headNodes}" />

        <%-- Load Dari's grid layout resources --%>
        <% request.setAttribute("com.psddev.dari.util.HtmlGrid.gridPaths", null); %>

        <%-- TODO: Grunt or alternative method for compilation --%>
        <link rel="stylesheet/less" type="text/css" href="/assets/style/less/confidence.less" />
        <script src="/assets/script/less-1.7.0.min.js" type="text/javascript"></script>
    </head>
    <body>

        <cms:layout class="layout-page-container">

            <cms:render area="page">
                <cms:render value="${mainContent}" />
            </cms:render>

        </cms:layout>

    </body>
</html>
