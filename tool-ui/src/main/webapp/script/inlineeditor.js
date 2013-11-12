(function($, window, undefined) {
    var $document = $(document),
            $body = $($document[0].body),
            $parentDocument = $(window.parent.document),
            $parentBody = $($parentDocument[0].body),
            $editor = $parentBody.find('.cms-inlineEditor'),
            mainObjectData = $.parseJSON($parentBody.find('.cms-mainObject').attr('data-object'));

    // Create controls for all the objects in the parent document.
    $parentBody.find('.cms-objectBegin').each(function() {
        var $begin = $(this),
                objectData = $.parseJSON($begin.attr('data-object')),
                $outline,
                $edit,
                $controls;

        if (mainObjectData.id === objectData.id) {
            return;
        }

        $outline = $('<div/>', {
            'class': 'inlineEditorOutline'
        });

        $edit = $('<a/>', {
            'class': 'icon icon-action-edit',
            'href': CONTEXT_PATH + '/content/edit.jsp?id=' + objectData.id,
            'target': 'contentEdit',
            'text': objectData.typeLabel,

            'mouseenter': function() {
                var box = $.data($begin[0], 'inlineEditor-box');

                $controls.addClass('inlineEditorControls-hover');

                // Fade all the controls that overlap with this one.
                $parentBody.find('.cms-objectBegin-hasControls').each(function() {
                    var previousBox = $.data(this, 'inlineEditor-box');

                    if (box !== previousBox &&
                            previousBox.controlsCss.left <= box.controlsCss.left + box.outlineCss.width &&
                            box.controlsCss.left <= previousBox.controlsCss.left + previousBox.controlsDimension.width &&
                            previousBox.controlsCss.top + previousBox.outlineCss.top <= box.controlsCss.top + box.outlineCss.top + box.outlineCss.height &&
                            box.controlsCss.top + box.outlineCss.top <= previousBox.controlsCss.top + previousBox.outlineCss.top + previousBox.controlsDimension.height) {
                        previousBox.$controls.addClass('inlineEditorControls-under');
                    }
                });
            },

            'mouseleave': function() {
                $controls.removeClass('inlineEditorControls-hover');
                $body.find('.inlineEditorControls').removeClass('inlineEditorControls-under');
            }
        });

        $controls = $('<ul/>', {
            'class': 'inlineEditorControls',
            'html': $('<li/>', {
                'html': [ $outline, $edit ]
            })
        });

        $.data(this, 'inlineEditor-$controls', $controls);
        $body.append($controls);
        $begin.addClass('cms-objectBegin-hasControls');
    });

    function positionControls() {
        var previousBoxes = [ ];

        $parentBody.find('.cms-objectBegin-hasControls').each(function() {
            var $begin = $(this),
                    $controls = $.data(this, 'inlineEditor-$controls'),
                    found = false,
                    minX = Number.MAX_VALUE,
                    maxX = 0,
                    minY = Number.MAX_VALUE,
                    maxY = 0,
                    box,
                    intersects;

            // Figure out the object box size by finding the minimum and
            // maximum coordinates among all the elements between begin
            // and end markers.
            $begin.nextUntil('.cms-objectEnd').filter(':visible').each(function() {
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

            if (!found) {
                $controls.hide();

            } else {
                if (minY < 37) {
                    minY = 37;
                }

                box = {
                    '$controls': $controls,
                    'controlsCss': {
                        'left': minX,
                        'top': minY
                    },
                    'controlsDimension': {
                        'height': $controls.outerHeight() + 5,
                        'width': $controls.outerWidth()
                    },
                    'outlineCss': {
                        'height': maxY - minY,
                        'top': 0,
                        'width': maxX - minX
                    }
                };

                $.data($begin[0], 'inlineEditor-box', box);

                // Move the controls down until they don't overlay with
                // any other controls.
                if (previousBoxes.length > 0) {
                    do {
                        retry = false;

                        $.each(previousBoxes, function(i, previousBox) {
                            if (previousBox.controlsCss.left <= box.controlsCss.left + box.controlsDimension.width &&
                                    box.controlsCss.left <= previousBox.controlsCss.left + previousBox.controlsDimension.width &&
                                    previousBox.controlsCss.top <= box.controlsCss.top + box.controlsDimension.height &&
                                    box.controlsCss.top <= previousBox.controlsCss.top + previousBox.controlsDimension.height) {
                                retry = true;

                                box.controlsCss.top += 1;
                                box.outlineCss.top -= 1;
                                return false;
                            }
                        });
                    } while (retry);
                }

                previousBoxes.push(box);
                $controls.css(box.controlsCss);
                $controls.find('.inlineEditorOutline').css(box.outlineCss);
                $controls.show();
            }
        });
    }

    positionControls();
    setInterval(positionControls, 2000);

    // Enable "click-through" editor IFRAME.
    $parentDocument.on('mousemove', function(event) {
        if ($($document[0].elementFromPoint(event.pageX, event.pageY)).closest('.inlineEditorControls').length > 0) {
            $editor.css('pointer-events', 'auto');
        }
    });

    $document.on('mousemove', function(event) {
        if ($body.find('.popup:visible').length === 0 &&
                $($document[0].elementFromPoint(event.pageX, event.pageY)).closest('.inlineEditorControls').length === 0) {
            $editor.css('pointer-events', 'none');
        }
    });

    $document.on('click', 'a[target]', function() {
        $editor.css('pointer-events', 'auto');
    });

    // Make sure that the editor IFRAME is at least as high as the parent
    // document.
    function equalizeHeight() {
        $editor.height(Math.max($document.height(), $parentBody.height()));
    }

    equalizeHeight();
    setInterval(equalizeHeight, 100);
})(jQuery, window);
