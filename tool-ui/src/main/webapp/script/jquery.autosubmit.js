if (typeof jQuery !== 'undefined') (function($) {

// Automatic form submission on any input change.
$.plugin('autoSubmit', {

'submit': function() {
    this.closest('form').autoSubmit('data', 'submitFunction')();
    return this;
},

// Initializes the automatic form submission.
'init': function() {
    return this.liveInit(function() {

        var $form = $(this).closest('form');
        var lastInputs;
        var submitFunction = $.throttle(500, function() {
            var inputs = $form.serialize();
            if (lastInputs !== inputs) {
                lastInputs = inputs;
                $form.submit();
            }
        });

        $form.autoSubmit('data', 'submitFunction', submitFunction);
        $form.attr('autocomplete', 'off');
        $form.find(':input').live('change', submitFunction);
        $form.find(':text').live('keyup', submitFunction);
        $form.find(':text').live('focus', function() {
            $('.frame[name=' + $form.attr('target') + ']').popup('open');
            submitFunction.apply(this, arguments);
        });

        var $targetFrame = $('.frame[name=' + $form.attr('target') + ']:not(.loading):not(.loaded)');
        if ($targetFrame.length > 0) {
            $form.autoSubmit('submit');
        }
    });
}

});

})(jQuery);
