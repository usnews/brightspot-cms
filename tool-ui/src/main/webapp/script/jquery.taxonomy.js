(function($, win, undef) {

$.plugin2('taxonomy', {
    '_create': function(root) {
        var $root = $(root),
                $container,
                processList;

        $container = $('<div/>', {
            'class': 'taxonomyContainer'
        });

        $root.after($container);
        $container.append($root);
                
        processList = function($list) {
            var $items = $list.find('> li'),
                    $subLists = $();

            $items.each(function() {
                var $item = $(this),
                        $subList = $item.find('> ul');

                if ($subList.length > 0) {
                    $subLists = $subLists.add($subList);

                    processList($subList);
                    $subList.before($('<a/>', {
                        'class': 'taxonomyExpand',
                        'text': 'Expand',
                        'click': function() {
                            $items.removeClass('state-selected');
                            $subLists.nextAll('ul').find('.state-selected').removeClass('state-selected');
                            $item.addClass('state-selected');

                            $subLists.nextAll('ul').hide();
                            $subLists.hide();
                            $subList.show();

                            return false;
                        }
                    }));
                }
            });

            $root.after($subLists);
            $subLists.hide();
        };

        processList($root);
    }
});

})(jQuery, window);
