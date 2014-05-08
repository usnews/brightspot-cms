define([
    'jquery',
    'bsp-utils' ],

function($, bsp_utils) {
    bsp_utils.onDomInsert(document, '.inputContainer-listLayoutItemContainer-embedded', {
        'insert': function(item) {
            var $item = $(item);
            var expanded;

            $item.append($('<span/>', {
                'class': 'inputContainer-listLayoutItemContainer_expand',
                'click': function() {
                    var $container = $item.offsetParent();

                    expanded = !expanded;

                    if (expanded) {
                        $item.css({
                            'left': 10,
                            'position': 'absolute',
                            'top': 10,
                            'z-index': 1
                        });

                        $item.outerWidth($container.outerWidth() - 20);
                        $item.outerHeight($container.outerHeight() - 20);
                        $item.addClass('inputContainer-listLayoutItemContainer-embedded-expanded');

                    } else {
                        $item.attr('style', '');
                        $item.removeClass('inputContainer-listLayoutItemContainer-embedded-expanded');
                    }

                    $item.trigger('resize');
                }
            }));
        }
    });
});
