<%@include file="/WEB-INF/common/taglibs.jsp" %>
<c:choose>
    <c:when test="${reference.rte.alignment == 'left'}">
        <c:set var="alignmentClass" value="left-align" />
    </c:when>
    <c:when test="${reference.rte.alignment == 'right'}">
        <c:set var="alignmentClass" value="right-align" />
    </c:when>
    <c:otherwise>
        <c:set var="alignmentClass" value="full" />
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${!empty reference.rte.imageSize}">
        <c:set var="size" value="${reference.rte.imageSize}" />
    </c:when>
    <c:otherwise>
        <c:set var="size" value="enhancement-sm" />
    </c:otherwise>
</c:choose>

<figure class="img-enhancement ${alignmentClass}">

    <cms:img src="${content}" title="${content.title}" alt="${content.altText}" align="${reference.rte.alignment}" size="${size}"/>

    <c:if test="${not empty content.caption}">
        <figcaption class="caption">
            <c:if test="${!empty content.caption}">
                <c:out value="${content.caption}" />
            </c:if>
        </figcaption>
    </c:if>

</figure>
