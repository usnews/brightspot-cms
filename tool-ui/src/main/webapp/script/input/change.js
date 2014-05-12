define([
    'jquery' ],

function($) {

    // Mark changed inputs.
    $(document).on('change', '.inputContainer', function() {
        var $container = $(this);
        var changed = false;

        $container.find('input, textarea').each(function() {
            if (this.defaultValue !== this.value) {
                changed = true;
                return;
            }
        });

        if (!changed) {
            $container.find('option').each(function() {
                if (this.defaultSelected !== this.selected) {
                    changed = true;
                    return;
                }
            });
        }

        $container.toggleClass('state-changed', changed);
    });
});
