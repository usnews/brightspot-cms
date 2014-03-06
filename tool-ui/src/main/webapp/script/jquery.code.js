(function($, window, undefined) {

"use strict";

var $window = $(window),
        $inputs = $(),
        mirrors = [ ];

$.plugin2('code', {
    '_create': function(input) {
        $inputs = $inputs.add($(input));
    }
});

setInterval(function() {
    $inputs.each(function() {
        var $input = $(this),
                mirror;

        if ($input.is(':visible')) {
            $inputs = $inputs.not($input);

            mirror = CodeMirror.fromTextArea(this, {
                'indentUnit': 4,
                'lineNumbers': true,
                'lineWrapping': true,
                'mode': $input.attr('data-code-type')
            });

            mirror.on('change', function() {
                $input.closest('.inputContainer').scrollTop(0);
            });

            mirrors.push(mirror);
        }
    });
}, 100);

$window.resize(function() {
    $.each(mirrors, function(i, mirror) {
        mirror.refresh();
    });
});

})(jQuery, window);
