/** Inline FRAME/IFRAME replacement. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        formTargetIndex = 0;

$.plugin2('frame', {
    '_init': function(selector, options) {
        var plugin = this,
                $caller = plugin.$caller,
                findTargetFrame,
                beginLoad,
                endLoad,
                loadPage;

        // Intercept anchor clicks to see if it's targeted.
        $caller.delegate('a', 'click.frame', function() {
            return plugin._findTargetFrame(this, function($anchor, $frame) {
                plugin._loadPage($frame, $anchor, $anchor.attr('href'));
                return false;
            });
        });

        // Intercept form submits to see if it's targeted.
        $caller.delegate('form', 'submit.frame', function() {
            return plugin._findTargetFrame(this, function($form, $frame) {
                if ($form.attr('method') === 'get') {
                    var action = $form.attr('action');
                    plugin._loadPage($frame, $form, action + (action.indexOf('?') > -1 ? '&' : '?') + $form.serialize());
                    return false;
                }

                var $isFrame = $form.find(':hidden[name=_isFrame]');
                if ($isFrame.length === 0) {
                    $isFrame = $('<input name="_isFrame" type="hidden" value="true"/>');
                    $form.prepend($isFrame);
                }

                // Add a target for $submitFrame later in case one doesn't exist.
                var target = $form.attr('target');
                var hasTarget = true;
                if (!target) {
                    formTargetIndex += 1;
                    target = 'frameTarget' + formTargetIndex + (+new Date());
                    $form.attr('target', target);
                    hasTarget = false;
                }

                var $submitFrame = $('iframe[name=' + target + ']');
                if ($submitFrame.length === 0) {
                    $submitFrame = $('<iframe/>', { 'name': target });
                    $submitFrame.hide();
                    $(doc.body).append($submitFrame);
                }

                var version = plugin._beginLoad($frame, $form);
                $submitFrame.unbind('.frame');
                $submitFrame.bind('load.frame', function() {
                    plugin._endLoad($frame, version, $submitFrame.contents().find('body').html());
                    if (!hasTarget) {
                        $form.removeAttr('target');
                        setTimeout(function() { $submitFrame.remove(); }, 0);
                    }
                });

                return true;
            });
        });

        // Any existing frame should be loaded.
        $caller.onCreate('.frame', function() {
            var $frame = $(this),
                    $anchor;

            plugin._initElement(this, options);

            if ($frame.is(':not(.loading):not(.loaded)')) {
                $anchor = $frame.find('a:only-child:not([target])');

                if ($anchor.length > 0) {
                    $anchor.click();

                } else {
                    $frame.find('form:only-child:not([target])').submit();
                }
            }
        });

        return $caller;
    },

    // Finds the target frame, creating one if necessary.
    '_findTargetFrame': function(element, callback) {
        var $element = $(element),
                target = $element.attr('target'),
                $frame;

        // Standard HTML elements that can handle the target.
        if (target && $('frame[name="' + target + '"], iframe[name="' + target + '"]').length > 0) {
            return true;
        }

        // Skip processing on special target names.
        if (target !== '_top' && target !== '_blank') {
            if (target === '_parent') {
                $frame = $element.frame('container').parent().frame('container');

            } else if (target) {
                $frame = $('.frame[name="' + target + '"]');

                if ($frame.length === 0) {
                    $frame = $('<div/>', { 'class': 'frame', 'name': target });
                    $(doc.body).append($frame);
                    $frame.popup();
                }

            } else {
                $frame = $element.frame('container');
            }

            if ($frame.length > 0) {
                return callback($element, $frame);
            }
        }

        // Natural browser event.
        return true;
    },

    // Begins loading $frame using $source.
    '_beginLoad': function($frame, $source) {
        var version = ($frame.data('frame-loadVersion') || 0) + 1,
                $popup = $frame.popup('container'),
                $oldSource;

        $frame.add($popup).removeClass('loaded').addClass('loading');
        $frame.data('frame-loadVersion', version);
        $frame.data('frame-$source', $source);

        // Source change on popup?
        $oldSource = $frame.popup('source');

        if ($popup[0] && (!$oldSource || $oldSource[0] != $source[0]) && (!$source[0] || !$.contains($popup[0], $source[0]))) {
            $frame.popup('source', $source);
            $frame.empty();
        }

        $frame.popup('open');

        return version;
    },

    // Ends loading $frame by setting it using data.
    '_endLoad': function($frame, version, data) {
        var $popup;

        if (version >= $frame.data('frame-loadVersion')) {
            $popup = $frame.popup('container');

            $frame.add($popup).removeClass('loading').addClass('loaded');

            if (typeof data === 'string') {
                data = data.replace(/^.*?<body[^>]*>/ig, '');
                data = data.replace(/<\/body>.*?$/ig, '');
            }

            $frame.html(data);
            $frame.trigger('load');
            $frame.trigger('create');
            $win.resize();
        }
    },

    // Loads the page at url into the $frame.
    '_loadPage': function($frame, $source, url) {
        var plugin = this,
                version = plugin._beginLoad($frame, $source),
                formData = $frame.attr('data-extra-form-data');

        $.ajax({
            'cache': false,
            'url': url + (url.indexOf('?') < 0 ? '?' : '&') + formData,
            'complete': function(response) {
                plugin._endLoad($frame, version, response.responseText);
            }
        });
    },

    // Returns the enclosing element that contains the frame.
    'container': function() {
        return this.$caller.closest('.frame');
    },

    // Returns the source element that triggered the frame to be populated.
    'source': function() {
        return this.container().data('frame-$source');
    }
});

}(jQuery, window));
