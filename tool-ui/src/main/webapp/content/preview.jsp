<%@ page import="

com.psddev.cms.db.PageFilter,
com.psddev.cms.tool.ToolPageContext
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

String previewFormId = wp.createId();
String previewTarget = wp.createId();

// --- Presentation ---

%><% wp.include("/WEB-INF/header.jsp"); %>

<h1>Preview</h1>

<form action="<%= wp.url("/content/sharePreview.jsp") %>" method="post" target="_blank">
    <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= wp.param("id") %>">
    <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
    <input type="submit" value="Share">
</form>

<form action="<%= wp.getCmsTool().getPreviewUrl() %>" id="<%= previewFormId %>" method="post" target="<%= previewTarget %>">
    <input name="<%= PageFilter.PREVIEW_ID_PARAMETER %>" type="hidden" value="<%= wp.param("id") %>">
    <input name="<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>" type="hidden">
</form>

<script type="text/javascript">
if (typeof jQuery !== 'undefined') (function($) {
    var $previewForm = $('#<%= previewFormId %>');
    var $contentForm = $previewForm.frame('source').closest('form');
    var action = $contentForm.attr('action');
    var questionAt = action.indexOf('?');
    $.ajax({
        'data': $contentForm.serialize(),
        'type': 'post',
        'url': CONTEXT_PATH + 'content/state.jsp' + (questionAt > -1 ? action.substring(questionAt) : ''),
        'complete': function(request) {
            $(':input[name=<%= PageFilter.PREVIEW_OBJECT_PARAMETER %>]').val(request.responseText);
            var $previewTarget = $('iframe[name=<%= previewTarget %>]');
            if ($previewTarget.length == 0) {
                $previewTarget = $('<iframe/>', {
                    'name': '<%= previewTarget %>',
                    'css': {
                        'border-style': 'none',
                        'margin': 0,
                        'overflow': 'hidden',
                        'padding': 0,
                        'width': '100%'
                    }
                });
                $previewForm.after($previewTarget);
            }
            var setHeightTimer;
            var setHeight = function() {
                if ($previewTarget[0]) {
                    var $body = $($previewTarget[0].contentWindow.document.body);
                    $body.css('overflow', 'hidden');
                    $previewTarget.height($body.outerHeight(true));
                } else if (setHeightTimer) {
                    clearInterval(setHeightTimer);
                    setHeightTimer = null;
                }
            };
            setHeightTimer = setInterval(setHeight, 100);
            $previewTarget.load(function() {
                setHeight();
                if (setHeightTimer) {
                    clearInterval(setHeightTimer);
                    setHeightTimer = null;
                }
            });
            $previewForm.submit();
        }
    });
})(jQuery);
</script>

<% wp.include("/WEB-INF/footer.jsp"); %>
