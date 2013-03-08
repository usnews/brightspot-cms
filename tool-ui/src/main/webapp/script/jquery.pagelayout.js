/** Visual page layout editor. */
(function($, win, undef) {

var doc = win.document,
        $win = $(win);

$.plugin2('pageLayout', {
    '_create': function(textarea) {
        var $textarea = $(textarea);
        var pageId = $textarea.attr('data-pageid');
        var layout;
        var openSettings;
        var isSettingsOpen = false;
        var updateSection;

        var $visual = $('<div/>', { 'class': 'pageLayout-visual' });
        $visual.bind('updateJson', function() {
            $textarea.val(JSON.stringify(layout, null, 2));
        });

        var updateVisual = function() {
            $visual.empty();
            updateSection($visual, layout.outermostSection);
            $visual.find('.section > .hat').hide();
            $visual.trigger('updateJson');
        };

        $visual.mousemove($.throttle(100, function(event) {
            if (!isSettingsOpen) {
                var $all = $visual.find('.section > .hat');
                var $over;
                var element = doc.elementFromPoint(event.pageX - $win.scrollLeft(), event.pageY - $win.scrollTop());
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
                            $hat.append(moveButtons[direction] = $('<span/>', {
                                'class': 'move' + direction + 'Button',
                                'text': direction
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

            var $removeButton = $('<span/>', {
                'class': 'removeButton',
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
                $section.parentsUntil('.pageLayout-visual').add($section).filter('[data-flex]').attr('data-flex', 2.5);
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
                                '<div class="inputLabel">Shareable?</div>' +
                                '<div class="smallInput"><input name="isShareable" type="checkbox"></div>' +
                            '</div>' +
                            '<div class="inputContainer">' +
                                '<div class="inputLabel">Cache Duration (in Milliseconds)</div>' +
                                '<div class="smallInput"><input name="cacheDuration" type="text"></div>' +
                            '</div>' +
                            '<div class="inputContainer">' +
                                '<div class="inputLabel">Type</div>' +
                                '<div class="smallInput"><select class="toggleable" name="_type">' +
                                    '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .hc" value="com.psddev.cms.db.HorizontalContainerSection">Container (Horizontal)</option>' +
                                    '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .vc" value="com.psddev.cms.db.VerticalContainerSection">Container (Vertical)</option>' +
                                    '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .s" value="com.psddev.cms.db.ScriptSection">Script</option>' +
                                    '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .c" value="com.psddev.cms.db.ContentSection">Script (with Content)</option>' +
                                    '<option data-hide="#sectionSettings .i" data-show="#sectionSettings .s" value="com.psddev.cms.db.MainSection">Script (with Main Content)</option>' +
                                '</select></div>' +
                            '</div>' +
                            '<div class="inputContainer i hc vc s c">' +
                                '<div class="inputLabel">Name</div>' +
                                '<div class="smallInput"><input name="name" type="text"></div>' +
                            '</div>' +
                            '<div class="inputContainer i s c">' +
                                '<div class="inputLabel">Engine</div>' +
                                '<div class="smallInput"><select name="engine">' + engines + '</select></div>' +
                            '</div>' +
                            '<div class="inputContainer i s c">' +
                                '<div class="inputLabel">Script</div>' +
                                '<div class="smallInput"><input name="script" type="text"></div>' +
                            '</div>' +
                            '<div class="inputContainer i hc vc">' +
                                '<div class="inputLabel">Begin Engine</div>' +
                                '<div class="smallInput"><select name="beginEngine">' + engines + '</select></div>' +
                            '</div>' +
                            '<div class="inputContainer i hc vc">' +
                                '<div class="inputLabel">Begin Script</div>' +
                                '<div class="smallInput"><input name="beginScript" type="text"></div>' +
                            '</div>' +
                            '<div class="inputContainer i hc vc">' +
                                '<div class="inputLabel">End Engine</div>' +
                                '<div class="smallInput"><select name="endEngine">' + engines + '</select></div>' +
                            '</div>' +
                            '<div class="inputContainer i hc vc">' +
                                '<div class="inputLabel">End Script</div>' +
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

        var $tabs = $('<div/>', { 'class': 'pageLayout-tabs' });
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
    }
});

}(jQuery, window));
