/** Inline FRAME/IFRAME replacement. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        formTargetIndex = 0;

$.plugin2('frame', {
    '_defaultOptions': {
        'frameClassName': 'dari-frame',
        'loadingClassName': 'dari-frame-loading',
        'loadedClassName': 'dari-frame-loaded',
        'bodyClassName': 'dari-frame-body',
        'setBody': function(body) {
            $(this).html(body);
        }
    },

    '_init': function(selector) {
        var plugin = this,
                $caller = plugin.$caller,
                options = plugin.option(),
                frameClassName = options.frameClassName,
                loadingClassName = options.loadingClassName,
                loadedClassName = options.loadedClassName,
                bodyClassName = options.bodyClassName,
                findTargetFrame,
                beginLoad,
                endLoad,
                loadPage;

        // Finds the target frame, creating one if necessary.
        findTargetFrame = function(element, callback) {
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
                    $frame = $('.' + frameClassName + '[name="' + target + '"]');

                    if ($frame.length === 0) {
                        $frame = $('<div/>', { 'class': frameClassName, 'name': target });
                        $(doc.body).append($frame);
                        $frame.popup();
                    }

                } else {
                    $frame = $element.frame('container');
                }

                if ($frame.length > 0 && $frame[0] !== doc) {
                    return callback($element, $frame);
                }
            }

            // Natural browser event.
            return true;
        };

        // Begins loading $frame using $source.
        beginLoad = function($frame, $source) {
            var version = ($frame.data('frame-loadVersion') || 0) + 1,
                    $popup = $frame.popup('container'),
                    $oldSource;

            $frame.add($popup).removeClass(loadedClassName).addClass(loadingClassName);
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
        };

        // Ends loading $frame by setting it using data.
        endLoad = function($frame, version, data) {
            var $popup,
                    $wrapper,
                    $bodyContainer,
                    body;

            if (version >= $frame.data('frame-loadVersion')) {
                $popup = $frame.popup('container');
                $wrapper = $('<div/>', { 'html': data });
                $bodyContainer = $wrapper.find('.' + bodyClassName + '[name="' + $frame.attr('name') + '"]');

                if ($bodyContainer.length > 0) {
                    body = $bodyContainer.text();

                } else {
                    body = $wrapper.html();
                    body = body.replace(/^.*?<body[^>]*>/ig, '');
                    body = body.replace(/<\/body>.*?$/ig, '');
                }

                $frame.add($popup).removeClass(loadingClassName).addClass(loadedClassName);
                options.setBody.call($frame[0], body);

                $frame.trigger('create');
                $frame.trigger('load');
                $win.resize();
            }
        };

        // Loads the page at url into the $frame.
        loadPage = function($frame, $source, url) {
            var plugin = this,
                    version = beginLoad($frame, $source),
                    extraFormData = $frame.attr('data-extra-form-data');

            $.ajax({
                'cache': false,
                'url': url + (url.indexOf('?') < 0 ? '?' : '&') + extraFormData,
                'complete': function(response) {
                    endLoad($frame, version, response.responseText);
                }
            });
        };

        // Intercept anchor clicks to see if it's targeted.
        $caller.delegate('a', 'click.frame', function(event) {
            return findTargetFrame(this, function($anchor, $frame) {
                loadPage($frame, $anchor, $anchor.attr('href'));
                return false;
            });
        });

        // Intercept form submits to see if it's targeted.
        $caller.delegate('form', 'submit.frame', function() {
            return findTargetFrame(this, function($form, $frame) {
                var action = $form.attr('action'),
                        extraFormData = $frame.attr('data-extra-form-data');

                if ($form.attr('method') === 'get') {
                    loadPage($frame, $form, action + (action.indexOf('?') > -1 ? '&' : '?') + $form.serialize());
                    return false;
                }

                $form.attr('action', action + (action.indexOf('?') < 0 ? '?' : '&') + extraFormData);

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

                var version = beginLoad($frame, $form);
                $submitFrame.unbind('.frame');
                $submitFrame.bind('load.frame', function() {
                    $form.attr('action', action);
                    endLoad($frame, version, $submitFrame.contents().find('body').html());
                    if (!hasTarget) {
                        $form.removeAttr('target');
                        setTimeout(function() { $submitFrame.remove(); }, 0);
                    }
                });

                return true;
            });
        });

        // Any existing frame should be loaded.
        $caller.onCreate('.' + frameClassName, function() {
            var $frame = $(this),
                    $anchor;

            plugin._initElement(this, options);

            if ($frame.is(':not(.' + loadingClassName + '):not(.' + loadedClassName + ')')) {
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

    // Returns the enclosing element that contains the frame.
    'container': function() {
        return this.closestInit();
    },

    // Returns the source element that triggered the frame to be populated.
    'source': function() {
        return this.container().data('frame-$source');
    }
});

}(jQuery, window));
