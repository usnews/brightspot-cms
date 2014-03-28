// Expandable INPUT[type=text] and TEXTAREA.
(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc),
        $shadowContainer,
        expand,
        SHADOW_DATA = 'expandable-shadow';

// Group reads and writes together across multiple inputs to minimize forced
// synchronous layouts.
expand = function($inputs) {

    // Read the input width if it's a block element.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                bounds;

        if (shadow) {
            bounds = this.getBoundingClientRect();
            shadow.width = bounds.width;
            shadow.height = bounds.height;
        }
    });

    // Write the input text into the shadow.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                $input,
                value,
                extra;

        if (shadow) {
            $input = $(this);
            value = $input.val();
            extra = shadow.display === 'block' ? ' foo foo foo' : ' foo';

            shadow.$element.text(value ? value + extra : ($input.prop('placeholder') || extra));
        }
    });

    // Write the shadow width if the input's a block element.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA);

        if (shadow && shadow.display === 'block') {
            shadow.$element.css('width', shadow.width);
        }
    });

    // Read the shadow size.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                shadowBounds;

        if (shadow) {
            shadowBounds = shadow.$element[0].getBoundingClientRect();

            if (shadow.display === 'block') {
                shadow.height = shadowBounds.height;

            } else {
                shadow.width = shadowBounds.width;
            }
        }
    });

    // Write the input size using the shadow size.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                $input;

        if (shadow) {
            $input = $(this);

            $input.css('overflow', 'hidden');

            if (shadow.display === 'block') {
                $input.css('height', shadow.height);

            } else {
                $input.css('width', shadow.width);
            }
        }
    });
};

$.plugin2('expandable', {
    '_init': function(selector, options) {
        var plugin = this;

        plugin.$caller.delegate(selector, 'expand.expandable input.expandable', function() {
            expand($(this));
        });
    },

    '_create': function(input, options) {
        var $input,
                display,
                boxSizing;

        // Read the input data into the shadow.
        if ($.data(input, SHADOW_DATA)) {
            return;
        }

        $input = $(input);
        display = $input.css('display');
        boxSizing = $input.css('box-sizing');

        $.data(input, SHADOW_DATA, {
            'display': display,
            '$element': $('<div/>', {
                'class': options.shadowClass + ' ' + $input.attr('data-expandable-class'),
                'css': {
                    'display': display,
                    'border-width': boxSizing === 'border-box' ? '' : 0,
                    'padding': boxSizing === 'border-box' ? '' : 0
                }
            })
        });
    },

    '_createAll': function(target, selector, options) {
        var $inputs = $(target).find(selector);

        // Make sure that the shadow container is available.
        if (!$shadowContainer) {
            $(doc.body).append($shadowContainer = $('<div/>', {
                'class': 'expandable-shadows',
                'css': {
                    'left': -10000,
                    'position': 'absolute',
                    'top': 0,
                    'visibility': 'hidden'
                }
            }));
        }

        // Write the input styles and the shadow into the container.
        $inputs.each(function() {
            $shadowContainer.append($.data(this, SHADOW_DATA).$element);
        });

        expand($inputs);
    }
});

$win.resize($.throttle(200, function() {
    expand($doc.find('.plugin-expandable:visible'));
}));

}(jQuery, window));
