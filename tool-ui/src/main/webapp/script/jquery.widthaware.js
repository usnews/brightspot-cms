/** Width-aware elements. */
(function($, win, undef) {

var $win = $(win);

$.plugin2('widthAware', {
    '_create': function(element) {
        var $element = $(element);
        var addWidthClasses = $.throttle(100, function() {
            var elementWidth = $element.width();
            $.each($element.attr('data-widths').split(/[,\s]/), function(index, width) {
                width = parseFloat(width);
                if (!isNaN(width)) {
                    var ltClass = 'lt-' + width;
                    var geClass = 'ge-' + width;
                    if (elementWidth < width) {
                        $element.addClass(ltClass);
                        $element.removeClass(geClass);
                    } else {
                        $element.removeClass(ltClass);
                        $element.addClass(geClass);
                    }
                }
            });
        });
        addWidthClasses();
        $win.resize(addWidthClasses);
    }
});

}(jQuery, window));
