(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc),

        $containers = $(),
        $currentContainer,
        showPreviewTimeout,
        hidePreviewTimeout;

$.plugin2('pageThumbnails', {
    '_create': function(container) {
        var plugin = this,
                $container = $(container),
                $body = $(doc.body),

                $toggle,
                enabled,

                $preview,
                togglePreview,

                $hover,
                previewUrl,
                containerOffset,
                containerWidth,
                leftLarger;

        $containers = $containers.add($container);

        $container.bind('hidePreview.pageThumbnails', function() {
            if ($toggle) {
                $toggle.remove();
                $toggle = null;
            }

            enabled = false;

            if ($preview) {
                $preview.remove();
                $preview = null;
            }

            if (showPreviewTimeout) {
                clearTimeout(showPreviewTimeout);
                showPreviewTimeout = null;
            }
        });

        $container.delegate('[data-preview-url]:not([data-preview-url=""])', 'mouseenter.pageThumbnails', function() {
            var hoverTop,
                    hoverHeight,
                    toggleLeft;

            if ($currentContainer && $currentContainer !== $container) {
                $currentContainer.trigger('hidePreview');
            }

            $currentContainer = $container;
            $hover = $(this);
            previewUrl = $hover.attr('data-preview-url');

            if (!$toggle) {
                $toggle = $('<div/>', {
                    'class': 'pageThumbnails_toggle',
                    'text': 'Preview'
                });

                $toggle.bind('click.pageThumbnails', function(event) {
                    enabled = !enabled;
                    $toggle.toggleClass('pageThumbnails_toggle-enabled', enabled);
                    togglePreview();

                    return false;
                });

                $body.append($toggle);
            }

            hoverTop = $hover.offset().top;
            hoverHeight = $hover.outerHeight();
            containerOffset = $container.offset();
            containerWidth = $container.outerWidth();
            leftLarger = containerOffset.left > $doc.width() - containerOffset.left - containerWidth;

            if (leftLarger) {
                toggleLeft = containerOffset.left -
                        $.css($container[0], 'padding-left', true) -
                        $.css($container[0], 'border-left-width', true) -
                        $.css($container[0], 'margin-left', true);

            } else {
                toggleLeft = containerOffset.left + containerWidth - $toggle.outerWidth(true);
            }

            $toggle.toggleClass('pageThumbnails_toggle-right', leftLarger);

            togglePreview(function() {
                $toggle.css({
                    'height': hoverHeight,
                    'left': toggleLeft,
                    'line-height': hoverHeight + 'px',
                    'position': 'absolute',
                    'top': hoverTop
                });
            });
        });

        togglePreview = function(moveToggle) {
            var showPreview;

            if (enabled) {
                if (!$preview) {
                    $preview = $('<div/>', {
                        'class': 'pageThumbnails_preview'
                    });

                    $preview.hide();
                    $body.append($preview);
                }

                if (showPreviewTimeout) {
                    clearTimeout(showPreviewTimeout);
                    showPreviewTimeout = null;
                }

                showPreview = function() {
                    var resizeFrame,
                            resizeFrameTimeout,
                            previewLeft;

                    if (moveToggle) {
                        moveToggle();
                    }

                    $preview.html($('<a/>', {
                        'class': 'pageThumbnails_preview_livePage',
                        'href': previewUrl,
                        'target': '_blank',
                        'text': 'Live Page',
                        'click': function() {
                            win.open($(this).attr('href'));

                            return false;
                        }

                    }).add($('<iframe/>', {
                        'class': 'pageThumbnails_preview_frame',
                        'src': previewUrl,
                        'load': function() {
                            resizeFrame();

                            if (resizeFrameTimeout) {
                                clearTimeout(resizeFrameTimeout);
                                resizeFrameTimeout = null;
                            }
                        }
                    })));

                    resizeFrame = function() {
                        var $frame;

                        if ($preview && $preview.is(':visible')) {
                            $frame = $preview.find('iframe');
                            $frame.height($frame.contents().find('body').height() + 50);
                            resizeFrameTimeout = setTimeout(resizeFrame, 100);
                        }
                    };

                    resizeFrame();

                    if (leftLarger) {
                        previewLeft = containerOffset.left -
                                $.css($container[0], 'padding-left', true) -
                                $.css($container[0], 'border-left-width', true) -
                                $.css($container[0], 'margin-left', true) -
                                $preview.outerWidth(true);

                    } else {
                        previewLeft = containerOffset.left + containerWidth;
                    }

                    $preview.toggleClass('pageThumbnails_preview-right', leftLarger);
                    $preview.css('left', previewLeft);
                    $preview.show();
                };

                if ($preview.is(':visible')) {
                    showPreviewTimeout = setTimeout(function() {
                        showPreview();
                        showPreviewTimeout = null;
                    }, 500);

                } else {
                    showPreview();
                }

            } else {
                if (moveToggle) {
                    moveToggle();
                }

                if ($preview) {
                    $preview.remove();
                    $preview = null;
                }
            }
        };
    }
});

$win.bind('mousemove.pageThumbnails', $.throttle(100, function(event) {
    var $under = $(event.target),
            cancelHiding,
            $current;

    if ($under.length === 0) {
        return;
    }

    if ($under.closest('.pageThumbnails_preview').length > 0) {
        cancelHiding = true;

        if (showPreviewTimeout) {
            clearTimeout(showPreviewTimeout);
            showPreviewTimeout = null;
        }

    } else if ($under.closest('.pageThumbnails_toggle').length > 0) {
        cancelHiding = true;
    }

    if (!cancelHiding &&
            $currentContainer &&
            !$.contains($currentContainer[0], $under[0])) {
        $current = $currentContainer;

        if (!hidePreviewTimeout) {
            hidePreviewTimeout = setTimeout(function() {
                $current.trigger('hidePreview');
                hidePreviewTimeout = null;

                if ($currentContainer === $current) {
                    $currentContainer = null;
                }
            }, 200);
        }

        return;
    }

    if (hidePreviewTimeout) {
        clearTimeout(hidePreviewTimeout);
        hidePreviewTimeout = null;
    }
}));

}(jQuery, window));
