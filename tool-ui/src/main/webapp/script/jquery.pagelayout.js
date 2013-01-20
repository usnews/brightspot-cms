/** Visual page layout editor. */
(function($, win, undef) {

var doc = win.document,
        $win = $(win);

$.plugin2('pageLayout', {
    '_create': function(textarea) {
        var $textarea = $(textarea),
                pageId = $textarea.attr('data-pageid'),
                layout,
                $visual,
                createVisualSection,
                $tabs,
                $visualButton,
                $textareaButton;

        $visual = $('<div/>', { 'class': 'pageLayout-visual' });

        $visual.bind('populateTextarea', function() {
            $textarea.val(JSON.stringify(layout, null, 2));
        });

        $visual.bind('updateVisual', function() {
            $visual.empty();
            createVisualSection($visual, layout.outermostSection);
            $visual.trigger('populateTextarea');
        });

        // Creates the visual editor according to the definition.
        createVisualSection = function($container, definition, parentDefinition) {
            var $section,
                    $controls,
                    $heading,
                    $content,
                    children,
                    childrenLength,
                    $moveButtons,
                    $removeButton,
                    isContainer,
                    $name;

            $section = $('<div/>', { 'class': 'section' });
            $controls = $('<div/>', { 'class': 'controls' });
            $heading = $('<div/>', { 'class': 'heading' });
            $content = $('<div/>', { 'class': 'content' });

            definition.page = definition.page || pageId;
            $.data($section[0], 'definition', definition);

            // Create the movement controls if within another section.
            if (parentDefinition) {
                children = parentDefinition.children;

                if (children) {
                    childrenLength = children.length;

                    if (childrenLength > 1) {
                        $moveButtons = { };

                        $.each([ 'Up', 'Down', 'Left', 'Right' ], function(index, direction) {
                            $controls.append($moveButtons[direction] = $('<span/>', {
                                'class': 'move' + direction + 'Button',
                                'text': direction
                            }));
                        });

                        $moveButtons.Up.add($moveButtons.Left).click(function() {
                            var index,
                                    before;

                            for (index = 1; index < childrenLength; ++ index) {
                                if (children[index] === definition) {
                                    before = children[index - 1];
                                    children[index - 1] = definition;
                                    children[index] = before;
                                    $visual.trigger('updateVisual');
                                    break;
                                }
                            }
                        });

                        $moveButtons.Down.add($moveButtons.Right).click(function() {
                            var index,
                                    after;

                            for (index = 0; index < childrenLength - 1; ++ index) {
                                if (children[index] === definition) {
                                    after = children[index + 1];
                                    children[index + 1] = definition;
                                    children[index] = after;
                                    $visual.trigger('updateVisual');
                                    break;
                                }
                            }
                        });
                    }
                }
            }

            $controls.append($('<a/>', {
                'class': 'settingsButton',
                'text': 'Settings',
                'target': 'sectionSettings',
                'href': CONTEXT_PATH + '/content/section.jsp' +
                        '?type=' + (definition._type ? encodeURIComponent(definition._type) : '') +
                        '&id=' + (definition._id ? encodeURIComponent(definition._id) : '') +
                        '&data=' + encodeURIComponent(JSON.stringify(definition))
            }));

            $removeButton = $('<span/>', {
                'class': 'removeButton',
                'text': 'Remove',
                'click': function() {
                    if ($section.is('.toBeRemoved')) {
                        definition._isIgnore = false;
                        $visual.trigger('populateTextarea');
                        $section.removeClass('toBeRemoved');
                        $(this).text('Remove').attr('src', CONTEXT_PATH + 'style/icon/delete.png');
                    } else {
                        definition._isIgnore = true;
                        $visual.trigger('populateTextarea');
                        $section.addClass('toBeRemoved');
                        $(this).text('Restore').attr('src', CONTEXT_PATH + 'style/icon/add.png');
                    }
                }
            });

            $controls.append($removeButton);

            if (definition._isIgnore) {
                $removeButton.click();
            }

            if (definition._type === 'com.psddev.cms.db.HorizontalContainerSection') {
                $section.add($content).addClass('horizontal');
                isContainer = true;

            } else if (definition._type === 'com.psddev.cms.db.VerticalContainerSection') {
                $section.add($content).addClass('vertical');
                isContainer = true;

            } else if (definition._type === 'com.psddev.cms.db.MainSection') {
                $section.addClass('main');
            }

            if (definition.isShareable) {
                $section.addClass('shared');
            }

            $name = $('<div/>', { 'class': 'name' });

            if (definition.name) {
                $name.text(definition.name);

            } else {
                $name.addClass('anonymous');
                $name.text('Unnamed ' + (isContainer ? 'Container' : 'Section'));
            }

            $heading.append($name);

            // Allow adding child sections within a container.
            if (isContainer) {
                $heading.append($('<span/>', {
                    'class': 'addButton',
                    'click': function() {
                        var childDefinition = { };
                        (definition.children = definition.children || [ ]).push(childDefinition);
                        $visual.trigger('updateVisual');
                    }
                }));

                if (definition.children) {
                    $.each(definition.children, function(index, childDefinition) {
                        createVisualSection($content, childDefinition, definition);
                    });
                }
            }

            $section.append($heading);
            $section.append($controls);
            $section.append($content);

            $container.append($section);
        };

        // Toggle between the visual editor and the textarea.
        $tabs = $('<div/>', { 'class': 'pageLayout-tabs' });

        $visualButton = $('<span/>', {
            'class': 'visualButton',
            'text': 'Visual',
            'click': function() {
                try {
                    layout = $.parseJSON($textarea.val());
                } catch (error) {
                }

                layout = $.extend(true, { 'outermostSection': { } }, layout);

                $textarea.hide();
                $textareaButton.removeClass('selected');
                $visual.show();
                $visualButton.addClass('selected');

                $visual.trigger('updateVisual');
            }
        });

        $textareaButton = $('<span/>', {
            'class': 'jsonButton',
            'text': 'JSON',
            'click': function() {
                $visual.hide();
                $visualButton.removeClass('selected');
                $textarea.show();
                $textareaButton.addClass('selected');
            }
        });

        $tabs.append($visualButton);
        $tabs.append($textareaButton);

        $textarea.before($tabs);
        $textarea.before($visual);

        $visualButton.click();
    },

    'definition': function(newDefinition, $popup) {
        var $section = this.$caller,
                definition = $.data($section[0], 'definition'),
                prop;

        if (newDefinition) {
            newDefinition.children = definition.children;

            for (prop in definition) {
                if (definition.hasOwnProperty(prop)) {
                    delete definition[prop];
                }
            }

            $.extend(definition, newDefinition);
            $section.trigger('updateVisual');

            return $section;

        } else {
            return definition;
        }
    }
});

}(jQuery, window));
