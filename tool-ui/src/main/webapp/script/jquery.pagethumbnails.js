(function($, win, undef) {

var $win = $(win),
        doc = win.document;

$.plugin2('pageThumbnails', {
    '_create': function(container) {
        var plugin = this,
                $container = $(container),
                $preview,
                $previewLink;

        $preview = $('<div/>', {
            'class': 'pageThumbnails-preview'
        });

        $previewLink = $('<a/>', {
            'class': 'pageThumbnails-previewLink',
            'href': '#',
            'target': '_blank',
            'text': 'View in Full'
        });

        $preview.hide();
        $(doc.body).append($preview);

        $container.mouseleave(function() {
            $preview.hide();
        });

        $container.delegate('[data-preview-url]', 'mouseenter', function() {
            var $element = $(this),
                    previewUrl = $element.attr('data-preview-url');

            if (!previewUrl) {
                $preview.hide();
                return;
            }

            $.data(this, 'pageThumbnails-showPreviewTimer', setTimeout(function() {
                var $previewLink = $container.find('.pageThumbnails-previewLink'),
                        $previewFrame = $.data($element[0], 'pageThumbnails-previewFrame'),
                        showPreviewFrame,
                        containerOffset,
                        previewWidth,
                        previewLeft;

                $previewLink.attr('href', previewUrl);
                $element.append($previewLink);

                if (!$previewFrame) {
                    $previewFrame = $('<iframe/>', {
                        'class': 'pageThumbnails-previewFrame',
                        'src': previewUrl
                    });

                    // $.data($element[0], 'pageThumbnails-previewFrame', $previewFrame);
                    $previewFrame.hide();
                    $preview.empty();
                    $preview.append($previewFrame);
                }

                if (!$preview.is(':visible')) {
                    containerOffset = $container.offset();
                    previewWidth = $preview.outerWidth(true);
                    previewLeft = containerOffset.left + $container.outerWidth(true);

                    if (previewLeft + previewWidth > $(doc.body).width()) {
                        previewLeft = containerOffset.left -
                                $.css($container[0], 'padding-left', true) -
                                $.css($container[0], 'border-left-width', true) -
                                $.css($container[0], 'margin-left', true) -
                                previewWidth;
                    }

                    $preview.css('left', previewLeft).show();
                }

                $previewFrame.show();
            }, 500));

        });

        $container.delegate('[data-preview-url]', 'mouseleave', function() {
            var $element = $(this),
                    showPreviewTimer = $.data(this, 'pageThumbnails-showPreviewTimer'),
                    $previewFrame = $.data($element[0], 'pageThumbnails-previewFrame');

            if (showPreviewTimer) {
                clearTimeout(showPreviewTimer);
            }

            if ($previewFrame) {
                $previewFrame.hide();
            }
        });
    }
});

}(jQuery, window));
