if (typeof jQuery !== 'undefined') (function($) {

// Select drop-down that can toggle other areas.
$.plugin('toggleable', {

'init': function() {
    var $selects = this; // .filter('select');

    var toggle = function() {
        var $option = $(this).find(':selected');
        var hideSelector = $option.attr('data-hide');
        if (hideSelector) {
            var $toBeHidden = $(hideSelector);
            $toBeHidden.hide();
            $toBeHidden.find(':input').attr('disabled', 'disabled');
        }
        var showSelector = $option.attr('data-show');
        if (showSelector) {
            var $toBeShown = $(showSelector);
            $toBeShown.show();
            $toBeShown.find(':input').removeAttr('disabled');
        }
    };

    $selects.live('change', toggle);
    $selects.liveInit(function() {
        toggle.call(this);
    });

    return this;
}

});

})(jQuery);
