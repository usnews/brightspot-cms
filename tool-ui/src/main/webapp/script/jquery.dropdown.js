/** Better drop-down list than standard SELECT. */
(function($, win, undef) {

var doc = win.document,
        $doc = $(doc),
        $openList;

$.plugin2('dropDown', {
    '_defaultOptions': {
        'classPrefix': 'dropDown-'
    },

    '_className': function(name) {
        return this.option('classPrefix') + name;
    },

    '_create': function(original) {
        var plugin = this,
                $original = $(original),
                isMultiple = $original.is('[multiple]'),
                $labelContainer,
                $label,
                $listContainer,
                $list;

        $labelContainer = $('<div/>', {
            'css': {
                'display': 'inline-block',
                'position': 'relative',
                'width': $original.outerWidth()
            }
        });

        $label = $('<a/>', {
            'class': plugin._className('label'),
            'href': '#',
            'click': function() {
                $list.trigger('dropDown-open');
                return false;
            }
        });

        $label.bind('dropDown-update', function() {
            var texts = $.map($original.find('> option[selected]'), function(option) {
                return $(option).text();
            });

            $label.html(texts.join(', ') || '&nbsp;');
        });

        $listContainer = $('<div/>', {
            'css': {
                'display': 'none',
                'position': 'absolute'
            }
        });

        $list = $('<ul/>', {
            'class': plugin._className('list')
        });

        $list.bind('dropDown-open', function() {
            var offset = $labelContainer.offset(),
                    labelLarger = $labelContainer.outerWidth(true) > $listContainer.outerWidth(true);

            $listContainer.css({
                'left': offset.left,
                'top': offset.top + $labelContainer.outerHeight(true)
            });

            $label.toggleClass(plugin._className('label-smaller'), !labelLarger);
            $label.toggleClass(plugin._className('label-larger'), labelLarger);
            $label.addClass('focus');

            $list.toggleClass(plugin._className('list-smaller'), labelLarger);
            $list.toggleClass(plugin._className('list-larger'), !labelLarger);

            $list.find('> li').removeClass('hover');
            $list.find(isMultiple ? '> li:first' : '> li:has(:checked)').addClass('hover');

            if ($openList) {
                $openList.trigger('dropDown-close');
            }

            $openList = $list;
            $listContainer.show();
            $original.focus();
        });

        $list.bind('dropDown-close', function() {
            $label.removeClass('focus');

            $openList = null;
            $listContainer.hide();
            $original.blur();
        });

        $list.bind('dropDown-hover', function(event, $item) {
            $list.find('> li').removeClass('hover');

            if ($item) {
                $item.addClass('hover');
            }
        });

        // Detect clicks within the window to toggle the list properly.
        $doc.click(function(event) {
            if ($listContainer.is(':visible') &&
                    !$.contains($listContainer[0], event.target)) {
                $list.trigger('dropDown-close');
            }
        });

        // Create the list based on the options in the original input.
        $original.find('> option').each(function() {
            var $option = $(this),
                    $item,
                    $check;

            $item = $('<li/>', {
                'class': plugin._className('listItem'),
                'html': $option.text() || '&nbsp;'
            });

            $check = $('<input/>', {
                'type': isMultiple ? 'checkbox' : 'radio'
            });

            if ($option.is('[selected]')) {
                $check.attr('checked', 'checked');
            }

            $item.mouseenter(function() {
                $list.trigger('dropDown-hover', [ $item ]);
            });

            $item.mouseleave(function() {
                $list.trigger('dropDown-hover');
            });

            $item.click(isMultiple ? function() {
                if ($option.is('[selected]')) {
                    $option.removeAttr('selected');
                    $item.find(':checkbox').removeAttr('checked');

                } else {
                    $option.attr('selected', 'selected');
                    $item.find(':checkbox').attr('checked', 'checked');
                }

                $label.trigger('dropDown-update');
                $original.change();

                return false;

            } : function() {
                if (!$option.is('[selected]')) {
                    $original.find('> option').removeAttr('selected');
                    $listContainer.find(':radio').removeAttr('checked');

                    $option.attr('selected', 'selected');
                    $check.attr('checked', 'checked');

                    $label.trigger('dropDown-update');
                    $original.change();
                }

                $list.trigger('dropDown-close');

                return false;
            });

            $item.prepend(' ');
            $item.prepend($check);
            $list.append($item);
        });

        // Replace input with the custom control.
        $label.trigger('dropDown-update');
        $labelContainer.append($label);
        $original.before($labelContainer);
        $original.hide();

        $listContainer.append($list);
        $(doc.body).append($listContainer);
    }
});

$doc.keydown(function(event) {
    var which,
            isUp,
            $hover,
            $newHover;

    if ($openList) {
        which = event.which;
        isUp = which === 38;

        if (isUp || which === 40) {
            $hover = $openList.find('> .hover').eq(0);

            if ($hover.length === 0) {
                $hover = $openList.find('> li:first');
            }

            if ($hover.length > 0) {
                $hover = $hover[isUp ? 'prev' : 'next']();

                if ($hover.length > 0) {
                    $openList.trigger('dropDown-hover', [ $hover ]);
                }
            }

            return false;

        } else if (which === 13 || which === 32) {
            $openList.find('> .hover').click();

            return false;

        } else if (which === 27) {
            $openList.trigger('dropDown-close');

            return false;
        }
    }

    return true;
});

}(jQuery, window));
