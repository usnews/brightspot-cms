/** Image editor. */
(function($, win, undef) {

var INPUT_NAMES = 'x y width height text textSize textX textY textWidth'.split(' '),
        doc = win.document;

$.plugin2('imageEditor', {
    '_create': function(element) {
        var $editor = $(element);
        var $form = $editor.closest('form');
        var $image = $editor.find('.imageEditor-image img');
        var imageSrc = $image.attr('src');
        var $originalImage = $image;
        var $imageClone = $image.clone();
        var imageClone = $imageClone[0];

        var $tools = $editor.find('.imageEditor-tools ul');
        var $edit = $editor.find('.imageEditor-edit');

        var $editButton = $('<li/>', {
            'html': $('<a/>', {
                'class': 'action-image-edit',
                'text': 'Edit Image',
                'click': function() {
                    $edit.popup('source', $(this));
                    $edit.popup('open');
                    return false;
                }
            })
        });

        $tools.prepend($editButton);

        var $editInputs = $('<div/>');

        $editButton.append($editInputs);

        var previousOperations;
        var processImageTimer;

        var processImage = function() {
            var operations = { };

            $edit.find(':input').each(function() {
                var $input = $(this);
                var value = $input.is(':checkbox') ? $input.is(':checked') : parseFloat($input.val());

                if (value === false || isNaN(value)) {
                    return;
                }

                var name = /.([^.]+)$/.exec($input.attr('name'));
                name = name ? name[1] : null;

                if (name === 'brightness') {
                    operations.brightness = operations.brightness || { };
                    operations.brightness.brightness = value * 150;
                    operations.brightness.legacy = true;

                } else if (name === 'contrast') {
                    operations.brightness = operations.brightness || { };
                    operations.brightness.contrast = value < 0 ? value : value * 3;

                } else if (name === 'flipH') {
                    operations.fliph = operations.fliph || { };

                } else if (name === 'flipV') {
                    operations.flipv = operations.flipv || { };

                } else if (name === 'grayscale') {
                    operations.desaturate = operations.desaturate || { };

                } else if (name === 'invert') {
                    operations.invert = operations.invert || { };

                } else if (name === 'rotate') {
                    operations.rotate = operations.rotate|| { };
                    operations.rotate.angle = -value;

                } else if (name === 'sepia') {
                    operations.sepia = operations.sepia || { };
                }
            });

            var serialized = JSON.stringify(operations);

            if (serialized !== previousOperations) {
                previousOperations = serialized;

                $editInputs.empty();
                $edit.find(':input').each(function() {
                    var $input = $(this);

                    if (!$input.is(':checkbox') || $input.is(':checked')) {
                        $editInputs.append($('<input/>', {
                            'type': 'hidden',
                            'name': $input.attr('name'),
                            'value': $input.val()
                        }));
                    }
                });

                var operationKeys = [ ];
                var operationKeyIndex = 0;

                for (var key in operations) {
                    if (operations.hasOwnProperty(key)) {
                        operationKeys[operationKeys.length] = key;
                    }
                }

                var operate = function(processedImage, index) {
                    if (index < operationKeys.length) {
                        var key = operationKeys[index];

                        Pixastic.process(processedImage, key, operations[key], function(newImage) {
                            ++ index;
                            operate(newImage, index);
                        });

                    } else {
                        if ($image !== $originalImage) {
                            $image.remove();
                        }

                        $originalImage.before(processedImage);
                        $originalImage.hide();
                        $image = $(processedImage);

                        processImageTimer = setTimeout(processImage, 100);
                    }
                };

                operate(imageClone, 0);

            } else {
                processImageTimer = setTimeout(processImage, 100);
            }
        };

        processImage();

        $edit.popup('container').bind('open', function() {
            processImage();
        });

        $edit.popup('container').bind('close', function() {
            if (processImageTimer) {
                clearTimeout(processImageTimer);
            }
        });

        var $resetButton = $('<button/>', {
            'text': 'Reset',
            'click': function() {
                $edit.find(':input').each(function() {
                    var $input = $(this);

                    if ($input.is(':checkbox')) {
                        $input.removeAttr('checked');

                    } else {
                        $input.val(0);
                    }
                });

                processImage();

                return false;
            }
        });

        $edit.append($resetButton);

        $edit.popup();
        $edit.popup('container').addClass('imageEditor-editPopup');
        $edit.popup('close');

        var $coverTop = $('<div/>', {
            'class': 'imageEditor-cover',
            'css': {
                'display': 'none',
                'left': '0',
                'position': 'absolute',
                'top': '0'
            }
        });

        var $coverLeft = $coverTop.clone(true);
        var $coverRight = $coverTop.clone(true);
        var $coverBottom = $coverTop.clone(true);

        $editor.append($coverTop);
        $editor.append($coverLeft);
        $editor.append($coverRight);
        $editor.append($coverBottom);

        var updateCover = function(bounds) {
            var imageWidth = $image.width();
            var imageHeight = $image.height();
            var boundsRight = bounds.left + bounds.width;
            var boundsBottom = bounds.top + bounds.height;

            $coverTop.css({
                'height': bounds.top,
                'width': imageWidth
            });
            $coverLeft.css({
                'height': bounds.height,
                'top': bounds.top,
                'width': bounds.left
            });
            $coverRight.css({
                'height': bounds.height,
                'left': boundsRight,
                'top': bounds.top,
                'width': imageWidth - boundsRight
            });
            $coverBottom.css({
                'height': imageHeight - boundsBottom,
                'top': boundsBottom,
                'width': imageWidth
            });

            $coverTop.show();
            $coverLeft.show();
            $coverRight.show();
            $coverBottom.show();
        };

        var $sizes = $editor.find('.imageEditor-sizes table');
        var $sizeSelectors = $('<ul/>', { 'class': 'imageEditor-sizeSelectors' });
        var $mainInputs = { };

        $sizes.find('th').each(function() {
            var $th = $(this);
            var $tr = $th.closest('tr');

            var sizeWidth = parseFloat($tr.attr('data-size-width'));
            var sizeHeight = parseFloat($tr.attr('data-size-height'));
            var sizeAspectRatio = sizeWidth / sizeHeight;

            var $input = { };

            $.each(INPUT_NAMES, function(index, name) {
                $input[name] = $tr.find(':input[name$=".' + name + '"]');
            });

            var ratio = Math.floor(sizeAspectRatio * 100) / 100;
            var $mainInput = $mainInputs['' + ratio];

            if ($mainInput) {
                $.each(INPUT_NAMES, function(index, name) {
                    $mainInput[name] = $mainInput[name].add($input[name]);
                });
                return;

            } else {
                $mainInputs['' + (ratio - 0.02)] = $input;
                $mainInputs['' + (ratio - 0.01)] = $input;
                $mainInputs['' + ratio] = $input;
                $mainInputs['' + (ratio + 0.01)] = $input;
                $mainInputs['' + (ratio + 0.02)] = $input;
            }

            var $sizeBox = $('<div/>', {
                'class': 'imageEditor-sizeBox',
                'css': { 'position': 'absolute' }
            });

            $sizeBox.hide();
            $editor.append($sizeBox);

            var getSizeBounds = function($image) {
                var imageWidth = $image.width();
                var imageHeight = $image.height();

                var left = parseFloat($input.x.val()) || 0.0;
                var top = parseFloat($input.y.val()) || 0.0;
                var width = parseFloat($input.width.val()) || 0.0;
                var height = parseFloat($input.height.val()) || 0.0;

                if (width === 0.0 || height === 0.0) {
                    width = imageHeight * sizeAspectRatio;
                    height = imageWidth / sizeAspectRatio;

                    if (width > imageWidth) {
                        width = height * sizeAspectRatio;
                    } else {
                        height = width / sizeAspectRatio;
                    }

                    left = (imageWidth - width) / 2;
                    top = 0;

                } else {
                    left *= imageWidth;
                    top *= imageHeight;
                    width *= imageWidth;
                    height *= imageHeight;
                }

                return {
                    'left': left,
                    'top': top,
                    'width': width,
                    'height': height
                };
            };

            var $sizeButton = $('<li/>', {
                'class': 'imageEditor-sizeButton',
                'click': function() {
                    var $item = $(this).closest('li');

                    if ($item.is('.imageEditor-sizeSelected')) {
                        $item.removeClass('imageEditor-sizeSelected');
                        $coverTop.hide();
                        $coverLeft.hide();
                        $coverRight.hide();
                        $coverBottom.hide();
                        $sizeBox.hide();
                        $textOverlay.hide();
                        return false;

                    } else {
                        $item.closest('ul').find('li').removeClass('imageEditor-sizeSelected');
                        $item.addClass('imageEditor-sizeSelected');
                    }

                    $editor.find('.imageEditor-sizeBox').hide();

                    var bounds = getSizeBounds($image);

                    updateCover(bounds);
                    $sizeBox.css(bounds);
                    $sizeBox.show();
                    $textOverlay.show();
                    resizeTextOverlayFont();

                    var textWidth = parseFloat($input.textWidth.val()) || 0.0;

                    if (textWidth !== 0.0) {
                        var textX = parseFloat($input.textX.val()) || 0.0;
                        var textY = parseFloat($input.textY.val()) || 0.0;

                        $textOverlay.css({
                            'left': (textX * 100) + '%',
                            'top': (textY * 100) + '%',
                            'width': (textWidth * 100) + '%'
                        });
                    }

                    return false;
                }
            });

            $sizeSelectors.append($sizeButton);

            var updatePreview = function() {
                var $body = $(doc.body);
                $body.append($imageClone);
                var bounds = getSizeBounds($imageClone);
                $imageClone.remove();
                Pixastic.process(imageClone, 'crop', bounds, function(newImage) {
                    var $preview = $sizeButton.find('.imageEditor-sizePreview');
                    $preview.empty();
                    $preview.append(newImage);
                });
            };

            var updateSizeBox = function(callback) {
                return function(event) {
                    var sizeBoxPosition = $sizeBox.position();
                    var original = {
                        'left': sizeBoxPosition.left,
                        'top': sizeBoxPosition.top,
                        'width': $sizeBox.width(),
                        'height': $sizeBox.height(),
                        'pageX': event.pageX,
                        'pageY': event.pageY
                    };

                    var imageWidth = $image.width();
                    var imageHeight = $image.height();

                    $.drag(this, event, function(event) {

                    }, function(event) {
                        var deltaX = event.pageX - original.pageX;
                        var deltaY = event.pageY - original.pageY;
                        var bounds = callback(event, original, {
                            'x': deltaX,
                            'y': deltaY,
                            'constrainedX': Math.max(deltaX, deltaY * sizeAspectRatio),
                            'constrainedY': Math.max(deltaY, deltaX / sizeAspectRatio)
                        });

                        // Fill out the missing bounds.
                        for (key in original) {
                            bounds[key] = bounds[key] || original[key];
                        }

                        var overflow;

                        // When moving, don't let it go outside the image.
                        if (bounds.moving) {
                            if (bounds.left < 0) {
                                bounds.left = 0;
                            }

                            if (bounds.top < 0) {
                                bounds.top = 0;
                            }

                            overflow = bounds.left + bounds.width - imageWidth;
                            if (overflow > 0) {
                                bounds.left -= overflow;
                            }

                            overflow = bounds.top + bounds.height - imageHeight;
                            if (overflow > 0) {
                                bounds.top -= overflow;
                            }

                        // Resizing...
                        } else {
                            if (bounds.width < 10 || bounds.height < 10) {
                                if (sizeAspectRatio > 1.0) {
                                    bounds.width = sizeAspectRatio * 10;
                                    bounds.height = 10;
                                } else {
                                    bounds.width = 10;
                                    bounds.height = 10 / sizeAspectRatio;
                                }
                            }

                            if (bounds.left < 0) {
                                bounds.width += bounds.left;
                                bounds.height = bounds.width / sizeAspectRatio;
                                bounds.top -= bounds.left / sizeAspectRatio;
                                bounds.left = 0;
                            }

                            if (bounds.top < 0) {
                                bounds.height += bounds.top;
                                bounds.width = bounds.height * sizeAspectRatio;
                                bounds.left -= bounds.top * sizeAspectRatio;
                                bounds.top = 0;
                            }

                            overflow = bounds.left + bounds.width - imageWidth;
                            if (overflow > 0) {
                                bounds.width -= overflow;
                                bounds.height = bounds.width / sizeAspectRatio;
                            }

                            overflow = bounds.top + bounds.height - imageHeight;
                            if (overflow > 0) {
                                bounds.height -= overflow;
                                bounds.width = bounds.height * sizeAspectRatio;
                            }
                        }

                        updateCover(bounds);
                        $sizeBox.css(bounds);
                        resizeTextOverlayFont();

                    // Set the hidden inputs to the current bounds.
                    }, function() {
                        var sizeBoxPosition = $sizeBox.position();
                        var sizeBoxWidth = $sizeBox.width();
                        var sizeBoxHeight = $sizeBox.height();

                        $input.x.val(sizeBoxPosition.left / imageWidth);
                        $input.y.val(sizeBoxPosition.top / imageHeight);
                        $input.width.val(sizeBoxWidth / imageWidth);
                        $input.height.val(sizeBoxWidth / sizeAspectRatio / imageHeight);

                        updatePreview();
                        resizeTextOverlayFont();
                    });

                    return false;
                };
            };

            $sizeBox.mousedown(updateSizeBox(function(event, original, delta) {
                return {
                    'moving': true,
                    'left': original.left + delta.x,
                    'top': original.top + delta.y
                };
            }));

            $sizeBox.hide();
            $editor.append($sizeBox);

            $sizeBox.append($('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-topLeft',
                'mousedown': updateSizeBox(function(event, original, delta) {
                    return {
                        'left': original.left + delta.constrainedX,
                        'top': original.top + delta.constrainedY,
                        'width': original.width - delta.constrainedX,
                        'height': original.height - delta.constrainedY
                    };
                })
            }));
            $sizeBox.append($('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-bottomRight',
                'mousedown': updateSizeBox(function(event, original, delta) {
                    return {
                        'width': original.width + delta.constrainedX,
                        'height': original.height + delta.constrainedY
                    };
                })
            }));

            var $sizeLabel = $('<span/>', {
                'class': 'imageEditor-sizeLabel',
                'text': $th.text()
            });

            $sizeButton.append($sizeLabel);

            var $sizePreview = $('<div/>', {
                'class': 'imageEditor-sizePreview',
                'html': $('<img/>', {
                    'alt': $th.text(),
                    'src': imageSrc,
                    'load': function() {
                        updatePreview();
                    }
                })
            });

            $sizeButton.append($sizePreview);

            var $textOverlay = $('<div/>', {
                'class': 'imageEditor-textOverlay',
                'css': {
                    'display': 'none',
                    'left': '0%',
                    'position': 'absolute',
                    'top': '0%',
                    'width': '100%',
                    'z-index': 1
                }
            });

            $sizeBox.append($textOverlay);

            var $textOverlayLabel = $('<div/>', {
                'class': 'imageEditor-textOverlayLabel',
                'text': 'Text Overlay'
            });

            var originalFontSize;
            var resizeTextOverlayFont = function() {
                var $body = $($textOverlay.find('.rte-container iframe')[0].contentDocument.body),
                        textSize;

                if (!originalFontSize) {
                    originalFontSize = parseFloat($body.css('font-size'));
                }

                textSize = 1 / sizeHeight * originalFontSize;
                $input.textSize.val(textSize);
                $body.css('font-size', $sizeBox.height() * textSize);
            };

            var updateTextOverlay = function(callback) {
                return function(event) {
                    resizeTextOverlayFont();

                    var textOverlayPosition = $textOverlay.position();
                    var original = {
                        'left': textOverlayPosition.left,
                        'top': textOverlayPosition.top,
                        'width': $textOverlay.width(),
                        'height': $textOverlay.height(),
                        'pageX': event.pageX,
                        'pageY': event.pageY
                    };

                    var sizeBoxWidth = $sizeBox.width();
                    var sizeBoxHeight = $sizeBox.height();

                    $.drag(this, event, function(event) {

                    }, function(event) {
                        var deltaX = event.pageX - original.pageX;
                        var deltaY = event.pageY - original.pageY;
                        var bounds = callback(event, original, {
                            'x': deltaX,
                            'y': deltaY
                        });

                        // Fill out the missing bounds.
                        for (key in original) {
                            bounds[key] = bounds[key] || original[key];
                        }

                        bounds.left = ((bounds.left / sizeBoxWidth) * 100) + '%';
                        bounds.top = ((bounds.top / sizeBoxHeight) * 100) + '%';
                        bounds.width = ((bounds.width / sizeBoxWidth) * 100) + '%';
                        bounds.height = 'auto';

                        $textOverlay.css(bounds);

                    // Set the hidden inputs to the current bounds.
                    }, function() {
                        var textOverlayPosition = $textOverlay.position();
                        var textOverlayWidth = $textOverlay.width();
                        var textOverlayHeight = $textOverlay.height();

                        $input.text.val($textOverlayInput.val());
                        $input.textX.val(textOverlayPosition.left / sizeBoxWidth);
                        $input.textY.val(textOverlayPosition.top / sizeBoxHeight);
                        $input.textWidth.val(textOverlayWidth / sizeBoxWidth);
                    });

                    return false;
                };
            };

            $textOverlayLabel.mousedown(updateTextOverlay(function(event, original, delta) {
                return {
                    'moving': true,
                    'left': original.left + delta.x,
                    'top': original.top + delta.y
                };
            }));

            $textOverlay.append($textOverlayLabel);

            $textOverlay.append($('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-left',
                'mousedown': updateTextOverlay(function(event, original, delta) {
                    return {
                        'left': original.left + delta.x,
                        'width': original.width - delta.x
                    };
                })
            }));
            $textOverlay.append($('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-right',
                'mousedown': updateTextOverlay(function(event, original, delta) {
                    return {
                        'left': original.left,
                        'width': original.width + delta.x
                    };
                })
            }));

            var $textOverlayInput = $('<input/>', {
                'class': 'imageEditor-textOverlayInput',
                'type': 'text',
                'value': $input.text.val()
            });

            $textOverlay.append($textOverlayInput);
            $textOverlayInput.rte({
                'useLineBreaks': true
            });

            $form.submit(function() {
                $input.text.val($textOverlayInput.val());
            });
        });

        $sizes.before($sizeSelectors);
        $sizes.hide();
    }
});

}(jQuery, window));
