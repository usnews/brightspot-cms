if (typeof jQuery !== 'undefined') (function($) {

// Inline frame replacement.
var formTargetIndex = 0;
$.plugin('frame', {

// Returns the enclosing element that contains the frame.
'container': function() {
    return this.closest('.frame');
},

// Returns the source element that triggered the frame to be populated.
'source': function() {
    return this.frame('container').frame('data', '$source');
},

// Initializes the frame.
'init': function() {
    return this.each(function() {
        var $body = $(this);

        // Finds the target frame, creating one if necessary.
        var findTargetFrame = function(element, callback) {
            var $element = $(element);
            var target = $element.attr('target');

            // Standard HTML elements that can handle the target.
            if (target && $('frame[name=' + target + '], iframe[name=' + target + ']').length > 0) {
                return true;
            }

            // Skip processing on special target names.
            if (target != '_top' && target != '_blank') {

                var $frame;
                if (target == '_parent') {
                    $frame = $element.closest('.frame').parent().closest('.frame');
                } else if (target) {
                    $frame = $body.find('.frame[name=' + target + ']');
                    if ($frame.length == 0) {
                        $frame = $('<div/>', { 'class': 'frame', 'name': target });
                        $body.append($frame);
                        $frame.popup();
                    }
                } else {
                    $frame = $element.closest('.frame');
                }

                if ($frame.length > 0) {
                    return callback($element, $frame);
                }
            }

            // Natural browser event.
            return true;
        };

        // Begins loading $frame using $source.
        var beginLoad = function($frame, $source) {
            var version = ($frame.frame('data', 'loadVersion') || 0) + 1;
            var $popup = $frame.popup('container');
            $frame.add($popup).removeClass('loaded').addClass('loading');
            $frame.frame('data', 'loadVersion', version);
            $frame.frame('data', '$source', $source);

            // Source change on popup?
            var $oldSource = $frame.popup('source');
            if ($popup[0] && (!$oldSource || $oldSource[0] != $source[0]) && (!$source[0] || !$.contains($popup[0], $source[0]))) {
                $frame.popup('source', $source);
                $frame.empty();
            }

            $frame.popup('open');
            return version;
        };

        // Ends loading $frame by setting it using data.
        var endLoad = function($frame, version, data) {
            if (version >= $frame.frame('data', 'loadVersion')) {
                var $popup = $frame.popup('container');
                $frame.add($popup).removeClass('loading').addClass('loaded');
                if (typeof data === 'string') {
                    data = data.replace(/^.*?<body[^>]*>/ig, '');
                    data = data.replace(/<\/body>.*?$/ig, '');
                }
                $frame.html(data);
                $(window).resize();
                $frame.trigger('load');
            }
        };

        // Loads the page at url into the $frame.
        var loadPage = function($frame, $source, url) {
            var version = beginLoad($frame, $source);
            $.ajax({
                'cache': false,
                'url': url,
                'complete': function(response) {
                    endLoad($frame, version, response.responseText);
                }
            });
        };

        // Intercepts anchor clicks to see if it's targeted. 
        $body.find('a').live('click', function() {
            return findTargetFrame(this, function($anchor, $frame) {
                loadPage($frame, $anchor, $anchor.attr('href'));
                return false;
            });
        });

        // Intercepts form submits to see if it's targeted. 
        $body.find('form').live('submit', function() {
            return findTargetFrame(this, function($form, $frame) {

                if ($form.attr('method') === 'get') {
                    var action = $form.attr('action');
                    loadPage($frame, $form, action + (action.indexOf('?') > -1 ? '&' : '?') + $form.serialize());
                    return false;
                }

                var $isFrame = $form.find(':hidden[name=_isFrame]');
                if ($isFrame.length == 0) {
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
                if ($submitFrame.length == 0) {
                    $submitFrame = $('<iframe/>', { 'name': target });
                    $submitFrame.hide();
                    $body.append($submitFrame);
                }

                var version = beginLoad($frame, $form);
                $submitFrame.unbind('.frame');
                $submitFrame.bind('load.frame', function() {
                    endLoad($frame, version, $submitFrame.contents().find('body').html());
                    if (!hasTarget) {
                        $form.removeAttr('target');
                        setTimeout(function() { $submitFrame.remove() }, 0);
                    }
                });

                return true;
            });
        });

        // Any existing frame should be loaded.
        $body.find('.frame').liveInit(function() {
            var $frame = $(this);
            if ($frame.is(':not(.loading):not(.loaded)')) {
                var $anchor = $frame.find('a:only-child:not([target])');
                if ($anchor.length > 0) {
                    $anchor.click();
                } else {
                    $frame.find('form:only-child:not([target])').submit();
                }
            }
        });
    });
}

});

})(jQuery);
