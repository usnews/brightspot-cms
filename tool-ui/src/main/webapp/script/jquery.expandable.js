/** Expandable INPUT[type=text] and TEXTAREA. */
(function($, win, undef) {

var $win = $(win),
        $allInputs = $();

$.plugin2('expandable', {
    '_defaultOptions': {
        'cssProperties': [
            'border-bottom-width', 'border-left-width', 'border-right-width',
            'border-top-width', '-moz-box-sizing', '-webkit-box-sizing',
            'box-sizing', 'font-family', 'font-size', 'font-stretch',
            'font-style', 'font-variant', 'font-weight', 'letter-spacing',
            'line-height', 'padding-bottom', 'padding-left', 'padding-right',
            'padding-top', 'white-space', 'word-spacing'
        ]
    },

    '_create': function(input) {
        var $input = $(input);

        $allInputs = $allInputs.add($input);
        $input.trigger('expand');
    },

    '_init': function(selector, options) {
        this.$caller.delegate(selector, 'expand.expandable input.expandable', function() {
            var $input = $(this),
                    $checker = $input.prevAll('.expandable-checker'),
                    properties = options.cssProperties,
                    index,
                    size,
                    property,
                    inputDisplay;

            // Create a hidden DIV that copies the input styles so that we can
            // measure the height.
            if ($checker.length === 0) {
                $checker = $('<div/>', {
                    'class': 'expandable-checker',
                    'css': {
                        'left': -10000,
                        'position': 'fixed',
                        'top': 0,
                        'visibility': 'hidden'
                    }
                });

                for (index = 0, size = properties.length; index < size; ++ index) {
                    property = properties[index];
                    $checker.css(property, $input.css(property));
                }

                $input.
                        before($checker).
                        css('overflow', 'hidden');
            }

            inputDisplay = $input.css('display');

            $checker.
                    css('display', inputDisplay).
                    text($input.val() + ' foo');

            if (inputDisplay === 'block') {
                $checker.width($input.width());
                $input.height($checker.height());

            } else {
                $input.width($checker.width());
            }
        });
    }
});

$win.resize($.throttle(200, function() {
    $allInputs.trigger('expand');
}));

}(jQuery, window));
