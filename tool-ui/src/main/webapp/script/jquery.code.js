(function($, window, undefined) {

"use strict";

var $window = $(window),
        mirrors = [ ];

$.plugin2('code', {
    '_create': function(input) {
        var $input = $(input),
                mirror;

        mirror = CodeMirror.fromTextArea(input, {
            'indentUnit': 4,
            'lineNumbers': true,
            'mode': $input.attr('data-code-type')
        });

        mirror.on('change', function() {
            $input.closest('.inputContainer').scrollTop(0);
        });

        mirrors.push(mirror);
    }
});

$window.resize(function() {
    $.each(mirrors, function(i, mirror) {
        mirror.refresh();
    });
});

})(jQuery, window);
