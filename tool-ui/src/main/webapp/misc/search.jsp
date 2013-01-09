<% new com.psddev.cms.tool.ToolPageContext(pageContext).include(
        "/WEB-INF/search.jsp",
        "searchName", "toolHeader",
        "newJsp", "/content/edit.jsp",
        "newTarget", "_top",
        "resultJsp", "/misc/searchResult.jsp"); %>
<script type="text/javascript">
    if (typeof jQuery !== 'undefined') (function(win, $, undef) {
        $('.toolHeader > .search :text').val($('.frame[name="miscSearch"] .searchForm-controlsFilters .searchInput :text').val()).trigger('input');
    })(window, jQuery);
</script>
