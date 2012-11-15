(function() {

if (typeof jQuery !== 'undefined') (function($) {

// replace the height/width function to take box-sizing into account
(function() {
    var old = { };
    $.each([ 'Height', 'Width' ], function(i, name) {
        var type = name.toLowerCase();
        old[type] = $.fn[type];
        $.fn[type] = function(size) {
            if (typeof size === 'number') {
                var $elem = $(this);
                if ('border-box' == ($elem.css('-moz-box-sizing') ||
                        $elem.css('-webkit-box-sizing'))) {
                    size += $elem['outer' + name]() - $elem[type]();
                }
            }
            return old[type].call(this, size);
        };
    });
})();

(function() {
    var oldPosition = $.fn.position;
    $.fn.position = function(options) {
        if (options && options.of) {
            $(this).css('position',
                    $(options.of).offsetParent().css('position'));
        }
        return oldPosition.apply(this, arguments);
    };
})();

/* */
(function() {
    var requests = { };
    var calls = { };
    $.queuedAjax = function(options) {
        var queue = options.url;
        requests[queue] = requests[queue] || [ ];
        var originalComplete = options.complete;
        options = $.extend({ }, options);
        options.complete = function(request, status) {
            if (originalComplete) {
                originalComplete(request, status);
            }
            var nextOptions = requests[queue].shift();
            calls[queue] = nextOptions ? $.ajax(nextOptions) : null;
        };

        if (!calls[queue]) {
            calls[queue] = $.ajax(options);
        } else {
            requests[queue].push(options);
        }
    };
})();

// drop down list replacement
$.fn.dropDown = function(config) {

    config = $.extend(config, {
        'listClass': 'dropDownList',
        'listItemSelectedClass': 'selected',
        'inputClass': 'dropDownInput',
        'inputFocusedClass': 'focused',
        'inputIconClass': 'dropDownInputIcon'
    });

    return this.liveInit(function() {
        var $select = $(this);
        if (!$select.is('select')) {
            return;
        }

        // init all replacement control elements
        var $listContainer = $('<div/>', {
            'css': { 'position': 'absolute' }
        });
        var $list = $('<ul/>', { 'class': config.listClass });
        var $inputContainer = $('<span/>', {
            'css': { 'position': 'relative' }
        });
        var $input = $('<span/>', { 'class': config.inputClass });
        var $inputIcon = $('<span/>', { 'class': config.inputIconClass });

        // helper to move the drop down list under the input
        var moveList = function() {
            var o = $inputContainer.offset();
            $listContainer.css({
                'left': o.left,
                'top': o.top + $inputContainer.outerHeight(true)
            });
        };

        // helper to hide the drop down list
        var hideList = function() {
            $input.removeClass(config.inputFocusedClass);
            $listContainer.hide();
            $select.blur();
        };

        // register in window object so that click outside the input
        // can be detected to close the drop down list
        $(document).click(function(e) {
            if ($listContainer.is(':visible')) {
                if (!$.contains($listContainer[0], e.target)) {
                    hideList();
                }
            } else {
                if ($.contains($inputContainer[0], e.target)) {
                    $input.addClass(config.inputFocusedClass);
                    moveList();
                    $listContainer.show();
                    $select.focus();
                }
            }
        });

        // helper to update the input
        var updateInput = function() {
            $input.html($.map($select.find('> option[selected]'), function(o) {
                return $(o).text();
            }).join(', ') || '&nbsp;');
        };
        updateInput();

        // create the drop down based on the options within select input
        $select.find('> option').each(function() {
            var $option = $(this);
            var $item = $('<li/>', {
                'class': $option.is('[selected]') ? config.listItemSelectedClass : '',
                'click': $select.is('[multiple]') ? function(e) {
                    if($option.is('[selected]')) {
                        $option.removeAttr('selected');
                        $item.removeClass(config.listItemSelectedClass);
                        $item.find(':checkbox').removeAttr('checked');
                    } else {
                        $option.attr('selected', 'selected');
                        $item.addClass(config.listItemSelectedClass);
                        $item.find(':checkbox').attr('checked', 'checked');
                    }
                    updateInput();
                    $select.change();
                } : function(e) {
                    if (!$option.is('[selected]')) {
                        $select.find('> option').removeAttr('selected');
                        $list.find('> li').removeClass(
                                config.listItemSelectedClass);
                        $listContainer.find(':checkbox').removeAttr('checked');
                        $option.attr('selected', 'selected');
                        $item.addClass(config.listItemSelectedClass);
                        $item.find(':checkbox').attr('checked', 'checked');
                        updateInput();
                        hideList();
                        $select.change();
                    }
                },
                'html': $option.text() || '&nbsp;'
            });

            var $check = $('<input/>', { 'type': 'checkbox' });
            if ($item.is('.' + config.listItemSelectedClass)) {
                $check.attr('checked', 'checked');
            }

            $item.prepend(' ');
            $item.prepend($check);
            $list.append($item);
        });

        // replace select input with custom control
        $listContainer.append($list).hide();
        $(document.body).append($listContainer);
        $inputContainer.append($input);
        $inputContainer.append($inputIcon);
        $select.before($inputContainer).hide();
    });
};

$(function() {

$('body').frame();
$('.autoSubmit').autoSubmit();
$('.tree').tree();

// Show stack trace when clicking on the exception message.
$('.exception > *').live('click', function() {
    $(this).find('> .stackTrace').toggle();
});

// Show selected nested item on the main tab.
$('.mainNav .selected').each(function() {
    var $item = $(this);
    var $list = $item.find('> ul');
    var $child = $list.find('> .selected > a');
    if ($child.length > 0) {
        var $link = $item.find('> a');
        $link.text($link.text() + ' \u2192 ' + $child.text());
    }
    $list.css('min-width', $item.width() - 9);
});

// Don't allow area links to be clickable if they have any children.
$('.mainNav li.isNested > a').click(function() {
    return false;
});

// Remove placeholder text over search text input on focus.
$('.searchInput').liveInit(function() {
    var $container = $(this);
    var $label = $container.find('> label');
    var $input = $container.find('> :text');
    $input.keydown(function() {
        setTimeout(function() {
            if ($input.val()) {
                $label.hide();
            } else {
                $label.show();
            }
        }, 0);
    });
    $input.keydown();
});

// Automatically focus on the specified element.
$('.autoFocus').liveInit(function() {
    var focused = document.activeElement;
    if (!focused || focused == document || focused == document.body) {
        $(this).focus();
    }
});

});

})(jQuery);

})();

(function() {

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
        if ($settings.length === 0) {
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
                    '</div>');

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
        if ($targetFrame.length === 0) {
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

// Update repeatable labels as the user edits the related sections.
(function() {

    var $form = $('.contentForm');
    var $repeatables = $form.find('.repeatableForm');
    if ($repeatables.length === 0) {
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

        var formAction = $input.closest('form').attr('action');
        var id = formAction.substring(formAction.indexOf('id=') + 3);

        $selectButton = $('<a/>', {
            'class': 'selectObjectId',
            'href': searcherPath + '?pt=' + encodeURIComponent(id) + '&p=' + $input.attr('data-pathed') + '&' + (typeIds ? $.map(typeIds.split(','), function(typeId) { return 'rt=' + typeId; }).join('&') : '') + "&aq=" + encodeURIComponent($input.attr('data-additional-query') || ''),
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
        if ($input.closest('.repeatableObjectId').length === 0) {
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
                        return;
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
    var $image = $editor.find('.imageEditor-image img');
    var $originalImage = $image;
    var $imageClone = $image.clone();
    var imageClone = $imageClone[0];

    var $tools = $editor.find('.imageEditor-tools ul');
    var $edit = $editor.find('.imageEditor-edit');

    var $editButton = $('<li/>', {
        'html': $('<a/>', {
            'class': 'icon-pencil',
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
            'display': 'block',
            'height': bounds.top,
            'width': imageWidth
        });
        $coverLeft.css({
            'display': 'block',
            'height': bounds.height,
            'top': bounds.top,
            'width': bounds.left
        });
        $coverRight.css({
            'display': 'block',
            'height': bounds.height,
            'left': boundsRight,
            'top': bounds.top,
            'width': imageWidth - boundsRight
        });
        $coverBottom.css({
            'display': 'block',
            'height': imageHeight - boundsBottom,
            'top': boundsBottom,
            'width': imageWidth
        });
    };

    var $sizes = $editor.find('.imageEditor-sizes table');
    var $sizeSelectors = $('<ul/>', { 'class': 'imageEditor-sizeSelectors' });

    $sizes.find('th').each(function() {
        var $th = $(this);
        var $tr = $th.closest('tr');

        var sizeWidth = parseFloat($tr.attr('data-size-width'));
        var sizeHeight = parseFloat($tr.attr('data-size-height'));
        var sizeAspectRatio = sizeWidth / sizeHeight;

        var $x = $tr.find(':input[name$=.x]');
        var $y = $tr.find(':input[name$=.y]');
        var $width = $tr.find(':input[name$=.width]');
        var $height = $tr.find(':input[name$=.height]');

        var $sizeBox = $('<div/>', {
            'class': 'imageEditor-sizeBox',
            'css': { 'position': 'absolute' }
        });

        $sizeBox.hide();
        $editor.append($sizeBox);

        var getSizeBounds = function($image) {
            var imageWidth = $image.width();
            var imageHeight = $image.height();

            var left = parseFloat($x.val()) || 0.0;
            var top = parseFloat($y.val()) || 0.0;
            var width = parseFloat($width.val()) || 0.0;
            var height = parseFloat($height.val()) || 0.0;

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
                    $sizeBox.hide();
                    $coverTop.hide();
                    $coverLeft.hide();
                    $coverRight.hide();
                    $coverBottom.hide();
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

                return false;
            }
        });

        $sizeSelectors.append($sizeButton);

        var updatePreview = function() {
            var $body = $(document.body);
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

                // Set the hidden inputs to the current bounds.
                }, function() {
                    var sizeBoxPosition = $sizeBox.position();
                    var sizeBoxWidth = $sizeBox.width();
                    var sizeBoxHeight = $sizeBox.height();

                    $x.val(sizeBoxPosition.left / imageWidth);
                    $y.val(sizeBoxPosition.top / imageHeight);
                    $width.val(sizeBoxWidth / imageWidth);
                    $height.val(sizeBoxWidth / sizeAspectRatio / imageHeight);

                    updatePreview();
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
                'src': $image.attr('src'),
                'load': function() {
                    updatePreview();
                }
            })
        });

        $sizeButton.append($sizePreview);
    });

    $sizes.before($sizeSelectors);
    $sizes.hide();
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
        var suggestedMinimum = parseInt($input.attr('data-suggested-minimum'), 10);
        var suggestedMaximum = parseInt($input.attr('data-suggested-maximum'), 10);
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

})();
