<%@ page import="

com.psddev.cms.tool.Search,
com.psddev.cms.tool.SearchResultRenderer,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.db.State,

java.io.IOException
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requireUser()) {
    return;
}

Search search = new Search(wp);
String pageId = wp.createId();
String removeId = wp.createId();

// --- Presentation ---

%><div id="<%= pageId %>">
    <% new SearchResultRenderer(wp, search) {

        @Override
        public void renderBeforeItem(Object item) throws IOException {
            writer.start("span",
                    "class", "link",
                    "data-objectId", State.getInstance(item).getId());
        }

        @Override
        public void renderAfterItem(Object item) throws IOException {
            writer.end();
        }
    }.render(); %>
</div>

<script type="text/javascript">
    if (typeof jQuery !== 'undefined') (function($) {
        var $page = $('#<%= pageId %>');
        var $input = $page.popup('source').parent().find(':input.objectId');

        $page.find('.link').click(function() {
            var $link = $(this);
            $input.attr('data-label', $link.text());
            $input.attr('data-preview', $link.find('img').attr('src'));
            $input.val($link.attr('data-objectId'));
            $input.change();
            $page.popup('close');
        });
    })(jQuery);
</script>
