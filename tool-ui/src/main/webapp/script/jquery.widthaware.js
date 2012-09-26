if (typeof jQuery !== 'undefined') (function($) {

// Width-aware elements.
$.plugin('widthAware', {

'init': function() {
    return this.liveInit(function() {
        var $element = $(this);
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
        $(window).resize(addWidthClasses);
    });
}

});

})(jQuery);
