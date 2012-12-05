<%@ page import="

com.psddev.cms.db.PageFilter,
com.psddev.cms.tool.ToolPageContext,

com.psddev.dari.util.JspUtils
" %><%

ToolPageContext wp = new ToolPageContext(pageContext);

if (wp.requirePermission("area/dashboard")) {
    return;
}

response.setContentType("text/javascript");

%>(function(win, undef) {

var doc = win.document,
        oldJQuery = win.jQuery,
        newJQueryScript = doc.createElement('script');

// Load a known version of jQuery without creating a conflict with possibly
// an existing version of jQuery already loaded in the page.
newJQueryScript.src = 'https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js';
newJQueryScript.onload = function() {
    var newJQuery = win.jQuery;
    win.jQuery = oldJQuery;

    newJQuery(function($) {
        var $win = $(win),
                $body = $(doc.body),
                bodyOverflow,
                bodyWidth,
                loc = win.location;

        bodyOverflow = $body.css('overflow');
        $body.css('overflow', 'hidden');
        bodyWidth = $body.width();
        $body.css('overflow', bodyOverflow);

        // Create a duplicate of the current page with overlay flag on.
        var $duplicate = $('<iframe/>', {
            'src': loc.href + (loc.href.indexOf('?') > -1 ? '&' : '?') + '<%= PageFilter.OVERLAY_PARAMETER %>=true',
            'css': {
                'background-color': 'white',
                'border': 'none',
                'height': $win.height(),
                'left': -10000,
                'overflow': 'auto',
                'position': 'absolute',
                'top': 0,
                'width': bodyWidth
            }
        });

        $duplicate.load(function() {
            var $duplicateBody = $duplicate.contents().find('body'),
                    mainObjectData = $.parseJSON($duplicateBody.find('.cms-mainObject').text()),
                    $overlay;

            // Create an overlay that contains all editable sections.
            $overlay = $('<iframe/>', {
                'src': '<%= wp.js(JspUtils.getAbsoluteUrl(application, request, "/content/remoteOverlay.jsp")) %>' +
                        '?id=' + encodeURIComponent(mainObjectData.id) +
                        '&url' + encodeURIComponent(loc.href),
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
                    var $begin = $(this),
                            data = $.parseJSON($begin.text()),
                            found = false,
                            minX = Number.MAX_VALUE,
                            maxX = 0,
                            minY = Number.MAX_VALUE,
                            maxY = 0,
                            $section,
                            $edit;

                    // Editable?
                    if (typeof data.id === 'undefined') {
                        return;
                    }

                    // Calculate the section size using the marker SPANs.
                    $begin.nextUntil('span.cms-overlayEnd').filter(':visible').each(function() {
                        var $item = $(this),
                                itemOffset = $item.offset(),
                                itemMinX = itemOffset.left,
                                itemMaxX = itemMinX + $item.outerWidth(),
                                itemMinY = itemOffset.top,
                                itemMaxY = itemMinY + $item.outerHeight();

                        found = true;

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

                    if (found) {
                        $section = $('<a/>', {
                            'class': 'remoteOverlay-section',
                            'css': {
                                'height': maxY - minY,
                                'left': minX,
                                'position': 'absolute',
                                'top': minY,
                                'width': maxX - minX
                            }
                        });

                        $edit = $('<a/>', {
                            'class': 'remoteOverlay-edit',
                            'href': '<%= wp.js(JspUtils.getAbsoluteUrl(application, request, "/content/remoteOverlayEdit.jsp")) %>?id=' + data.id,
                            'target': 'contentRemoteOverlayEdit',
                            'text': 'Edit ' + (data.sectionName || data.typeLabel + ': ' + data.label)
                        });

                        $section.append($edit);
                        $overlayBody.append($section);
                    }
                });
            });

            $body.append($overlay);
        });

        $body.append($duplicate);
    });
};

doc.body.appendChild(newJQueryScript)

}(window));
