define([ 'jquery', 'bsp-utils' ], function($, bsp_utils) {

    bsp_utils.onDomInsert(document, '.infiniteScroll', {

      insert : function (scrollable) {

        var defaults  = {
          navSelector              : '.searchResult-pagination',
          nextSelector             : 'li.next a',
          previousSelector         : 'li.prev a',
          itemsContainerSelector   : '.searchResult-images',
          itemSelector             : 'a figure',
          scrollBuffer             : 100,
          prefill                  : true
        };

        var settings = $.extend({}, defaults, {state : {}});

        var $scrollable = $(scrollable);
        var $itemContainer = $scrollable.find(settings.itemsContainerSelector);
        var $nav = $scrollable.find(settings.navSelector);

        if (settings.prefill) {
          var $items = $itemContainer.find(settings.itemSelector);
          var containerArea = $itemContainer.outerWidth() * $itemContainer.outerHeight();
          var itemArea = $items.first().outerWidth() * $items.first().outerHeight();
          if (containerArea > itemArea * $items.size()) {
            doFetchAndAppend();
          }
        }

        $scrollable.scroll(bsp_utils.throttle(100, onScroll));

        function onScroll() {
          if (shouldFetch()) {
            doFetchAndAppend();
          }
        }

        function shouldFetch() {

          if (settings.state.isAwaitingResponse || settings.state.hasLoadedAllItems) {
            return false;
          }

          return $itemContainer.outerHeight(true) - $scrollable.height() - $scrollable.scrollTop() <= settings.scrollBuffer;
        }

        function doFetchAndAppend() {

          var $next = $nav.find(settings.nextSelector);

          if ($next) {
            settings.state.isAwaitingResponse = true;
            $.ajax({
              'cache': false,
              'type': 'get',
              'url' : $next.attr('href')
            }).done(function(html) {
              settings.state.isAwaitingResponse = false;

              var $html = $(html);
              var $items = $html.find(settings.itemsContainerSelector).children();
              var nextLink = $html.find(settings.navSelector).find(settings.nextSelector);

              if ($items.size() == 0 || nextLink.size() == 0) {
                settings.state.hasLoadedAllItems = true;
                return;
              }

              $itemContainer.append($items);
              $next.attr('href', nextLink.attr('href'));
            });
          }
        }
      }
    });

});