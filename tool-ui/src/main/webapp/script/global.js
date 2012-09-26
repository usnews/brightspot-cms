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
                if ('border-box' == ($elem.css('-moz-box-sizing')
                        || $elem.css('-webkit-box-sizing'))) {
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
                'class': $option.is('[selected]')
                        ? config.listItemSelectedClass : '',
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
