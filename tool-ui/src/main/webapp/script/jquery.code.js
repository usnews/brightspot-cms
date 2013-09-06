(function($, window, undefined) {

"use strict";

var $window = $(window),
        mirrors = [ ];

$.plugin2('code', {
    '_create': function(input) {
        var $input = $(input);

        mirrors.push(CodeMirror.fromTextArea(input, {
            'indentUnit': 4,
            'lineNumbers': true,
            'mode': $input.attr('data-code-type')
        }));
    }
});

$window.resize(function() {
    $.each(mirrors, function(i, mirror) {
        mirror.refresh();
    });
});

})(jQuery, window);
