define([ 'jquery', 'bsp-utils', 'v3/input/carousel' ], function($, bsp_utils, carouselUtility) {

  var settings  = {
    containerSelector         : '.searchResult-carousel',
    nextAttr                  : 'next-page',
    prevAttr                  : 'prev-page',
    itemsSelector             : 'a'
  };

  bsp_utils.onDomInsert(document, settings.containerSelector, {

    insert : function (container) {

      var carousel;

      var $container = $(container);
      carousel = Object.create(carouselUtility);
      carousel.init($container, {numbered:false});

      addTiles($container.children(settings.itemsSelector), false);

      // Add more tiles via ajax when carousel ends
      $container.on('carousel.end carousel.begin', function(e, data) {

        var isEndEvent = e.namespace === 'end';
        var dataAttr = isEndEvent ? settings.nextAttr : settings.prevAttr;

        $.ajax({
          'method' : 'get',
          'url'    : $container.data(dataAttr),
          'cache'  : false
        }).done(function(html) {
          var $searchCarousel = $(html).find(settings.containerSelector).first();
          $container.data(dataAttr, $searchCarousel.data(dataAttr));
          addTiles($searchCarousel.children(settings.itemsSelector), !isEndEvent);
          data.carousel.update();
        });

      });

      /**
       * Appends or prepends tiles to carousel
       *
       * @param {Array|jQueryObject} tiles
       * jQuery object array
       *
       * @param {Boolean} [isPrepend]
       * Optionally specifies to prepend
       */
      function addTiles(tiles, isPrepend) {

        var $tiles = $(tiles);

        if (isPrepend) {
          $tiles = $tiles.get().reverse();
        }

        // Adds tiles to carousel
        $.each($tiles, function(i, elem) {
          var $elem = $(elem);

          if (isPrepend) {
            carousel.prependTile($elem);
          } else {
            carousel.addTile($elem);
          }

          // Sets active carousel item
          if ($elem.find('.carousel-tile-content-active').size() > 0) {
            $elem.closest('.carousel-tile').addClass('carousel-active');
          }
        });
      }
    }
  });

});