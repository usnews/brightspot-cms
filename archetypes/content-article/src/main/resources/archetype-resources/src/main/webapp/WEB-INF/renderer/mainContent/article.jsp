<%@include file="/WEB-INF/common/taglibs.jsp" %>

<cms:layout class="layout-page">

    <cms:render area="header">
    </cms:render>

    <cms:render area="nav">
    </cms:render>

    <cms:render area="main">
        <cms:layout class="layout-standard">

            <cms:render area="well">
                <article class="article main_well">
                    <div class="article_title">
                        <h1><cms:render value="${content.headline}"/></h1>
                    </div>

                    <div class="article_body">
                        <div class="article_image">
                            <cms:img src="${content.image}" height="300" cropOption="none" />
                            <c:if test="${!empty content.image.title}" >
                                <div class="cm-article_image-title"><c:out value="${content.image.title}"/></div>
                            </c:if>
                            <c:if test="${!empty content.image.caption}" >
                                <div class="cm-article_image-caption"><c:out value="${content.image.caption}"/></div>
                            </c:if>
                        </div>
                        <div class="article_bodyText">
                            <c:if test="${!empty content.subheadline}">
                                <h3 class="cm-article_intro"><c:out value="${content.subheadline}"/></h3>
                            </c:if>
                            <cms:render value="${content.body}"/>
                        </div>
                    </div>
                </article>

            </cms:render>

            <cms:render area="footer">
            </cms:render>

        </cms:layout>
    </cms:render>

</cms:layout>
