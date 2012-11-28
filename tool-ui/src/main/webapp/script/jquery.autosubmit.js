/** Automatic form submission on any input change. */
(function($, win, undef) {

$.plugin2('autoSubmit', {
    '_create': function(input) {
        var $form = $(input).closest('form'),
                lastInputs,
                submitFunction;

        submitFunction = $.throttle(500, function() {
            var inputs = $form.serialize();

            if (lastInputs !== inputs) {
                lastInputs = inputs;
                $form.submit();
            }
        });

        $form.data('autoSubmit-submitFunction', submitFunction);
        $form.attr('autocomplete', 'off');
        $form.delegate(':input', 'change', submitFunction);
        $form.delegate(':text', 'keyup', submitFunction);
        $form.delegate(':text', 'focus', function() {
            $('.frame[name=' + $form.attr('target') + ']').popup('open');
            submitFunction.apply(this, arguments);
        });

        var $targetFrame = $('.frame[name=' + $form.attr('target') + ']:not(.loading):not(.loaded)');
        if ($targetFrame.length > 0) {
            $form.autoSubmit('submit');
        }
    },

    'submit': function() {
        return this.$init.each(function() {
            $(this).closest('form').data('autoSubmit-submitFunction')();
        });
    }
});

}(jQuery, window));
