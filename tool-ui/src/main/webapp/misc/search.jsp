<%@ page import="

com.psddev.cms.tool.ToolPageContext
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requireUser()) {
    return;
}

wp.include(
        "/WEB-INF/search.jsp",
        "name", "toolHeader",
        "newJsp", "/content/edit.jsp",
        "newTarget", "_top",
        "resultJsp", "/misc/searchResult.jsp");

wp.writeStart("form",
        "method", "post",
        "action", wp.url(""),
        "style", wp.cssString(
                "margin-left", "200px"));
    wp.writeStart("h2").writeHtml("Work Stream").writeEnd();

    wp.writeTag("input",
            "type", "text",
            "name", "workStreamName",
            "placeholder", "Name");

    wp.writeHtml(" ");

    wp.writeStart("button");
        wp.writeHtml("New");
    wp.writeEnd();
wp.writeEnd();

%>
<script type="text/javascript">
    if (typeof jQuery !== 'undefined') (function(win, $, undef) {
        var $headerInput = $('.toolSearch :text');
        if (!$headerInput.val()) {
            $headerInput.val($('.frame[name="miscSearch"] .searchForm-filters .searchInput :text').val()).trigger('input');
            $headerInput[0].select();
        }
    })(window, jQuery);
</script>
