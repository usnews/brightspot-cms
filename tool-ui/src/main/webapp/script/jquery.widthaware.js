(function($, window, undefined) {

var $window = $(window),
        $elements = $(),
        raf = window.requestAnimationFrame;

$.plugin2('widthAware', {
    '_create': function(element) {
        var $element = $(element),
                widths = $element.attr('data-widths');

        if (widths) {
            $elements = $elements.add($element);
        }
    }
});

$window.bind('create resize', $.throttle(100, function() {
    $elements.filter('[data-widths]:visible').each(function() {
        var $element = $(this),
                elementWidth = $element.width(),
                classNames = { 'add': '', 'remove': '' };

        $.each($.parseJSON($element.attr('data-widths')), function(className, rule) {
            $.each(rule, function(operator, width) {
                switch (operator) {
                    case 'eq' :
                    case '=' :
                    case '==' :
                        classNames[elementWidth === width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    case 'ge' :
                    case '>=' :
                        classNames[elementWidth >= width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    case 'gt' :
                    case '>' :
                        classNames[elementWidth > width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    case 'le' :
                    case '<=' :
                        classNames[elementWidth <= width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    case 'lt' :
                    case '<' :
                        classNames[elementWidth < width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    case 'ne' :
                    case '!=' :
                        classNames[elementWidth !== width ? 'add' : 'remove'] += ' ' + className;
                        break;

                    default :
                        break;
                }
            });
        });
        
        raf(function() {
            $element.removeClass(classNames['remove']);
            $element.addClass(classNames['add']);
        });
    });
}));

})(jQuery, window);
