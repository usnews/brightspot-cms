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
    if (typeof jQuery !== 'undefined') (function(win, $) {
        var $win = $(win),
                $page = $('#<%= pageId %>');

        $page.delegate('[data-objectId]', 'click', function() {
            var $source = $page.popup('source'),
                    $input = $source.parent().find(':input.objectId'),
                    $link = $(this),
                    $repeatable = $source.closest('.inputContainer').find('.repeatableObjectId'),
                    $sourceContainer,
                    $added;

            $input.attr('data-label', $link.text());
            $input.attr('data-preview', $link.find('img').attr('src'));
            $input.val($link.attr('data-objectId'));
            $input.change();

            if ($repeatable.length > 0) {
                $sourceContainer = $source.closest('li');

                if ($sourceContainer.nextAll('li').length === 0) {
                    $repeatable.find('.addButton').click();

                    $added = $sourceContainer.nextAll('li').eq(0);

                    if ($added.length > 0) {
                        $page.popup('source', $added.find('a.objectId-select'));
                        $win.scrollTop($win.scrollTop() + $sourceContainer.outerHeight(true));
                        return false;
                    }
                }
            }

            $page.popup('close');
            return false;
        });
    })(window, jQuery);
</script>
