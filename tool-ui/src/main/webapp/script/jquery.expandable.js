/** Expandable INPUT[type=text] and TEXTAREA. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc),
        $clones;

$.plugin2('expandable', {
    '_init': function(selector, options) {
        this.$caller.delegate(selector, 'expand.expandable input.expandable', function() {
            var $input = $(this),
                    $clone = $.data(this, 'expandable-clone'),
                    inputDisplay;

            // Create a hidden DIV that copies the input styles so that we can
            // measure the height.
            if (!$clone) {
                if (!$clones) {
                    $clones = $('<div/>', {
                        'class': 'expandable-clones',
                        'css': {
                            'left': -10000,
                            'position': 'absolute',
                            'top': 0,
                            'visibility': 'hidden'
                        }
                    });

                    $(doc.body).append($clones);
                }

                $clone = $('<div/>', {
                    'class': options.cloneClass
                });

                $input.css('overflow', 'hidden');
                $clones.append($clone);
                $.data(this, 'expandable-clone', $clone);
            }

            inputDisplay = $input.css('display');

            $clone.css('display', inputDisplay);
            $clone.text($input.val() + ' foo');

            if (inputDisplay === 'block') {
                $clone.width($input.width());
                $input.height($clone.height());

            } else {
                $input.width($clone.width());
            }
        });
    },

    '_create': function(input) {
        $(input).trigger('expand');
    }
});

$win.resize($.throttle(200, function() {
    $doc.find('.plugin-expandable:visible').trigger('expand');
}));

}(jQuery, window));
