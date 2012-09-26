(function() {

    // Constants.
    var fontFamily = '"Helvetica Neue", "Arial", sans-serif';
    var highRed = 204, highGreen = 0, highBlue = 0;
    var mediumRed = 170, mediumGreen = 170, mediumBlue = 0;
    var lowRed = 0, lowGreen = 204, lowBlue = 0;

    // Throttles the excution of the given throttledFunction to run
    // at most every given interval.
    var throttle = function(interval, throttledFunction) {
        if (interval <= 0) {
            return throttledFunction;
        }

        var lastTrigger = 0;
        var timeout;
        var lastArguments;
        return function() {

            lastArguments = arguments;
            if (timeout) {
                return;
            }

            var context = this;
            var now = +new Date();
            var delay = interval - now + lastTrigger;

            if (delay <= 0) {
                lastTrigger = now;
                throttledFunction.apply(context, lastArguments);

            } else {
                timeout = setTimeout(function() {
                    lastTrigger = now;
                    timeout = null;
                    throttledFunction.apply(context, lastArguments);
                }, delay);
            }
        };
    };

    // Calculates the boundary that can contain all elements between
    // the given $start and $end.
    var calculateBoundary = function($start, $stop) {
        var minX, minY, maxX, maxY;

        $start.nextUntil($stop).each(function() {
            var $child = $(this);
            var childOffset = $child.offset();
            var childMinX = childOffset.left;
            var childMinY = childOffset.top;
            var childMaxX = childMinX + $child.outerWidth(true);
            var childMaxY = childMinY + $child.outerHeight(true);

            if (!minX || minX > childMinX) {
                minX = childMinX;
            }
            if (!minY || minY > childMinY) {
                minY = childMinY;
            }
            if (!maxX || maxX < childMaxX) {
                maxX = childMaxX;
            }
            if (!maxY || maxY < childMaxY) {
                maxY = childMaxY;
            }
        });

        return {
            'left': minX, 
            'top': minY, 
            'width': maxX - minX,
            'height': maxY - minY
        };
    };

    // Main entry point.
    var main = function($) {
        var $body = $('body');

        // Code editor IFRAME.
        var $editorContainer = $('<div/>', {
            'css': {
                'background': 'white',
                '-moz-border-radius': '6px',
                '-webkit-border-radius': '6px',
                'border-radius': '6px',
                '-moz-box-shadow': '0 3px 7px rgba(0, 0, 0, 0.3)',
                '-webkit-box-shadow': '0 3px 7px rgba(0, 0, 0, 0.3)',
                'box-shadow': '0 3px 7px rgba(0, 0, 0, 0.3)',
                'display': 'none',
                'position': 'fixed',
                'z-index': 1000000
            }
        });
        var $editor = $('<iframe/>', {
            'css': {
                'border': 'none',
                '-moz-border-radius': '6px',
                '-webkit-border-radius': '6px',
                'border-radius': '6px',
                'height': '100%',
                'margin': '0px',
                'padding': '0px',
                'width': '100%'
            }
        });
        var $editorClose = $('<div/>', {
            'text': '×',
            'css': {
                'color': 'white',
                'cursor': 'default',
                'font-size': '20px',
                'font-weight': 'bold',
                'line-height': '18px',
                'text-shadow': '0 1px 0 black',
                'position': 'absolute',
                'right': '20px',
                'top': '10px'
            },
            'click': function() {
                $editorContainer.trigger('close');
            }
        });
        var $editorShim = $('<div/>', {
            'css': {
                'background': 'transparent',
                'display': 'none',
                'left': '0px',
                'height': '100%',
                'position': 'absolute',
                'top': '0px',
                'width': '100%'
            }
        });

        $body.append($editorContainer);
        $editorContainer.append($editor);
        $editorContainer.append($editorClose);
        $body.append($editorShim);

        // Tooltip that shows profile information around the element
        // under the mouse.
        var $tooltip = $('<div/>', {
            'css': {
                'background-color': 'rgba(0, 0, 0, 0.8)',
                '-moz-border-radius': '5px',
                '-webkit-border-radius': '5px',
                'border-radius': '5px',
                'color': 'white',
                'display': 'none',
                'font-family': fontFamily,
                'font-size': '12px',
                'max-width': '25%',
                'padding': '5px',
                'position': 'fixed',
                'top': '5px',
                'right': '5px',
                'z-index': 3000000
            }
        });
        $body.append($tooltip);

        // Create an IFRAME for the profile result so that it can
        // be shown with its own CSS.
        var $profile = $('<iframe src="/_resource/cms/profile.html" />');
        $profile.css({
            'border': 'none',
            'height': 1,
            'margin': 0,
            'padding': 0,
            'width': '100%'
        });

        $profile.load(function() {
            var $profile = $(this);
            var $profileBody = $(this.contentDocument.body);

            $profileBody.html($('#_profile-result').remove().html());
            $profile.height($profileBody.height() + 30);
            var $events = $profileBody.find('#_profile-eventTimeline tbody tr');

            var lastHoverElement;
            $(window).mousemove(throttle(100, function(event) {

                var $container = $(this);
                var x = event.pageX - $container.scrollLeft();
                var y = event.pageY - $container.scrollTop();

                // Hovering over a different element?
                $('._profile-openEditor').hide();
                var hoverElement = document.elementFromPoint(x, y);
                $('._profile-openEditor').show();
                if (hoverElement === lastHoverElement) {
                    return;
                } else {
                    lastHoverElement = hoverElement;
                }

                var $hoverElement = $(hoverElement);
                var tooltipHtml = '';

                // Over a visual display of an event.
                if ($hoverElement.is('._profile-eventDisplay')) {
                    var index = parseInt($hoverElement.attr('data-index'));
                    var $event = $events.eq(index);
                    tooltipHtml += 'Event #' + index + '<br>';
                    tooltipHtml += 'Start: ' + $event.find('td:eq(1)').html() + '<br>';
                    tooltipHtml += 'Total: ' + $event.find('td:eq(2)').html() + '<br>';
                    tooltipHtml += 'Own: ' + $event.find('td:eq(3)').html() + '<br>';
                    tooltipHtml += 'Objects: ' + $event.find('td:eq(4)').html();

                // Try to find all JSPs that may be associated with
                // rendering the element under the mouse.
                } else if (!$editorContainer.is(':visible')) {
                    $('._profile-openEditor').remove();

                    var $parent = $hoverElement;
                    var isFirst = true;
                    var padding = 0;
                    for (; $parent.length > 0; $parent = $parent.parent()) (function() {

                        var $start;
                        var $stop;
                        var jsp;
                        var $prev = $parent;
                        for (; $prev.length > 0; $prev = $prev.prev()) {

                            if ($prev.is('._profile-jspStart')) {
                                $start = $prev;
                                var prevJsp = $prev.attr('data-jsp');
                                if (prevJsp.indexOf('/_draft/') > -1) {
                                    continue;
                                }

                                var $next = $parent;
                                for (; $next.length > 0; $next = $next.next()) {
                                    if ($next.is('._profile-jspStop[data-jsp=' + prevJsp + ']')) {
                                        $stop = $next;
                                        jsp = prevJsp;
                                        tooltipHtml += (isFirst ? 'JSP: ' : '<br>Called from ') + jsp;
                                        isFirst = false;
                                        break;
                                    }
                                }
                                break;

                            } else if ($prev.is('._profile-jspStop')) {
                                break;
                            }
                        }

                        if (!jsp) {
                            return;
                        }

                        var boundary = calculateBoundary($start, $stop);
                        padding += 4;

                        $('body').append($('<a/>', {
                            'class': '_profile-openEditor',
                            'data-padding': padding,
                            'href': '/_debug/code' +
                                    '?action=edit' +
                                    '&type=JSP' +
                                    '&servletPath=' + encodeURIComponent(jsp) +
                                    '&jspPreviewUrl=' + encodeURIComponent(location.href),
                            'css': {
                                'border': '1px solid rgba(204, 0, 0, 0.8)',
                                '-moz-border-radius': '4px',
                                '-webkit-border-radius': '4px',
                                'border-radius': '4px',
                                'display': 'block',
                                'height': boundary.height + (padding * 2),
                                'left': boundary.left - padding,
                                'position': 'absolute',
                                'top': boundary.top - padding,
                                'width': boundary.width + (padding * 2),
                                'z-index': 1000000 - padding
                            },

                            'click': function() {
                                var $openEditor = $(this);
                                var padding = $openEditor.attr('data-padding');
                                $('._profile-openEditor').not($openEditor).fadeOut();

                                // Close the editor if it's already open.
                                $editorContainer.trigger('close');

                                // Copy the result from the editor to the page.
                                $editor.load(function() {
                                    $editor.unbind('load');
                                    console.log('binding');

                                    $editorContainer.data('copyResultInterval', setInterval(function() {
                                        var $resultContainer = $($editor[0].contentDocument.body).find('.resultContainer');
                                        var $result = $resultContainer.find('.frame > *');

                                        if ($result.length > 0 && !$result.is('.copied')) {
                                            $result.addClass('copied');

                                            if ($result.is('.alert')) {
                                                $resultContainer.show();

                                            } else {
                                                $start.nextUntil($stop).remove();
                                                $start.after($result.text());
                                                $resultContainer.hide();

                                                var boundary = calculateBoundary($start, $stop);
                                                boundary.left -= padding;
                                                boundary.top -= padding;
                                                boundary.width += padding * 2;
                                                boundary.height += padding * 2;
                                                $openEditor.css(boundary);
                                            }
                                        }
                                    }, 50));

                                    $tooltip.fadeOut();
                                    $('._profile-eventDisplay').fadeOut();
                                });

                                $editor.attr('src', $(this).attr('href'));

                                // Stop copying result when the editor is closed.
                                var originalScrollLeft = $body.scrollLeft();
                                var originalScrollTop = $body.scrollTop();
                                $editorContainer.bind('close', function() {
                                    $editorContainer.unbind('close');

                                    var copyResultInterval = $editorContainer.data('copyResultInterval');
                                    if (copyResultInterval) {
                                        clearInterval(copyResultInterval);
                                    }

                                    $editorContainer.fadeOut();
                                    $body.animate({ 'scrollLeft': originalScrollLeft, 'scrollTop': originalScrollTop }, {
                                        'complete': function() {
                                            $editorShim.hide();
                                            $('._profile-eventDisplay').fadeIn();
                                        }
                                    });
                                });

                                // Open to the right or bottom?
                                var $win = $(window);
                                var winWidth = $win.width();
                                var winHeight = $win.height();
                                var rightArea = (winWidth - boundary.width) * winHeight;
                                var bottomArea = winWidth * (winHeight - boundary.height);

                                if (rightArea > bottomArea) {
                                    $editorShim.css({
                                        'height': $body.height(),
                                        'width': winWidth + boundary.left - 20
                                    });
                                    $editorContainer.css({
                                        'height': winHeight - 40,
                                        'left': winWidth,
                                        'top': 20,
                                        'width': winWidth - boundary.width - 60
                                    });

                                    $editorShim.show();
                                    $editorContainer.show();
                                    $body.animate({ 'scrollLeft': boundary.left - 20, 'scrollTop': boundary.top - 20 });
                                    $editorContainer.animate({ 'left': boundary.width + 40 });

                                } else {
                                    $editorShim.css({
                                        'height': $body.height(),
                                        'width': $body.width()
                                    });
                                    $editorContainer.css({
                                        'height': winHeight - (boundary.height) - 60,
                                        'left': 20,
                                        'top': winHeight,
                                        'width': winWidth - 40
                                    });

                                    $editorShim.show();
                                    $editorContainer.show();
                                    $body.animate({ 'scrollTop': boundary.top - 20 });
                                    $editorContainer.animate({ 'top': (boundary.height) + 40 });
                                }

                                return false;
                            }
                        }));
                    })();
                }

                if (tooltipHtml) {
                    $tooltip.html(tooltipHtml);
                    $tooltip.fadeIn();
                } else {
                    $tooltip.fadeOut();
                }
            }));

            var maxOwn = 100;
            $events.each(function() {
                var own = parseFloat($(this).find('td:eq(3)').text());
                if (maxOwn < own) {
                    maxOwn = own;
                }
            });

            // Visual display of all events.
            $('._profile-eventStart').each(function() {
                var $eventStart = $(this);
                var index = parseInt($eventStart.attr('data-index'));
                var $event = $events.eq(index);
                var own = parseFloat($event.find('td:eq(3)').text());
                var ratio = own / maxOwn;
                var size = 40 + ratio * 160;

                // Find the closest parent that's visible so that
                // the display shows in the correct position.
                while ($eventStart.length > 0 && !$eventStart.is(':visible')) {
                    $eventStart = $eventStart.parent();
                }
                if ($eventStart.length < 1) {
                    return 
                }

                var pointOffset = $eventStart.offset();
                var pointLeft = pointOffset.left;
                var pointTop = pointOffset.top;

                // Center it.
                pointLeft -= size / 2;
                pointTop -= size / 2;

                // Make sure it's not off-screen.
                if (pointLeft < 0) {
                    pointLeft = 0;
                }
                if (pointTop < 0) {
                    pointTop = 0;
                }

                // Color based on how long the event took.
                var red, green, blue, alpha = ratio * 0.2 + 0.2;
                if (ratio > 0.5) {
                    var r = (ratio - 0.5) * 2;
                    red = mediumRed + (highRed - mediumRed) * r;
                    green = mediumGreen + (highGreen - mediumGreen) * r;
                    blue = mediumBlue + (highBlue - mediumBlue) * r;

                } else {
                    var r = ratio * 2;
                    red = lowRed + (mediumRed - lowRed) * r;
                    green = lowGreen + (mediumGreen - lowGreen) * r;
                    blue = lowBlue + (mediumBlue - lowBlue) * r;
                }

                $body.append($('<div/>', {
                    'class': '_profile-eventDisplay',
                    'data-index': index,
                    'text': Math.floor(own),
                    'css': {
                        'background-color': 'rgba(' + Math.floor(red) + ', ' + Math.floor(green) + ', ' + Math.floor(blue) + ', ' + alpha + ')',
                        '-moz-border-radius': size,
                        '-webkit-border-radius': size,
                        'border-radius': size,
                        'color': 'white',
                        'cursor': 'pointer',
                        'height': size,
                        'font-family': fontFamily,
                        'font-size': size * 0.4,
                        'left': pointLeft,
                        'line-height': size + 'px',
                        'position': 'absolute',
                        'text-align': 'center',
                        'text-shadow': 'rgba(0, 0, 0, 0.5) 0 0 ' + Math.floor(ratio * 4 + 3) + 'px',
                        'top': pointTop,
                        'width': size,
                        'z-index': Math.floor(2000000 + ratio * 1000)
                    },
                    'click': function() {
                        $tooltip.fadeOut();
                        $body.animate({ 'scrollTop': $profile.offset().top + $event.offset().top });
                        return false;
                    }
                }));
            });
        });

        $body.append($profile);
    };

    var jqScript = document.createElement('script');
    jqScript.src = '/_resource/jquery/jquery-1.7.1.min.js';
    jqScript.onload = function() { main(jQuery.noConflict(true)); };
    document.body.appendChild(jqScript);
})();
