/** Inputs that can be repeated. */
(function($, win, undef) {

var $win = $(win),
        cacheNonce = 0;

$.plugin2('repeatable', {
    '_defaultOptions': {
        'addButtonText': 'Add',
        'removeButtonText': 'Remove',
        'restoreButtonText': 'Restore',
        'sortableOptions': {
            'delay': 300
        }
    },

    '_create': function(container) {
        var $container = $(container),
                options = this.option();

        $container.addClass('event-input-disable');

        $container.bind('input-disable', function(event, disable) {
            $(event.target).closest('.inputContainer').toggleClass('state-disabled', disable);
        });

        // Helper for creating extra stuff on an item.
        var createExtra = function() {

            var $item = $(this);

            var type = $item.attr('data-type');
            if (type) {
                var label = $item.attr('data-label');
                var $labelHtml = $item.find(" > .repeatableLabel");
                $labelHtml.removeClass('repeatableLabel');
                if ($item.find('.message-error').length === 0) {
                    $item.addClass('collapsed');
                }
                var $label = $('<div/>', {
                    'class': 'repeatableLabel',
                    'text': type + (label ? ': ' + label : ''),
                    'data-object-id': $item.find('> input[type="hidden"][name$=".id"]').val(),
                    'data-dynamic-text': '${content.state.getType().label}: ${content.label}',
                    'click': function() {
                        var $input = $item.find('> input[data-form-fields-url]');

                        if ($input.length > 0) {
                            var url = $input.attr('data-form-fields-url');
                            var data = $input.val();

                            $input.removeAttr('data-form-fields-url');
                            $input.val('');
                            $item.toggleClass('collapsed');

                            $.ajax({
                                'type': 'POST',
                                'cache': false,
                                'url': url,
                                'data': { 'data': data },
                                'complete': function(response) {
                                    $item.append(response.responseText);
                                    $item.trigger('create');
                                    $item.trigger('load');
                                    $item.resize();
                                    $item.find(':input:first').change();
                                }
                            });

                        } else {
                            $item.toggleClass('collapsed');
                            $item.resize();
                            $item.find(':input:first').change();
                        }
                    }
                });
                if ($labelHtml.size() !== 0) {
                    $label.append($labelHtml);
                    $label.find(':input').click(function(e) {
                        e.stopPropagation();
                    });
                }
                $item.prepend($label);
            }

            $item.find(':input[name$=".toggle"]').hide();
            $item.append($('<span/>', {
                'class': 'removeButton',
                'text': options.removeButtonText
            }));
        };

        // List of inputs is contained in ul or ol (latter is sortable).
        var $list = $container.find('> ul:first');
        if ($list.length === 0) {
            $list = $container.find('> ol:first');
            if ($list.length === 0) {
                return;
            } else {
                $list.sortable(options.sortableOptions);
            }
        }

        var $templates = $();

        $list.find('> li.template, > script[type="text/template"]').each(function() {
            var $template = $(this);

            if ($template.is('li.template')) {
                $templates = $templates.add($template);

            } else {
                $templates = $templates.add($($template.text()));
            }

            $template.remove();
        });

        $list.find('> li').each(createExtra);

        var $addButtonContainer = $('<div/>', { 'class': 'addButtonContainer' });
        $container.append($addButtonContainer);

        // Enable single input mode when there's only one template and one input.
        var $singleInput;
        if (!options.addButtonText && $templates.length == 1) {
            var $inputs = $templates.find(':input');
            var $toggle = $templates.find(':input[name$=".toggle"]');
            $inputs = $inputs.not($toggle);
            if ($inputs.length == 1) {
                $singleInput = $inputs.clone();
                $singleInput.removeAttr('id');
                $singleInput.keydown(function(event) {
                    if (event.which == 13) {
                        $addButtonContainer.find('.addButton').trigger('click');
                        return false;
                    }
                });
                $addButtonContainer.append($('<input/>', {
                    'name': $toggle.attr('name'),
                    'type': 'hidden',
                    'value': $toggle.attr('value')
                }));
                $addButtonContainer.append($singleInput);
            }
        }

        // Create an add link for each template.
        var idIndex = 0;
        $templates.each(function() {
            var $template = $(this);
            $addButtonContainer.append($('<span/>', {
                'class': 'addButton',
                'text': options.addButtonText ? options.addButtonText + ' ' + ($template.attr('data-type') || 'Item') : '',
                'click': function(event, customCallback) {

                    // Don't allow blank text in single input mode.
                    if ($singleInput && !$singleInput.val()) {
                        return false;
                    }

                    var $addedItem = $template.clone();
                    $addedItem.removeClass('template');
                    $addedItem.find(':input[name$=".toggle"]').attr('checked', 'checked');

                    var callback = function() {

                        $list.append($addedItem);
                        $addedItem.each(createExtra);
                        $addedItem.removeClass('collapsed');

                        // Copy value in single input to the newly added item.
                        if ($singleInput) {
                            $addedItem.find(':input:not([name$=".toggle"])').val($singleInput.val());
                            $singleInput.val('');
                        }

                        // So that IDs don't conflict.
                        $addedItem.find('*[id]').attr('id', function(index, attr) {
                            idIndex += 1;
                            var newAttr = attr + 'r' + idIndex;
                            $addedItem.find('*[for=' + attr + ']').attr('for', newAttr);
                            $addedItem.find('*[data-show=#' + attr + ']').attr('data-show', '#'+newAttr);
                            return newAttr;
                        });

                        $addedItem.change();
                        $addedItem.trigger('create');
                        $win.resize();

                        var $select = $addedItem.find('.objectId-select');

                        if ($select.length > 0 &&
                                $select.closest('.repeatableObjectId').length > 0) {
                            // $select.click();
                        }

                        if (customCallback) {
                            customCallback.call($addedItem[0]);
                        }
                    };

                    // Load an external form if the template consists of a single link without any other inputs.
                    var $templateLink;
                    if ($addedItem.find(':input').length === 0 && ($templateLink = $addedItem.find('a')).length > 0) {
                        ++ cacheNonce;

                        $.ajax({
                            'cache': false,
                            'url': $templateLink.attr('href'),
                            'data': { '_nonce': cacheNonce },
                            'complete': function(response) {
                                $addedItem.html(response.responseText);
                                callback();
                            }
                        });
                    } else {
                        callback();
                    }

                    return false;
                }
            }));
        });

        // On remove link click:
        // - Add toBeRemoved class on the item.
        // - Disable all inputs.
        // - Change remove link text.
        $list.delegate('> li > .removeButton', 'click', function() {

            var $removeButton = $(this);
            var $item = $removeButton.closest('li');
            var $inputs = $item.find(':input');

            if ($item.is('.toBeRemoved')) {
                $item.removeClass('toBeRemoved');
                $inputs.removeAttr('disabled');
                if (options.removeButtonText) {
                    $removeButton.text(options.removeButtonText);
                }

            } else {
                $item.addClass('toBeRemoved');
                $inputs.attr('disabled', 'disabled');
                if (options.restoreButtonText) {
                    $removeButton.text(options.restoreButtonText);
                }
            }

            $item.change();
        });
    },

    'add': function(callback) {
        this.$caller.closest('.addButton').trigger('click', [ callback ]);

        return this.$caller;
    }
});

}(jQuery, window));
