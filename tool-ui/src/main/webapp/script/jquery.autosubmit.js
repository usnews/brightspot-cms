/** Automatic form submission on any input change. */
(function($, win, undef) {

$.plugin2('autoSubmit', {
    '_create': function(input) {
        var $input = $(input),
                $form = $(input).closest('form'),
                lastInputs,
                submitFunction,
                $targetFrame;

        submitFunction = $.throttle(500, function() {
            var inputs = $form.serialize();

            if (lastInputs !== inputs) {
                lastInputs = inputs;
                $form.submit();
            }
        });

        $input.data('autoSubmit-submitFunction', submitFunction);
        $input.attr('autocomplete', 'off');

        if ($input[0] === $form[0]) {
            $form.delegate(':input', 'input.autoSubmit change.autoSubmit', submitFunction);
            $form.delegate(':text', 'focus.autoSubmit', function() {
                $('.frame[name=' + $form.attr('target') + ']').popup('open');
                submitFunction();
            });

        } else {
            $input.bind('input.autoSubmit change.autoSubmit', submitFunction);
            $input.bind('focus.autoSubmit', function() {
                $('.frame[name=' + $form.attr('target') + ']').popup('open');
                submitFunction();
            });
        }

        $targetFrame = $('.frame[name=' + $form.attr('target') + ']:not(.loading):not(.loaded)');

        if ($targetFrame.length > 0) {
            submitFunction();
        }
    },

    'submit': function() {
        this.closestInit().data('autoSubmit-submitFunction')();
        return this;
    }
});

}(jQuery, window));
