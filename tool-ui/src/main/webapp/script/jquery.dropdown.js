/** Better drop-down list than standard SELECT. */
(function($, win, undef) {

var doc = win.document,
        $doc = $(doc);

$.plugin2('dropDown', {
    '_defaultOptions': {
        'listClass': 'dropDown-list',
        'listItemSelectedClass': 'selected',
        'inputClass': 'dropDown-input'
    },

    '_create': function(select) {
        var $select = $(select),
                options = this.option();

        // init all replacement control elements
        var $listContainer = $('<div/>', {
            'css': { 'position': 'absolute' }
        });
        var $list = $('<ul/>', { 'class': options.listClass });
        var $inputContainer = $('<span/>', {
            'css': { 'position': 'relative' }
        });
        var $input = $('<span/>', { 'class': options.inputClass });

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
            $input.removeClass('focus');
            $listContainer.hide();
            $select.blur();
        };

        // register in window object so that click outside the input
        // can be detected to close the drop down list
        $doc.click(function(e) {
            if ($listContainer.is(':visible')) {
                if (!$.contains($listContainer[0], e.target)) {
                    hideList();
                }
            } else {
                if ($.contains($inputContainer[0], e.target)) {
                    $input.addClass('focus');
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
                'class': $option.is('[selected]') ? options.listItemSelectedClass : '',
                'click': $select.is('[multiple]') ? function(e) {
                    if($option.is('[selected]')) {
                        $option.removeAttr('selected');
                        $item.removeClass(options.listItemSelectedClass);
                        $item.find(':checkbox').removeAttr('checked');
                    } else {
                        $option.attr('selected', 'selected');
                        $item.addClass(options.listItemSelectedClass);
                        $item.find(':checkbox').attr('checked', 'checked');
                    }
                    updateInput();
                    $select.change();
                } : function(e) {
                    if (!$option.is('[selected]')) {
                        $select.find('> option').removeAttr('selected');
                        $list.find('> li').removeClass(
                                options.listItemSelectedClass);
                        $listContainer.find(':checkbox').removeAttr('checked');
                        $option.attr('selected', 'selected');
                        $item.addClass(options.listItemSelectedClass);
                        $item.find(':checkbox').attr('checked', 'checked');
                        updateInput();
                        hideList();
                        $select.change();
                    }
                },
                'html': $option.text() || '&nbsp;'
            });

            var $check = $('<input/>', { 'type': 'checkbox' });
            if ($item.is('.' + options.listItemSelectedClass)) {
                $check.attr('checked', 'checked');
            }

            $item.prepend(' ');
            $item.prepend($check);
            $list.append($item);
        });

        // replace select input with custom control
        $listContainer.append($list).hide();
        $(doc.body).append($listContainer);
        $inputContainer.append($input);
        $select.before($inputContainer).hide();
    }
});

}(jQuery, window));
