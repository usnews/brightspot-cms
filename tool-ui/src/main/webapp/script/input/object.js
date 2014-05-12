define([
    'jquery',
    'jquery.extra',
    'jquery.popup' ],

function($) {
    var refresh,
            SHADOW_DATA = 'objectId-shadow',
            TARGET_DATA = 'objectId-target',
            targetIndex = 0;

    refresh = function($inputs) {
        $inputs.each(function() {
            var $input = $(this),
                    shadow = $.data(this, SHADOW_DATA),
                    $select,
                    $edit,
                    $clear,
                    preview,
                    visibility,
                    label,
                    placeholder,
                    value;

            if (!shadow) {
                return;
            }

            $select = shadow.$select;
            $edit = shadow.$edit;
            $clear = shadow.$clear;
            preview = $input.attr('data-preview');
            label = $input.attr('data-label');
            visibility = $input.attr('data-visibility');
            value = $input.val();

            if (preview) {
                $select.html($('<figure/>', {
                    'html': [
                        $('<img/>', {
                            'src': preview
                        }),
                        $('<figcaption>', {
                            'text': label
                        })
                    ]
                }));

            } else {
                if (label) {
                    $select.text(label);

                    if (visibility) {
                        $select.prepend(' ');
                        $select.prepend($('<span/>', {
                            'class': 'visibilityLabel',
                            'text': visibility
                        }));
                    }

                } else {
                    placeholder = $input.attr('placeholder');

                    if (placeholder) {
                        $select.html($('<span/>', {
                            'class': 'objectId-placeholder',
                            'text': placeholder
                        }));

                    } else {
                        $select.text('\u00a0');
                    }
                }
            }

            $edit.toggle(
                    $input.attr('data-editable') !== 'false' &&
                    !!value);

            $clear.toggle(
                    $clear.is('.restore') ||
                    ($input.attr('data-clearable') !== 'false' &&
                    $input.closest('.repeatableObjectId').length === 0 &&
                    !!value));

            $edit.attr('href', CONTEXT_PATH + 'content/edit.jsp' +
                    '?id=' + (value || '') +
                    '&' + (((/[&?](variationId=[^&]+)/).exec(window.location.search) || [ ])[1] || ''));
        });
    };

    $.plugin2('objectId', {
        '_init': function(selector) {
            this.$caller.on('change.objectId', selector, function() {
                refresh($(this));
            });

            this.$caller.on('close', function(event) {
                var body = document.body,
                        $body = $(body),
                        target = $.data(body, 'objectId-target');

                if (target && $(event.target).is('[name="' + target + '"]')) {
                    $body.animate({
                        'scrollTop': $.data(body, 'objectId-scrollTop')
                    });

                    $body.removeClass('objectEditing');
                    $.removeData(body, 'objectId-target');
                    $.removeData(body, 'objectId-scrollTop');
                }
            });
        },

        '_create': function(input) {
            var $input,
                    $form,
                    target,
                    typeIds,
                    formAction,
                    $select,
                    $edit,
                    $clear,
                    shadow;

            if ($.data(input, SHADOW_DATA)) {
                return;
            }

            $input = $(input);
            $form = input.form ? $(input.form) : $input.closest('form');

            // Make sure that there's only one frame target per form.
            target = $.data($form[0], TARGET_DATA);

            if (!target) {
                ++ targetIndex;
                target = 'objectId-' + targetIndex;
                $.data($form[0], TARGET_DATA, target);
            }

            typeIds = $input.attr('data-typeIds');
            formAction = $form.attr('action');

            $select = $('<a/>', {
                'class': 'objectId-select',
                'target': target,
                'click': function() { return !$(this).is('.state-disabled'); },
                'href': ($input.attr('data-searcher-path') || (CONTEXT_PATH + 'content/objectId.jsp')) +
                        '?pt=' + encodeURIComponent((/id=([^&]+)/.exec(formAction) || [ ])[1] || '') +
                        '&p=' + encodeURIComponent($input.attr('data-pathed')) +
                        '&' + (typeIds ? $.map(typeIds.split(','), function(typeId) { return 'rt=' + typeId; }).join('&') : '') +
                        '&aq=' + encodeURIComponent($input.attr('data-additional-query') || '') +
                        '&sg=' + encodeURIComponent($input.attr('data-suggestions') || '')
            });

            $edit = $('<a/>', {
                'class': 'objectId-edit',
                'target': target,
                'text': 'Edit',
                'click': function() {
                    var $body = $(document.body);

                    if (!$body.is('.objectEditing')) {
                        $.data($body[0], 'objectId-scrollTop', $body.scrollTop());
                        $.data($body[0], 'objectId-target', target);
                        $body.addClass('objectEditing');
                    }

                    $body.animate({
                        'scrollTop': ($edit.closest('.inputContainer').offset().top - $('.toolHeader:visible').outerHeight(true))
                    });
                }
            });

            $clear = $('<a/>', {
                'class': 'objectId-clear',
                'text': 'Clear',
                'click': function() {
                    var $clear = $(this),
                            shadow = $.data($clear[0], SHADOW_DATA),
                            $select = shadow.$select,
                            $edit = shadow.$edit;

                    if ($input.val()) {
                        if ($input.attr('data-restorable') === 'false') {
                            $input.removeAttr('data-label');
                            $input.removeAttr('data-preview');

                        } else {
                            $select.addClass('toBeRemoved');
                            $edit.addClass('toBeRemoved');
                            $clear.addClass('restore');
                            $clear.text('Restore');
                        }

                        $input.val('');

                    } else {
                        $select.removeClass('toBeRemoved');
                        $edit.removeClass('toBeRemoved');
                        $clear.removeClass('restore');
                        $clear.text('Clear');
                        $input.val($input[0].defaultValue);
                    }

                    $input.change();
                    return false;
                }
            });

            shadow = {
                '$input': $input,
                '$select': $select,
                '$edit': $edit,
                '$clear': $clear
            };

            $.data(input, SHADOW_DATA, shadow);
            $.data($select[0], SHADOW_DATA, shadow);
            $.data($edit[0], SHADOW_DATA, shadow);
            $.data($clear[0], SHADOW_DATA, shadow);
        },

        '_createAll': function(target, selector) {
            var $inputs = $(target).find(selector);

            $inputs.each(function() {
                var $input = $(this),
                        shadow = $.data(this, SHADOW_DATA);

                $input.hide();
                $input.after(shadow.$clear);
                $input.after(shadow.$edit);
                $input.after(shadow.$select);
            });

            $inputs.bind('input-disable', function(event, disable) {
                var $input = $(this),
                        shadow = $.data(this, SHADOW_DATA);

                if (shadow) {
                    shadow.$clear.toggleClass('state-disabled', disable);
                    shadow.$select.toggleClass('state-disabled', disable);
                }
            });

            refresh($inputs);
        }
    });

    $(document).onCreate('[data-o-id]', function() {
        var $element = $(this),
                $source = $element.popup('source'),
                $input;

        if ($source &&
                ($source.is('.objectId-select') ||
                $source.is('.objectId-edit'))) {
            $input = $source.parent().find(':input.objectId');

            $input.attr('data-label', $element.attr('data-o-label'));
            $input.attr('data-preview', $element.attr('data-o-preview'));
            $input.val($element.attr('data-o-id'));
            $input.change();
        }
    });
});
