if (typeof jQuery !== 'undefined') (function($) {

// Inputs that can be repeated.
$.plugin('repeatable', {

'init': function(options) {

    options = $.extend(true, {
        'addButtonText': 'Add',
        'removeButtonText': 'Remove',
        'restoreButtonText': 'Restore',
        'sortableOptions': { 'delay': 300 }
    }, options);

    // Helper for creating extra stuff on an item.
    var createExtra = function() {

        var $item = $(this);

        var type = $item.attr('data-type');
        if (type) {
            var label = $item.attr('data-label');
            var $labelHtml = $item.find(" > .label");
            $labelHtml.removeClass('label');
            $item.addClass('collapsed');
            var $label = $('<div/>', {
                'class': 'label',
                'text': type + (label ? ': ' + label : ''),
                'click': function() {
                    $item.toggleClass('collapsed');
                }
            });
            if($labelHtml.size() !== 0) {
                $label.append($labelHtml);
                $label.find(':input').click(function(e) {
                    e.stopPropagation();
                });
            }
            $item.prepend($label);
        }

        $item.find(':input[name$=.toggle]').hide();
        $item.append($('<span/>', {
            'class': 'removeButton',
            'text': options.removeButtonText
        }));
    };

    return this.liveInit(function() {
        var $container = $(this);

        // List of inputs is contained in ul or ol (latter is sortable).
        var $list = $container.find('> ul:first');
        if ($list.length == 0) {
            $list = $container.find('> ol:first');
            if ($list.length == 0) {
                return;
            } else {
                $list.sortable(options.sortableOptions);
            }
        }

        var $templates = $list.find('> li.template').remove();
        $list.find('> li').each(createExtra);

        var $addButtonContainer = $('<div/>', { 'class': 'addButtonContainer' });
        $container.append($addButtonContainer);

        // Enable single input mode when there's only one template and one input.
        var $singleInput;
        if (!options.addButtonText && $templates.length == 1) {
            var $inputs = $templates.find(':input');
            var $toggle = $templates.find(':input[name$=.toggle]');
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
                'click': function() {

                    // Don't allow blank text in single input mode.
                    if ($singleInput && !$singleInput.val()) {
                        return false;
                    }

                    var $addedItem = $template.clone();
                    $addedItem.removeClass('template');
                    $addedItem.find(':input[name$=.toggle]').attr('checked', 'checked');

                    var callback = function() {

                        $list.append($addedItem);
                        $addedItem.each(createExtra);
                        $addedItem.removeClass('collapsed');

                        // Copy value in single input to the newly added item.
                        if ($singleInput) {
                            $addedItem.find(':input:not([name$=.toggle])').val($singleInput.val());
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
                    };

                    // Load an external form if the template consists of a single link without any other inputs.
                    var $templateLink;
                    if ($addedItem.find(':input').length == 0 && ($templateLink = $addedItem.find('a')).length > 0) {
                        $.ajax({
                            'cache': false,
                            'url': $templateLink.attr('href'),
                            'complete': function(response) {
                                $addedItem.html(response.responseText);
                                callback();
                            }
                        });
                    } else {
                        callback();
                    }

                    $(window).resize();
                }
            }));
        });

        // On remove link click:
        // - Add toBeRemoved class on the item.
        // - Disable all inputs.
        // - Change remove link text.
        $list.find('> li > .removeButton').live('click', function() {

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
    });
}

});

})(jQuery);
