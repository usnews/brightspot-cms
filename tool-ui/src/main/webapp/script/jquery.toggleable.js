/** Toggle display of other areas. */
(function($, win, undef) {

$.plugin2('toggleable', {
    '_init': function(selector) {
        this.$caller.delegate(selector, 'toggle.toggleable change', function() {
            var $option = $(this).find(':selected'),
                    hideSelector = $option.attr('data-hide'),
                    showSelector = $option.attr('data-show'),
                    $toBeHidden,
                    $toBeShown;

            if (hideSelector) {
                $toBeHidden = $(hideSelector);
                $toBeHidden.hide();
                $toBeHidden.find(':input').attr('disabled', 'disabled');
            }

            if (showSelector) {
                $toBeShown = $(showSelector);
                $toBeShown.show();
                $toBeShown.find(':input').removeAttr('disabled');
                $toBeShown.rte('enable');
            }
        });

        this.$caller.find(selector).trigger('toggle');
    }
});

}(jQuery, window));
