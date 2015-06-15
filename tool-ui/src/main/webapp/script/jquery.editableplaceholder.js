(function($, window, undefined) {

$.plugin2('editablePlaceholder', {
    '_init': function(selector) {
        var plugin = this;

        // NOTE:
        // There are potential conflicts between this code and other code that fetches
        // dynamic placeholders for the inputs: the placeholder attributes on the input
        // can sometimes change without warning.
        // Because of this, whenever we insert the placeholder value into the input,
        // we must store that value for later comparison (because if we attempt to
        // compare it against the placeholder value it might have been updated so it
        // will not be what we expect and this might lead to erasing the input
        // after the user enters a new value).
        // 
        function getPlaceholder($input) {
            return $input.attr('data-editable-placeholder') || $input.prop('placeholder');
        }
        
        function setEditablePlaceholder($input, value) {
            $input.data('editable-placeholder-value', value);
        }
        
        function removeEditablePlaceholder($input) {
            $input.removeData('editable-placeholder-value');
        }
        
        function getEditablePlaceholder($input) {
            return $input.data('editable-placeholder-value');
        }

        // If the input is empty and the user moves mouse over the input or focus on the input,
        // put the placeholder value into the input.
        plugin.$caller.on('focus.editablePlaceholder mouseenter.editablePlaceholder', selector, function() {
            
            var $input, placeholder;
            
            $input = $(this);
            removeEditablePlaceholder($input);

            if (!$input.val()) {
                
                $input.data('editable-placeholder-empty-before-focus', true);
                placeholder = getPlaceholder($input);
                setEditablePlaceholder($input, placeholder);
            }
        });

        // If the user changes the input then mark the input so we don't change it back to the empty value
        plugin.$caller.on('input.editablePlaceholder', selector, function() {
            var $input;
            $input = $(this);
            $input.removeData('editable-placeholder-empty-before-focus');
        });

        // If the user leaves the input field, remove the placeholder
        // unless the user has modified the field.
        plugin.$caller.on('blur.editablePlaceholder', selector, function() {
            var $input, placeholder;
            
            $input = $(this);
            placeholder = getEditablePlaceholder($input);

            if ($input.data('editable-placeholder-empty-before-focus')
                || $input.val() === getEditablePlaceholder($input)) {
                
                $input.val('');
                removeEditablePlaceholder($input);
            }
        });

        // If the user moves the mouse off the input field, remove the placeholder
        // unless the user is currently editing the field,
        plugin.$caller.on('mouseleave.editablePlaceholder', selector, function() {
            var $input, placeholder;
            
            $input = $(this);
            placeholder = getEditablePlaceholder($input);
            
            if (!$input.is(':focus')
                && placeholder
                && $input.val() === placeholder) {
                
                $input.val('');
                removeEditablePlaceholder($input);
            }
        });
    }
});

})(jQuery, window);
