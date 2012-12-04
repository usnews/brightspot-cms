(function($, win, undefined) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc);

// Standard behaviors.
$doc.repeatable('live', '.repeatableForm, .repeatableInputs, .repeatableObjectId');
$doc.repeatable('live', '.repeatableText', {
    'addButtonText': '',
    'removeButtonText': '',
    'restoreButtonText': ''
});

$doc.autoSubmit('live', '.autoSubmit');
$doc.calendar('live', ':text.date');
$doc.dropDown('live', 'select[multiple]');
$doc.expandable('live', ':text.expandable, textarea:not(.richtext)');

$doc.frame({
    'frameClassName': 'frame',
    'loadingClassName': 'loading',
    'loadedClassName': 'loaded'
});

$doc.layout('live', '.layout');
$doc.imageEditor('live', '.imageEditor');
$doc.objectId('live', ':input.objectId');
$doc.pageLayout('live', '.pageLayout');
$doc.rte('live', '.richtext');
$doc.toggleable('live', '.toggleable');
$doc.widthAware('live', '[data-widths]');

// Remove placeholder text over search input when there's text.
$doc.onCreate('.searchInput', function() {
    var $container = $(this),
            $label = $container.find('> label'),
            $input = $container.find('> :text');

    $input.bind('input', $.run(function() {
        $label.toggle(!$input.val());
    }));
});

// Automatically focus on certain elements.
$doc.onCreate('[autofocus], .autoFocus', function() {
    var focused = doc.activeElement;

    if (!focused || focused === doc || focused === doc.body) {
        $(this).focus();
    }
});

// Hide non-essential items in the permissions input.
$doc.onCreate('.inputContainer .permissions select', function() {
    var $select = $(this);

    $select.bind('change', $.run(function() {
        $select.parent().find('> ul').toggle($select.find(':selected').text() === 'Some');
    }));
});

// Show stack trace when clicking on the exception message.
$doc.delegate('.exception > *', 'click', function() {
    $(this).find('> .stackTrace').toggle();
});

// Soft validation based on suggested sizes.
$doc.delegate(':input[data-suggested-minimum]', 'input.suggested', function() {
    var $input = $(this),
            minimum = +$input.attr('data-suggested-minimum');

    $input.closest('.inputContainer').toggleClass(
            'suggestedSize-tooShort',
            !isNaN(minimum) && $input.val().length < minimum);
});

$doc.delegate(':input[data-suggested-maximum]', 'input.suggested', function() {
    var $input = $(this),
            maximum = +$input.attr('data-suggested-maximum');

    $input.closest('.inputContainer').toggleClass(
            'suggestedSize-tooLong',
            !isNaN(maximum) && $input.val().length > maximum);
});

// Make sure that most elements are always in view.
(function() {
    var lastScrollTop = $win.scrollTop();

    $win.scroll($.throttle(100, function() {
        var scrollTop = $win.scrollTop();

        $('.leftNav, .withLeftNav > .main, .aside').each(function() {
            var $element = $(this),
                    elementTop = $element.offset().top,
                    initialElementTop = $element.data('initialElementTop'),
                    windowHeight,
                    elementHeight,
                    alignToTop;

            if (!initialElementTop) {
                initialElementTop = elementTop;
                $element.data('initialElementTop', initialElementTop);
                $element.css({
                    'position': 'relative',
                    'top': 0
                });
            }

            windowHeight = $win.height();
            elementHeight = $element.outerHeight();
            alignToTop = function() {
                $element.stop(true);
                $element.animate({
                    'top': Math.max(scrollTop, 0)
                }, 'fast');
            };

            // The element height is less than the window height,
            // so there's no need to account for the bottom alignment.
            if (initialElementTop + elementHeight < windowHeight) {
                alignToTop();

            // The user is scrolling down.
            } else {
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

// Make sure that the label for the focused input is visible.
$doc.delegate(':input', 'focus', function() {
    var $parents = $(this).parentsUntil('form');

    $parents.addClass('focus focused');

    $win.bind('scroll.focus', $.run($.throttle(100, function() {
        var $label = $('.focusLabel'),
                labelText = '',
                index,
                $parent,
                parentPosition,
                parentLabel;

        for (index = $parents.length - 1; index >= 0; -- index) {
            $parent = $($parents[index]);

            if ($parent.offset().top > $win.scrollTop() + 100) {
                if (labelText) {
                    if ($label.length === 0) {
                        $label = $('<div/>', { 'class': 'focusLabel' });
                        $(doc.body).append($label);
                    }

                    parentPosition = $parent.position();
                    $label.text(labelText);
                    $label.show();
                    return;

                } else {
                    break;
                }
            }

            parentLabel = $parent.find('> .label').text();

            if (parentLabel) {
                if (labelText) {
                    labelText += ' \u2192 ';
                }
                labelText += parentLabel;
            }
        }

        $label.hide();
    })));
});

$doc.delegate(':input', 'blur', function() {
    var $label = $('.focusLabel');

    $label.hide();
    $(this).parents('.focus').removeClass('focus focused');
    $win.unbind('.focus');
});

$doc.ready(function() {
    $(doc.activeElement).focus();
});

$doc.ready(function() {
    $(this).trigger('create');

    // Add the name of the sub-selected item on the main nav.
    $('.mainNav .selected').each(function() {
        var $selected = $(this),
                $subList = $selected.find('> ul'),
                $subSelected = $subList.find('> .selected > a'),
                $selectedLink;

        if ($subSelected.length > 0) {
            $selectedLink = $selected.find('> a');
            $selectedLink.text($selectedLink.text() + ' \u2192 ' + $subSelected.text());
        }

        $subList.css('min-width', $selected.outerWidth());
    });

    // Don't allow main nav links to be clickable if they have any children.
    $('.mainNav li.isNested > a').click(function() {
        return false;
    });

    // Sync the search input in the tool header with the one in the popup.
    (function() {
        var previousValue;

        $('.toolHeader > .search :text').bind('focus input', $.throttle(500, function() {
            var $input = $(this),
                    $form = $input.closest('form'),
                    $targetFrame,
                    currentValue;

            $form.attr('autocomplete', 'off');
            $targetFrame = $('.frame[name=' + $form.attr('target') + ']');

            if ($targetFrame.length === 0) {
                $form.submit();

            } else {
                $targetFrame.popup('open');
                currentValue = $input.val();

                if (previousValue !== currentValue) {
                    previousValue = currentValue;
                    $targetFrame.find('.searchInput :text').val($input.val());
                    $targetFrame.find('form.existing').submit();
                }
            }
        }));
    }());

    // Update repeatable labels as the user edits the related sections.
    $('.contentForm .repeatableForm').delegate(':input, textarea', 'change input', $.throttle(1000, function() {
        var $container = $(this).closest('li'),
                inputs = '_=' + (+new Date()),
                id;

        $container.find(':input:not([disabled])').each(function() {
            var $input = $(this);
            inputs += '&' + encodeURIComponent($input.attr('name')) + '=' + encodeURIComponent($input.val());
        });

        if ($container.data('repeatableLabels-lastInputs') !== inputs) {
            $container.data('repeatableLabels-lastInputs', inputs);

            id = $container.find('> :hidden[name$=".id"]').val();
            inputs += '&id=' + id;
            inputs += '&typeId=' + $container.find('> :hidden[name$=".typeId"]').val();

            $.ajax({
                'data': inputs,
                'type': 'post',
                'url': CONTEXT_PATH + 'content/repeatableLabels.jsp',
                'complete': function(request) {
                    $container.find('> .label').text($container.attr('data-type') + ': ' + $.parseJSON(request.responseText)[id]);
                }
            });
        }
    }));
});

}(jQuery, window));
