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
        var shadow = $.data(this, SHADOW_DATA);

        if (shadow.display === 'block') {
            shadow.width = this.getBoundingClientRect().width;
        }
    });

    // Write the input text into the shadow.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA);
        shadow.$element.text($(this).val() + ' foo');
    });

    // Write the shadow width if the input's a block element.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA);

        if (shadow.display === 'block') {
            shadow.$element.css('width', shadow.width);
        }
    });

    // Read the shadow size.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                shadowBounds = shadow.$element[0].getBoundingClientRect();

        if (shadow.display === 'block') {
            shadow.height = shadowBounds.height;

        } else {
            shadow.width = shadowBounds.width;
        }
    });

    // Write the input size using the shadow size.
    $inputs.each(function() {
        var shadow = $.data(this, SHADOW_DATA),
                $input = $(this);

        if (shadow.display === 'block') {
            $input.css('height', shadow.height);

        } else {
            $input.css('width', shadow.width);
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
                bounds,
                boxSizing;

        // Read the input data into the shadow.
        if ($.data(input, SHADOW_DATA)) {
            return;
        }

        $input = $(input);
        display = $input.css('display');
        bounds = input.getBoundingClientRect();
        boxSizing = $input.css('box-sizing');

        $.data(input, SHADOW_DATA, {
            'display': display,
            'width': bounds.width,
            'height': bounds.height,
            '$element': $('<div/>', {
                'class': options.shadowClass,
                'text': $input.val() + ' foo',
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
            var $input = $(this),
                    shadow = $.data($input[0], SHADOW_DATA);

            $input.css('overflow', 'hidden');
            $shadowContainer.append(shadow.$element);
        });

        expand($inputs);
    }
});

$win.resize($.throttle(200, function() {
    expand($doc.find('.plugin-expandable:visible'));
}));

}(jQuery, window));
