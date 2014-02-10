<%@ page session="false" import="

com.psddev.cms.db.Content,
com.psddev.cms.db.Directory,
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
                    "data-permalink", item instanceof Content ?
                            ((Content) item).getPermalink() :
                            State.getInstance(item).as(Directory.ObjectModification.class).getPermalink());
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

        $page.delegate('[data-permalink]', 'click', function() {
            var $source = $page.popup('source'),
                    $input = $source.parent().find('.rte-dialogLinkHref'),
                    $link = $(this);

            $input.val($link.attr('data-permalink'));
            $input.change();
            $page.popup('close');
            return false;
        });
    })(window, jQuery);
</script>
