if (typeof jQuery !== 'undefined') (function($) {

$.plugin('tree', {

'init': function() {

    this.liveInit(function() {
        $(this).find(':checkbox').each(function() {
            var $checkbox = $(this);
            var $container = $checkbox.parent();
            var $children = $container.find(':checkbox').not($checkbox);
            if ($children.length > 0) {
                $container.addClass($children.is(':checked') ? 'open' : 'closed');
            }
        });
    });

    this.find(':checkbox').live('change', function() {

        var $checkbox = $(this);
        var $container = $checkbox.parent();
        var $children = $container.find(':checkbox').not($checkbox);

        if ($children.length > 0) {
            if ($checkbox.is(':checked')) {
                if (!$children.is(':checked')) {
                    $container.removeClass('closed');
                    $container.addClass('open');
                    $children.attr('checked', 'checked');
                    $children.change();
                }

            } else {
                if (!$children.is(':not(:checked)')) {
                    $container.removeClass('open');
                    $container.addClass('closed');
                    $children.removeAttr('checked');
                    $children.change();
                }
            }
        }
    });

    return this;
}

});

})(jQuery);
