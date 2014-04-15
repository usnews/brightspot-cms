(function(globals, factory) {
    if (typeof define === 'function' && define.amd) {
        define([ 'jquery', 'bsp-utils' ], factory);

    } else {
        factory(globals.jQuery, globals.bsp_utils, globals);
    }

})(this, function($, bsp_utils, globals) {
    var SHADOW_DATA_KEY = 'shadow';

    var $d = $(document);
    var $shadowContainer;

    return bsp_utils.plugin(globals, 'bsp', 'autoExpand', {
        '_install': function() {
            var plugin = this;

            plugin._on(window, 'resize', bsp_utils.throttle(200, function() {
                plugin.expand($.makeArray($d.find('.' + plugin._itemClassName + ':visible')));
            }));
        },

        '_init': function(roots, selector) {
            var plugin = this;

            plugin._on(roots, 'input', selector, function() {
                plugin.expand(this);
            });
        },

        '_each': function(input) {
            var $input = $(input);
            var display = $input.css('display');
            var $element = $('<div/>');

            $element.attr('style', window.getComputedStyle(input, null).cssText);

            $element.css({
                'height': 'auto',
                'width': 'auto'
            });

            this._data(input, SHADOW_DATA_KEY, {
                'display': display,
                '$input': $input,
                '$element': $element
            });
        },

        '_all': function(inputs) {
            var plugin = this;
            var $inputs = $(inputs);

            if (!$shadowContainer) {
                $shadowContainer = $('<div/>', {
                    'class': plugin._classNamePrefix + 'shadows',
                    'css': {
                        'left': -10000,
                        'position': 'absolute',
                        'top': 0,
                        'visibility': 'hidden'
                    }
                });

                $(document.body).append($shadowContainer);
            }

            $inputs.each(function() {
                var $element = plugin._data(this, SHADOW_DATA_KEY).$element;

                $shadowContainer.append($element);
            });

            plugin.expand(inputs);
        },

        'expand': function(inputs) {
            var plugin = this;
            var $inputs = $(inputs);
            var shadows = [ ];

            // Group reads and writes together across multiple inputs to
            // minimize forced synchronous layouts.
            $inputs.each(function() {
                var shadow = plugin._data(this, SHADOW_DATA_KEY);

                if (shadow) {
                    shadows.push(shadow);
                }
            });

            // Read the input dimension.
            $.each(shadows, function(i, shadow) {
                var bounds = shadow.$input[0].getBoundingClientRect();

                shadow.width = bounds.width;
                shadow.height = bounds.height;
            });

            // Write the input text into the shadow.
            $.each(shadows, function(i, shadow) {
                var $input = shadow.$input;
                var value = $input.val();
                var extra = shadow.display === 'block' ? ' foo foo foo' : ' foo';

                shadow.$element.text(value ?
                        value + extra :
                        ($input.prop('placeholder') || extra));
            });

            // Write the shadow width if the input's a block element.
            $.each(shadows, function(i, shadow) {
                if (shadow.display === 'block') {
                    shadow.$element.css('width', shadow.width);
                }
            });

            // Read the shadow size.
            $.each(shadows, function(i, shadow) {
                var shadowBounds = shadow.$element[0].getBoundingClientRect();

                if (shadow.display === 'block') {
                    shadow.height = shadowBounds.height;

                } else {
                    shadow.width = shadowBounds.width;
                }
            });

            // Write the input size using the shadow size.
            $.each(shadows, function(i, shadow) {
                var $input = shadow.$input;

                $input.css('overflow', 'hidden');

                if (shadow.display === 'block') {
                    $input.css('height', shadow.height);

                } else {
                    $input.css('width', shadow.width);
                }
            });

            return $inputs;
        }
    });
});
