<%@include file="/WEB-INF/common/taglibs.jsp" %>

<cms:layout class="layout-page">

    <cms:render area="header">
    </cms:render>

    <cms:render area="nav">
    </cms:render>

    <cms:render area="main">
        <cms:layout class="layout-standard">

            <cms:render area="well">
                <article class="cm-article cm-main_well">
                    <div class="cm-article_title">
                        <h1><cms:render value="${content.headline}"/></h1>
                    </div>

                    <div class="cm-article_body">
                        <div class="cm-article_image">
                            <cms:img src="${content.image}" height="300" cropOption="none" />
                            <c:if test="${!empty content.image.title}" >
                                <div class="cm-article_image-title">${content.image.title}</div>
                            </c:if>
                            <c:if test="${!empty content.image.caption}" >
                                <div class="cm-article_image-caption">${content.image.caption}</div>
                            </c:if>
                        </div>
                        <div class="cm-article_bodyText">
                            <c:if test="${!empty content.subheadline}">
                                <h3 class="cm-article_intro">${content.subheadline}</h3>
                            </c:if>
                            <cms:render value="${content.body}" />
                        </div>
                        <c:if test="${!empty content.tags}">
                            <ul class="cm-article_tags">
                                <span>TAGS:</span>
                                <c:forEach items="${content.tags}" var="tag">
                                    <li><cms:a href="${tag}">${tag.name}</cms:a></li>
                                </c:forEach>
                            </ul>
                        </c:if>
                    </div>
                </article>

                <div class="cm-article_relatedStories">

                </div>
            </cms:render>

            <cms:render area="footer">
            </cms:render>

        </cms:layout>
    </cms:render>

</cms:layout>
