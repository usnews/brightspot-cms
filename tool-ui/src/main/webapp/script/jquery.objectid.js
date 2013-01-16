/** Object selector and editor. */
(function($, win, undef) {

var globalTargetIndex = 0;

$.plugin2('objectId', {
    '_create': function(input) {
        var $input = $(input);

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
        var $editButton = $input.attr('data-editable') === 'false' ? $() : $('<a/>', {
            'class': 'objectId-edit',
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
                'class': 'objectId-select',
                'href': searcherPath + '?pt=' + encodeURIComponent(id) + '&p=' + $input.attr('data-pathed') + '&' + (typeIds ? $.map(typeIds.split(','), function(typeId) { return 'rt=' + typeId; }).join('&') : '') + "&aq=" + encodeURIComponent($input.attr('data-additional-query') || ''),
                'target': target
            });

            $selectButton.bind('updatePreview', function() {
                var preview = $input.attr('data-preview'),
                        label,
                        placeholder;

                if (preview) {
                    $selectButton.html($('<img/>', {
                        'src': preview
                    }));

                } else {
                    label = $input.attr('data-label');

                    if (label) {
                        $selectButton.text(label);

                    } else {
                        placeholder = $input.attr('placeholder');

                        if (placeholder) {
                            $selectButton.html($('<span/>', {
                                'class': 'objectId-placeholder',
                                'text': placeholder
                            }));

                        } else {
                            $selectButton.text('\u00a0');
                        }
                    }
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
                    'class': 'objectId-clear',
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
                ($editButton.length > 0 ? $editButton : $selectButton).after($clearButton);
            }

        // Just add the edit button if drop down already.
        } else {
            $input.after($editButton);
            $input.after(' ');
        }

        // Update visual whenever input changes.
        $input.change($.run(function() {
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
        }));
    }
});

}(jQuery, window));
