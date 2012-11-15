if (typeof jQuery !== 'undefined') (function($) {

// Expandable input[type=text] and textarea.
$.plugin('expandable', {

'init': function(options) {

    options = $.extend({
        'checkInterval': 200,
        'cssProperties': [
            'border-bottom-width', 'border-left-width', 'border-right-width', 'border-top-width',
            'box-sizing', '-moz-box-sizing', '-webkit-box-sizing',
            'font-family', 'font-size', 'font-stretch', 'font-style', 'font-variant', 'font-weight',
            'letter-spacing', 'line-height',
            'padding-bottom', 'padding-left', 'padding-right', 'padding-top',
            'word-spacing'
        ]
    }, options);

    // Bind expand event that recalculates the size.
    this.live('expand.expandable', function() {

        var $input = $(this);
        var isTextArea = $input.is('textarea');
        var $checker = $input.data('expandable-checker');

        if (!$checker) {
            $input.css('overflow', 'hidden');
            $checker = $('<div/>', {
                'css': {
                    'left': -10000,
                    'position': 'absolute',
                    'top': 0,
                    'visibility': 'hidden'
                }
            });
            $.each(options.cssProperties, function(i, name) {
                $checker.css(name, $input.css(name));
            });
            $checker.css(isTextArea ? {
                'display': 'block',
                'white-space': 'pre-wrap',
                'width': $input.width()
            } : {
                'display': 'inline-block',
                'white-space': 'nowrap',
                'width': 'auto'
            });
            $input.data('expandable-checker', $checker);
            $input.after($checker);
        }

        $checker.text($input.val() + 'xxx');
        if (isTextArea) {
            $input.height($checker.height());
        } else {
            $input.width($checker.width());
        }
    });

    // Make sure the expand event is called initially on every element.
    this.liveInit(function() {
        $(this).trigger('expand');
    });

    // Immediately re-check on user pressing enter key.
    this.live('keyup.expandable', function(event) {
        if (event.which == 13) {
            $(this).trigger('expand');
        }
    });

    // Check every so often to cover copy/paste, etc.
    this.live('focus.expandable', function() {
        var $input = $(this);
        $input.data('expandable-interval', setInterval(function() {
            $input.trigger('expand');
        }, options.checkInterval));

    });
    this.live('blur.expandable', function() {
        var $input = $(this);
        var interval = $input.data('expandable-interval');
        if (interval) {
            $input.removeData('expandable-interval');
            clearTimeout(interval);
        }
    });

    // To maintain chainability.
    return this;
}

});

})(jQuery);
