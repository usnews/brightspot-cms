if (typeof jQuery !== 'undefined') jQuery(function($) {

$('.repeatableForm:visible, .repeatableInputs:visible').repeatable({
    'sortableOptions': {
        'start': function(event, ui) {
            $(ui.item).find('textarea.richtext').each(function() {
                try {
                    $(this).ckeditorGet().destroy();
                } catch (error) {
                }
            });
        },
        'stop': function(event, ui) {
            $(ui.item).find('textarea.richtext').editor();
        }
    }
});
$('.repeatableObjectId:visible').repeatable();
$('.repeatableText:visible').repeatable({
    'addButtonText': '',
    'removeButtonText': '',
    'restoreButtonText': ''
});

$(':input.date:visible').calendar();
$('select[multiple]:visible').dropDown();
$('textarea.richtext').editor();
$('textarea:visible, :text.expandable:visible').expandable();
$('.layout:visible').layout();
$('.toggleable').toggleable();
$('[data-widths]:visible').widthAware();

// Visual page layout editor.
$('textarea.pageLayout').liveInit(function() {

    var $textarea = $(this);
    var pageId = $textarea.attr('data-pageid');
    var layout;
    var openSettings;
    var isSettingsOpen = false;
    var updateSection;

    var $visual = $('<div/>', { 'class': 'visualPageLayout' });
    $visual.bind('updateJson', function() {
        $textarea.val(JSON.stringify(layout, null, 2));
    });

    var updateVisual = function() {
        $visual.empty();
        updateSection($visual, layout.outermostSection);
        $visual.find('.section > .hat').hide();
        $visual.trigger('updateJson');
        $visual.layout();
    };

    var $window = $(window);
    $visual.mousemove($.throttle(100, function(event) {
        if (!isSettingsOpen) {
            var $all = $visual.find('.section > .hat');
            var $over;
            var element = document.elementFromPoint(event.pageX - $window.scrollLeft(), event.pageY - $window.scrollTop());
            if (element) {
                $over = $(element).closest('.section').find('> .hat');
                $all = $all.not($over);
            }
            $all.hide();
            if ($over) {
                $over.show();
            }
        }
    }));

    // Updates the visual editor according to the given definitions.
    updateSection = function($container, definition, parentDefinition) {

        var $section = $('<div/>', { 'class': 'section', 'data-flex': 1 });
        definition.page = definition.page || pageId;
        $section.data('definition', definition);

        var $hat = $('<div/>', { 'class': 'hat' });
        var $heading = $('<div/>', { 'class': 'heading' });
        var $content = $('<div/>', { 'class': 'content', 'data-flex': 1 });

        if (parentDefinition) {
            var children = parentDefinition.children;
            if (children) {
                var childrenLength = children.length;
                if (childrenLength > 1) {

                    var moveButtons = { };
                    $.each([ 'Up', 'Down', 'Left', 'Right' ], function(index, direction) {
                        $hat.append(moveButtons[direction] = $('<img/>', {
                            'class': 'move' + direction + 'Button',
                            'src': CONTEXT_PATH + 'style/icon/arrow_' + direction.toLowerCase() + '.png'
                        }));
                    });

                    moveButtons.Up.add(moveButtons.Left).click(function() {
                        for (var i = 1; i < childrenLength; ++ i) {
                            if (children[i] === definition) {
                                var before = children[i - 1];
                                children[i - 1] = definition;
                                children[i] = before;
                                updateVisual();
                                break;
                            }
                        }
                    });

                    moveButtons.Down.add(moveButtons.Right).click(function() {
                        for (var i = 0; i < childrenLength - 1; ++ i) {
                            if (children[i] === definition) {
                                var after = children[i + 1];
                                children[i + 1] = definition;
                                children[i] = after;
                                updateVisual();
                                break;
                            }
                        }
                    });
                }
            }
        }

        $hat.append($('<a/>', {
            'class': 'settingsButton',
            'text': 'Settings',
            'click': function() {
                openSettings($(this), definition);
                return false;
            }
        }));

        var $removeButton = $('<img/>', {
            'class': 'removeButton',
            'src': CONTEXT_PATH + 'style/icon/delete.png',
            'text': 'Remove',
            'click': function() {
                if ($section.is('.toBeRemoved')) {
                    definition._isIgnore = false;
                    $visual.trigger('updateJson');
                    $section.removeClass('toBeRemoved');
                    $(this).text('Remove').attr('src', CONTEXT_PATH + 'style/icon/delete.png');
                } else {
                    definition._isIgnore = true;
                    $visual.trigger('updateJson');
                    $section.addClass('toBeRemoved');
                    $(this).text('Restore').attr('src', CONTEXT_PATH + 'style/icon/add.png');
                }
            }
        });
        $hat.append($removeButton);
        if (definition._isIgnore) {
            $removeButton.click();
        }

        var isContainer = true;
        if (definition._type === 'com.psddev.cms.db.HorizontalContainerSection') {
            $section.add($content).addClass('horizontal');
        } else if (definition._type === 'com.psddev.cms.db.VerticalContainerSection') {
            $section.add($content).addClass('vertical');
        } else {
            if (definition._type === 'com.psddev.cms.db.MainSection') {
                $section.addClass('main');
            }
            isContainer = false;
        }

        if (definition.isShareable) {
            $section.addClass('shared');
        }

        var $name = $('<div/>', { 'class': 'name' });
        if (definition.name) {
            $name.text(definition.name);
        } else {
            $name.addClass('anonymous');
            $name.text('Unnamed ' + (isContainer ? 'Container' : 'Section'));
        }
        $heading.append($name);

        if (definition._type === 'com.psddev.cms.db.ContentSection') {
            var $contentButton = $('<a/>', {
                'class': 'contentButton',
                'text': definition.contentTypeLabel && definition.contentLabel ? definition.contentTypeLabel + ': ' + definition.contentLabel : 'Select Content'
            });
            if ($container.closest('.contentForm').length > 0 && definition.content) {
                var href = location.href.replace(/&contentId=[^&]*/, '');
                href = href.replace(/\?contentId=[^&]*/, '?');
                href += '&contentId=' + definition.content;
                $contentButton.attr('href', href);
                $contentButton.attr('target', '_top');
            } else {
                $contentButton.attr('href', CONTEXT_PATH + 'content/sectionContent.jsp?id=' + (definition.content || ''));
                $contentButton.attr('target', 'contentSectionContent');
            }
            $heading.append($contentButton);
            if (definition.contentTypeLabel && definition.contentLabel) {
                $heading.append($('<a/>', {
                    'class': 'removeContentButton',
                    'text': 'Remove',
                    'click': function() {
                        definition.content = null;
                        definition.contentTypeLabel = null;
                        definition.contentLabel = null;
                        updateVisual();
                    }
                }));
            }
        }

        $section.append($hat);
        $section.append($heading);
        $section.append($content);
        $container.append($section);

        if (isContainer) {
            $heading.append($('<span/>', {
                'class': 'addButton',
                'click': function() {
                    var childDefinition = { '_type': 'com.psddev.cms.db.ScriptSection' };
                    (definition.children = definition.children || [ ]).push(childDefinition);
                    updateVisual();
                }
            }));

            if (definition.children) {
                $.each(definition.children, function(index, childDefinition) {
                    updateSection($content, childDefinition, definition);
                });
            }
        }

        if (definition._type === 'com.psddev.cms.db.MainSection') {
            $section.parentsUntil('.visualPageLayout').add($section).filter('[data-flex]').attr('data-flex', 2.5);
        }
    };

    // Opens the settings popup.
    openSettings = function($button, definition) {

        var $settings = $('#sectionSettings');
        if ($settings.length == 0) {
            var engines = '<option value=""></option><option value="JSP">JSP</option><option value="RawText">Raw Text</option>';
            $settings = $(
                '<div id="sectionSettings">' +
                    '<h1>Section: <strong class="name"></strong></h1>' +
                    '<div class="inputContainer">' +
                        '<div class="label">Shareable?</div>' +
                        '<div class="smallInput"><input name="isShareable" type="checkbox"></div>' +
                    '</div>' +
                    '<div class="inputContainer">' +
                        '<div class="label">Cache Duration (in Milliseconds)</div>' +
                        '<div class="smallInput"><input name="cacheDuration" type="text"></div>' +
                    '</div>' +
                    '<div class="inputContainer">' +
                        '<div class="label">Type</div>' +
                        '<div class="smallInput"><select class="toggleable" name="_type">' +
                            '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .hc" value="com.psddev.cms.db.HorizontalContainerSection">Container (Horizontal)</option>' +
                            '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .vc" value="com.psddev.cms.db.VerticalContainerSection">Container (Vertical)</option>' +
                            '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .s" value="com.psddev.cms.db.ScriptSection">Script</option>' +
                            '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .c" value="com.psddev.cms.db.ContentSection">Script (with Content)</option>' +
                            '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .s" value="com.psddev.cms.db.MainSection">Script (with Main Content)</option>' +
                        '</select></div>' +
                    '</div>' +
                    '<div class="inputContainer i hc vc s c">' +
                        '<div class="label">Name</div>' +
                        '<div class="smallInput"><input name="name" type="text"></div>' +
                    '</div>' +
                    '<div class="inputContainer i s c">' +
                        '<div class="label">Engine</div>' +
                        '<div class="smallInput"><select name="engine">' + engines + '</select></div>' +
                    '</div>' +
                    '<div class="inputContainer i s c">' +
                        '<div class="label">Script</div>' +
                        '<div class="smallInput"><input name="script" type="text"></div>' +
                    '</div>' +
                    '<div class="inputContainer i hc vc">' +
                        '<div class="label">Begin Engine</div>' +
                        '<div class="smallInput"><select name="beginEngine">' + engines + '</select></div>' +
                    '</div>' +
                    '<div class="inputContainer i hc vc">' +
                        '<div class="label">Begin Script</div>' +
                        '<div class="smallInput"><input name="beginScript" type="text"></div>' +
                    '</div>' +
                    '<div class="inputContainer i hc vc">' +
                        '<div class="label">End Engine</div>' +
                        '<div class="smallInput"><select name="endEngine">' + engines + '</select></div>' +
                    '</div>' +
                    '<div class="inputContainer i hc vc">' +
                        '<div class="label">End Script</div>' +
                        '<div class="smallInput"><input name="endScript" type="text"></div>' +
                    '</div>' +
                    '<div class="buttons">' +
                        '<a class="button continueButton" href="#">Continue Editing</a>' +
                    '</div>' +
                '</div>'
            );

            $settings.find('.continueButton').click(function() {
                $settings.popup('close');
                return false;
            });

            $('body').append($settings);
            $settings.popup();
        }

        $settings.popup('source', $button);
        $settings.popup('open');
        isSettingsOpen = true;

        $settings.find('> h1 > .name').text(definition.name || 'Unnamed');
        $.each('isShareable cacheDuration _type name engine script beginEngine beginScript endEngine endScript'.split(' '), function(index, name) {
            var $input = $settings.find(':input[name=' + name + ']');
            if ($input.is(':checkbox')) {
                if (definition[name]) {
                    $input.attr('checked', 'checked');
                } else {
                    $input.removeAttr('checked');
                }
            } else {
                $input.val(definition[name] || '');
            }
            $input.change();
        });

        var $popup = $settings.popup('container');
        $popup.unbind('close.section');
        $popup.bind('close.section', function() {
            $.each('isShareable cacheDuration _type name engine script beginEngine beginScript endEngine endScript'.split(' '), function(index, name) {
                var $input = $settings.find(':input[name=' + name + ']');
                if ($input.is(':checkbox')) {
                    definition[name] = $input.is(':checked');
                } else if (name === 'cacheDuration') {
                    definition[name] = $input.val() || 0;
                } else {
                    definition[name] = $input.val();
                }
            });
            updateVisual();
            isSettingsOpen = false;
        });
    };

    var $tabs = $('<div/>', { 'class': 'pageLayoutTabs' });
    var $visualButton = $('<span/>', {
        'class': 'visualButton',
        'text': 'Visual',
        'click': function() {
            try {
                layout = $.parseJSON($textarea.val());
            } catch (error) {
            }
            layout = $.extend(true, { 'outermostSection': { '_type': 'com.psddev.cms.db.ScriptSection' } }, layout);
            $textarea.hide();
            $jsonButton.removeClass('selected');
            $visual.show();
            $visualButton.addClass('selected');
            updateVisual();
        }
    });
    var $jsonButton = $('<span/>', {
        'class': 'jsonButton',
        'text': 'JSON',
        'click': function() {
            $visual.hide();
            $visualButton.removeClass('selected');
            $textarea.show();
            $jsonButton.addClass('selected');
        }
    });

    $tabs.append($visualButton);
    $tabs.append($jsonButton);

    $textarea.before($tabs);
    $textarea.before($visual);
    $visualButton.click();
});

(function() {

    var previousValue;
    $('.toolHeader > .search :text').bind('keyup focus', $.throttle(500, function() {

        var $input = $(this);
        var $form = $input.closest('form');
        $form.attr('autocomplete', 'off');

        var $targetFrame = $('.frame[name=' + $form.attr('target') + ']');
        if ($targetFrame.length == 0) {
            $form.submit();

        } else {
            $targetFrame.popup('open');
            var currentValue = $input.val();
            if (previousValue !== currentValue) {
                previousValue = currentValue;
                $targetFrame.find('.searchInput :text').val($input.val());
                $targetFrame.find('form.existing').submit();
            }
        }
    }));
})();

$('.extensionContainer').each(function() {
    var $container = $(this);
    var $extensions = $container.find('> .extension');
    var save = function() {
        var params = {
            'area': $container.attr('area'),
            'name': [ ],
            'isCollapsed': [ ]
        };
        $container.find('> .extension').each(function() {
            var $ext = $(this);
            params.name.push($ext.attr('name'));
            params.isCollapsed.push($ext.is('.collapsed'));
        });
        $.queuedAjax({
            'type': 'POST',
            'url': CONTEXT_PATH + 'script/saveExtensions.jsp',
            'data': $.param(params, true)
        });
    };

    $container.find('> .extension:has(.area)').each(function() {
        $(this).append($('<span/>', {
            'class': 'collapseToggle',
            'click': function() {
                $(this).closest('.extension').toggleClass('collapsed');
                save();
            }
        }));
    });

    $container.sortable({
        'axis': 'y',
        'forcePlaceholderSize': true,
        'handle': '> .area > h1',
        'placeholder': 'placeholder',
        'update': save
    });
});

// Publication widget customizations.
$('.widget-publication').each(function() {
    var $publicationWidget = $(this);

    // Change save button label if scheduling.
    var $dateInput = $publicationWidget.find('.dateInput');
    var $saveButton = $publicationWidget.find('.saveButton');
    var oldSaveButton = $saveButton.val();
    var oldDate = $dateInput.val();
    $dateInput.change(function() {
        $saveButton.val($dateInput.val() ? (oldDate ? 'Reschedule' : 'Schedule') : oldSaveButton);
    });
    $dateInput.change();

    // Move the publication area to the top if within aside section.
    var $aside = $publicationWidget.closest('.aside');
    if ($aside.length > 0) {

        var top = $aside.offset().top;
        var width = $publicationWidget.width();
        var $parent = $publicationWidget.offsetParent();
        var right = $parent.width() + $parent.offset().left - $publicationWidget.offset().left - width;
        $publicationWidget.css({
            'margin-bottom': $aside.find('.area').css('margin-bottom'),
            'position': 'fixed',
            'right': right,
            'top': top,
            'width': width
        });

        // Push other areas down.
        $aside.css('margin-top', $publicationWidget.outerHeight(true));
    }
});

// Update repeatable labels as the user edits the related sections.
(function() {

    var $form = $('.contentForm');
    var $repeatables = $form.find('.repeatableForm');
    if ($repeatables.length == 0) {
        return;
    }

    var submitFunction = $.throttle(1000, function() {
        var $container = $(this).closest('li');

        var inputs = '_=' + (+new Date());
        $container.find(':input:not([disabled])').each(function() {
            var $input = $(this);
            inputs += '&' + encodeURIComponent($input.attr('name')) + '=' + encodeURIComponent($input.val());
        });

        if ($container.data('lastInputs') !== inputs) {
            $container.data('lastInputs', inputs);

            var id = $container.find('> :hidden[name$=.id]').val();
            inputs += '&id=' + id;
            inputs += '&typeId=' + $container.find('> :hidden[name$=.typeId]').val();

            $.ajax({
                'data': inputs,
                'type': 'post',
                'url': CONTEXT_PATH + 'content/repeatableLabels.jsp',
                'complete': function(request) {
                    $container.find('> .label').text($container.attr('data-type') + ': ' + $.parseJSON(request.responseText)[id]);
                }
            });
        }
    });

    $repeatables.find(':input:not(:text)').live('change', submitFunction);
    $repeatables.find(':text, textarea').live('keyup', submitFunction);
})();

// Add an edit link next to an input that contain an object ID.
(function() {

var globalTargetIndex = 0;
$('[class!=template] > :input.objectId').liveInit(function() {
    var $input = $(this);

    // Make sure that there's only one frame target per form.
    var $form = $input.closest('form');
    var target = $form.data('objectId-target');
    if (!target) {
        ++ globalTargetIndex;
        target = 'objectId-' + globalTargetIndex;
        $form.data('objectId-target', target);
    }

    var $selectButton;
    var $clearButton;
    var $editButton = $('<a/>', {
        'class': 'editObjectId',
        'target': target,
        'text': 'Edit',
        'click': function() {
            if (!$editButton.attr('href')) {
                return false;
            } else if ($editButton.closest('.toBeRemoved').length > 0) {
                return false;
            } else if ($editButton.is('.toBeRemoved')) {
                return false;
            } else {
                return true;
            }
        }
    });

    // Replace the text box with a custom drop down.
    if ($input.is(':text')) {
        var searcherPath = $input.attr('data-searcher-path') || (CONTEXT_PATH + 'content/objectId.jsp');
        var typeIds = $input.attr('data-typeIds');

        $selectButton = $('<a/>', {
            'class': 'selectObjectId',
            'href': searcherPath + '?p=' + $input.attr('data-pathed') + '&' + (typeIds ? $.map(typeIds.split(','), function(typeId) { return 'rt=' + typeId; }).join('&') : '') + "&aq=" + encodeURIComponent($input.attr('data-additional-query') || ''),
            'target': target
        });

        $selectButton.bind('updatePreview', function() {
            var preview = $input.attr('data-preview');
            if (preview) {
                $selectButton.html($('<img/>', {
                    'src': preview
                }));
            } else {
                $selectButton.text($input.attr('data-label') || '\u00a0');
            }
        });

        $input.before($selectButton);
        $input.after($editButton);
        $editButton.hide();
        $input.hide();
        $selectButton.trigger('updatePreview');

        // Add clear button when not part of repeatable (which provides
        // the functionally equivalent remove button).
        if ($input.closest('.repeatableObjectId').length == 0) {
            var previousValue;
            $clearButton = $('<a/>', {
                'class': 'clearObjectId',
                'text': 'Clear',
                'click': function() {

                    var currentValue = $input.val();
                    if (currentValue) {
                        previousValue = currentValue;
                        $selectButton.addClass('toBeRemoved');
                        $editButton.addClass('toBeRemoved');
                        $clearButton.addClass('restore');
                        $clearButton.text('Restore');
                        $input.val('');
                        $input.change();

                    } else {
                        $selectButton.removeClass('toBeRemoved');
                        $editButton.removeClass('toBeRemoved');
                        $clearButton.removeClass('restore');
                        $clearButton.text('Clear');
                        $input.val(previousValue);
                        $input.change();
                    }

                    return false;
                }
            });
            $editButton.after($clearButton);
        }

    // Just add the edit button if drop down already.
    } else {
        $input.after($editButton);
        $input.after(' ');
    }

    // Update visual whenever input changes.
    $input.change(function() {
        if ($selectButton) {
            $selectButton.trigger('updatePreview');
        }

        var objectId = $input.val();
        if (objectId) {
            $editButton.attr('href', CONTEXT_PATH + 'content/objectIdEdit.jsp?id=' + objectId);
            $editButton.show();
            if ($clearButton) {
                $clearButton.show();
            }

        } else if ($clearButton && !$clearButton.is('.restore')) {
            $editButton.hide();
            $clearButton.hide();

        } else if (!$clearButton) {
            $editButton.hide();
        }
    });
    $input.change();
});

})();

(function() {

    var $window = $(window);
    var $inputs = $(':input');

    var $label = $('<div/>', { 'class': 'focusLabel' });
    $(document.body).append($label);

    $inputs.live('focus', function() {

        var $parents = $(this).parentsUntil('form');
        $parents.addClass('focused');

        var onScroll = $.throttle(100, function() {
            var labelText = '';
            for (var i = $parents.length - 1; i >= 0; -- i) {

                var $parent = $($parents[i]);
                if ($parent.offset().top > $window.scrollTop() + 100) {
                    if (labelText) {
                        var parentPosition = $parent.position();
                        $label.text(labelText);
                        $label.show();
                        return
                    } else {
                        break;
                    }
                }

                var parentLabel = $parent.find('> .label').text();
                if (parentLabel) {
                    if (labelText) {
                        labelText += ' \u2192 ';
                    }
                    labelText += parentLabel;
                }
            }

            $label.hide();
        });

        $window.bind('scroll.focus', onScroll);
        onScroll();
    });

    $inputs.live('blur', function() {
        $label.hide();
        $(this).parents('.focused').removeClass('focused');
        $window.unbind('.focus');
    });

    $(document.activeElement).focus();
})();

// Image editor.
$('.imageEditor').liveInit(function() {

    var $editor = $(this);
    var $image = $editor.find('> img');

    $editor.find('.width').hide();
    $editor.find('.height').hide();

    var $imageCopy = $('<img/>', {
        'css': { 'display': 'none' },
        'load': function() {
            var $image = $(this);
            $editor.find('.width > :input').val($image.width());
            $editor.find('.height > :input').val($image.height());
            $image.remove();
        }
    });
    $('body').append($imageCopy);
    $imageCopy.attr('src', $image.attr('src'));

    var $coverTop = $('<div/>', {
        'class': 'cropCover',
        'css': {
            'display': 'none',
            'left': '0',
            'position': 'absolute',
            'top': '0'
        },
        'click': function() {
            $editor.find('.cropBox').hide();
            $coverTop.hide();
            $coverLeft.hide();
            $coverRight.hide();
            $coverBottom.hide();
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
        $coverTop.css({
            'display': 'block',
            'height': bounds.top + 'px',
            'right': 0
        });
        $coverLeft.css({
            'display': 'block',
            'height': bounds.height + 'px',
            'top': bounds.top + 'px',
            'width': bounds.left + 'px'
        });
        $coverRight.css({
            'display': 'block',
            'height': bounds.height + 'px',
            'left': (bounds.left + bounds.width) + 'px',
            'right': 0,
            'top': bounds.top + 'px'
        });
        $coverBottom.css({
            'display': 'block',
            'bottom': 0,
            'top': (bounds.top + bounds.height) + 'px',
            'right': 0
        });
    };

    var $crops = $editor.find('.crops');
    var $cropSelector = $('<span/>', { 'class': 'cropSelector' });

    $crops.find('th').each(function() {

        var $th = $(this);
        var $tr = $th.closest('tr');

        var sizeWidth = parseFloat($tr.attr('data-size-width'));
        var sizeHeight = parseFloat($tr.attr('data-size-height'));
        var sizeAspectRatio = sizeWidth / sizeHeight;

        var $x = $tr.find(':input[name$=.x]');
        var $y = $tr.find(':input[name$=.y]');
        var $width = $tr.find(':input[name$=.width]');
        var $height = $tr.find(':input[name$=.height]');

        var $cropBox = $('<div/>', {
            'class': 'cropBox',
            'css': { 'position': 'absolute' }
        });

        $cropBox.hide();
        $editor.append($cropBox);

        var updateCropBox = function(callback) {
            return function(event) {

                var cropBoxPosition = $cropBox.position();
                var original = {
                    'left': cropBoxPosition.left,
                    'top': cropBoxPosition.top,
                    'width': $cropBox.width(),
                    'height': $cropBox.height(),
                    'pageX': event.pageX,
                    'pageY': event.pageY
                };

                var imageWidth = $image.width();
                var imageHeight = $image.height();

                $.drag(function(event) {
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

                    // Make sure bounds isn't outside of the image.
                    if (bounds.left < 0) {
                        bounds.left = 0;
                    } else if (bounds.left + bounds.width > imageWidth) {
                        bounds.left = imageWidth - bounds.width;
                    }
                    if (bounds.top < 0) {
                        bounds.top = 0;
                    } else if (bounds.top + bounds.height > imageHeight) {
                        bounds.top = imageHeight - bounds.height;
                    }

                    updateCover(bounds);
                    $cropBox.css(bounds);

                // Set the hidden inputs to the current bounds.
                }, function() {
                    var cropBoxPosition = $cropBox.position();
                    var cropBoxWidth = $cropBox.width();
                    var cropBoxHeight = $cropBox.height();
                    $x.val(cropBoxPosition.left / imageWidth);
                    $y.val(cropBoxPosition.top / imageHeight);
                    $width.val(cropBoxWidth / imageWidth);
                    $height.val(cropBoxWidth / sizeAspectRatio / imageHeight);
                });

                return false;
            };
        };

        $cropBox.mousedown(updateCropBox(function(event, original, delta) {
            return {
                'left': original.left + delta.x,
                'top': original.top + delta.y
            };
        }));

        $cropBox.hide();
        $editor.append($cropBox);

        $cropBox.append($('<div/>', {
            'class': 'cropResizer cropResizer-topLeft',
            'mousedown': updateCropBox(function(event, original, delta) {
                return {
                    'left': original.left + delta.constrainedX,
                    'top': original.top + delta.constrainedY,
                    'width': original.width - delta.constrainedX,
                    'height': original.height - delta.constrainedY
                };
            })
        }));
        $cropBox.append($('<div/>', {
            'class': 'cropResizer cropResizer-bottomRight',
            'mousedown': updateCropBox(function(event, original, delta) {
                return {
                    'width': original.width + delta.constrainedX,
                    'height': original.height + delta.constrainedY
                };
            })
        }));

        var $cropButton = $('<span/>', {
            'class': 'cropButton',
            'text': $th.text(),
            'click': function() {

                var imageWidth = $image.width();
                var imageHeight = $image.height();
                $editor.find('.cropBox').hide();

                var x = (parseFloat($x.val()) || 0) * imageWidth;
                var y = (parseFloat($y.val()) || 0) * imageHeight;
                var width = (parseFloat($width.val()) || 0) * imageWidth;
                var height = (parseFloat($height.val()) || 0) * imageHeight;
                if (width === 0.0 || height === 0.0) {
                    width = imageHeight * sizeAspectRatio,
                    height = imageWidth / sizeAspectRatio
                    if (width > imageWidth) {
                        width = height * sizeAspectRatio;
                    } else {
                        height = width / sizeAspectRatio;
                    }
                    x = (imageWidth - width) / 2;
                    y = (imageHeight - height) / 2;
                }

                var bounds = {
                    'left': x,
                    'top': y,
                    'width': width,
                    'height': height
                };
                updateCover(bounds);
                $cropBox.css(bounds);
                $cropBox.show();
            }
        });

        $cropSelector.append($cropButton);
    });

    $crops.before($cropSelector);
    $crops.hide();
});

// Make sure that most elements are always in view.
(function() {

var $window = $(window);
var lastScrollTop = $window.scrollTop();
$window.scroll($.throttle(100, function() {

    var scrollTop = $window.scrollTop();
    $('.leftNav, .withLeftNav > .main, .aside').each(function() {

        var $element = $(this);
        var elementTop = $element.offset().top;
        var initialElementTop = $element.data('initialElementTop');
        if (!initialElementTop) {
            initialElementTop = elementTop;
            $element.data('initialElementTop', initialElementTop);
            $element.css({
                'position': 'relative',
                'top': 0
            });
        }

        var windowHeight = $window.height();
        var elementHeight = $element.outerHeight();
        var alignToTop = function() {
            $element.stop(true);
            $element.animate({
                'top': Math.max(scrollTop, 0)
            }, 'fast');
        };

        // The element height is less than the window height,
        // so there's no need to account for the bottom alignment.
        if (initialElementTop + elementHeight < windowHeight) {
            alignToTop();
        } else {

            // The user is scrolling down.
            if (lastScrollTop < scrollTop) {
                var windowBottom = scrollTop + windowHeight;
                var elementBottom = elementTop + elementHeight;
                if (windowBottom > elementBottom) {
                    $element.stop(true);
                    $element.animate({
                        'top': Math.min(windowBottom, $('body').height()) - elementHeight - initialElementTop
                    }, 'fast');
                }

            // The user is scrolling up.
            } else if (lastScrollTop > scrollTop) {
                if (elementTop > scrollTop + initialElementTop) {
                    alignToTop();
                }
            }
        }
    });

    lastScrollTop = scrollTop;
}));

})();

// Hide non-essential items in the permissions input.
(function() {
    var toggleChildren = function() {
        var $select = $(this);
        var $list = $select.parent().find('> ul');
        if ($select.find(':selected').text() === 'Some') {
            $list.show();
        } else {
            $list.hide();
        }
    };
    var $selects = $('.inputContainer .permissions select');
    $selects.liveInit(toggleChildren);
    $selects.live('change', toggleChildren);
})();

// Soft validation based on suggested values.
$('.inputContainer .smallInput').liveInit(function() {
    var $container = $(this);
    $container.find('textarea').each(function() {
        var $input = $(this);
        var suggestedMinimum = parseInt($input.attr('data-suggested-minimum'));
        var suggestedMaximum = parseInt($input.attr('data-suggested-maximum'));
        if (suggestedMinimum || suggestedMaximum) {
            $input.keyup($.throttle(100, function() {
                setTimeout(function() {
                    var length = $input.val().length;
                    if (suggestedMinimum && length < suggestedMinimum) {
                        $container.addClass('suggestedSize suggestedSize-tooShort');
                    } else if (suggestedMaximum && length > suggestedMaximum) {
                        $container.addClass('suggestedSize suggestedSize-tooLong');
                    } else {
                        $container.removeClass('suggestedSize suggestedSize-tooShort suggestedSize-tooLong');
                    }
                }, 0);
            }));
            $input.keyup();
        }
    });
});

});
