(function($, window, undefined) {

var $elements = $();

$.plugin2('lazyLoad', {
    '_create': function(element) {
        var $element = $(element);

        if ($element.attr('href')) {
            $elements = $elements.add($element);
        }
    }
});

setInterval(function() {
    $elements.filter(':visible').each(function() {
        var $element = $(this);

        $elements = $elements.not($element);

        $.ajax({
            'cache': false,
            'type': 'get',
            'url': $element.attr('href'),
            'complete': function(response) {
                var $container = $('<div/>');

                $container.append(response.responseText);
                $element.after($container);
                $container.trigger('create');
                $element.remove();
            }
        });
    });
}, 100);

})(jQuery, window);
