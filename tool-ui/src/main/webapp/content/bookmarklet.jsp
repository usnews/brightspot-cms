<%@ page import="

com.psddev.cms.db.PageFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.JspUtils
" %><%

// --- Logic ---

ToolPageContext wp = new ToolPageContext(pageContext);
if (wp.requirePermission("area/dashboard")) {
    return;
}

// --- Presentation ---

response.setContentType("text/javascript");
%>(function() {

var existingLibrary = window.jQuery;
var myLibraryScript = document.createElement('script');
myLibraryScript.src = 'https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js';
myLibraryScript.onload = function() {
    var myLibrary = window.jQuery;
    window.jQuery = existingLibrary;
    myLibrary(function($) {

        var $body = $('body');
        var overflow = $body.css('overflow');
        $body.css('overflow', 'hidden');
        var width = $body.width();
        $body.css('overflow', overflow);

        // Create a duplicate of the current page with overlay flag on.
        var $duplicate = $('<iframe/>', {
            'src': location.href + (location.href.indexOf('?') > -1 ? '&' : '?') + '<%= PageFilter.OVERLAY_PARAMETER %>=true',
            'css': {
                'background-color': 'white',
                'border': 'none',
                'height': $(window).height(),
                'left': -10000,
                'overflow': 'auto',
                'position': 'absolute',
                'top': 0,
                'width': width
            }
        });

        $duplicate.load(function() {

            var $duplicateBody = $duplicate.contents().find('body');
            var mainObjectData = $.parseJSON($duplicateBody.find('.cms-mainObject').text());

            // Create an overlay that contains all the edit controls.
            var $overlay = $('<iframe/>', {
                'src': '<%= wp.js(JspUtils.getAbsoluteUrl(application, request, "/content/remoteOverlay.jsp")) %>' +
                        '?id=' + encodeURIComponent(mainObjectData.id) +
                        '&url' + encodeURIComponent(location.href),
                'css': {
                    'border': 'none',
                    'height': 10000,
                    'left': 0,
                    'position': 'absolute',
                    'top': 0,
                    'width': '100%',
                    'z-index': 2147483648
                }
            });

            $overlay.load(function() {
                var $overlayBody = $overlay.contents().find('body');

                $duplicateBody.find('span.cms-overlayBegin').each(function() {
                    var $begin = $(this);
                    var data = $.parseJSON($begin.text());

                    var isFound = false;
                    var minX = Number.MAX_VALUE;
                    var maxX = 0;
                    var minY = Number.MAX_VALUE;
                    var maxY = 0;
                    $begin.nextUntil('span.cms-overlayEnd').filter(':visible').each(function() {
                        var $item = $(this);
                        isFound = true;

                        var itemOffset = $item.offset();
                        var itemMinX = itemOffset.left;
                        var itemMaxX = itemMinX + $item.outerWidth();
                        var itemMinY = itemOffset.top;
                        var itemMaxY = itemMinY + $item.outerHeight();

                        if (minX > itemMinX) {
                            minX = itemMinX;
                        }
                        if (maxX < itemMaxX) {
                            maxX = itemMaxX;
                        }
                        if (minY > itemMinY) {
                            minY = itemMinY;
                        }
                        if (maxY < itemMaxY) {
                            maxY = itemMaxY;
                        }
                    });

                    if (isFound) {
                        var hasObject = data.id;
                        var $section = $('<div/>', {
                            'class': 'overlay' + (hasObject ? ' hasObject' : '') + (maxY - minY < 30 ? ' short' : ''),
                            'css': {
                                'height': maxY - minY,
                                'left': minX,
                                'position': 'absolute',
                                'top': minY,
                                'width': maxX - minX
                            }
                        });

                        var $content = $('<div/>', { 'class': 'content' });
                        var $heading = $('<h1/>', { 'text': data.sectionName || data.typeLabel + ': ' + data.label });
                        $content.append($heading);

                        if (hasObject) {
                            var $editButton = $('<a/>', {
                                'class': 'editButton',
                                'href': '<%= wp.js(JspUtils.getAbsoluteUrl(application, request, "/content/remoteOverlayEdit.jsp")) %>?id=' + data.id,
                                'target': 'contentRemoteOverlayEdit',
                                'text': 'Edit'
                            });
                            $content.append($editButton);
                        }

                        $section.append($content);
                        $overlayBody.append($section);
                    }
                });
            });

            $body.append($overlay);
        });

        $body.append($duplicate);
    });
};
document.body.appendChild(myLibraryScript)

})();
