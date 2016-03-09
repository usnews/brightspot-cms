define([ 'jquery', 'bsp-utils', 'sticky-kit' ], function($, bsp_utils) {
  function toolHeaderBottom(includeMargin) {
    var $toolHeader = $('.toolHeader');

    return $toolHeader.is(':visible') ?
        $toolHeader.offset().top - $(window).scrollTop() + $toolHeader.outerHeight(includeMargin) :
        0;
  }

  bsp_utils.onDomInsert(document, '.withLeftNav > .leftNav, .withLeftNav > .main, .contentForm-main', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: function () {
          return toolHeaderBottom(true);
        }
      });
    }
  });

  bsp_utils.onDomInsert(document, '.contentForm-aside > .contentWidgets', {
    insert: function (element) {
      var $element = $(element);

      $element.stick_in_parent({
        parent: '.contentForm',
        offset_top: function () {
          return toolHeaderBottom(true) + $element.closest('.contentForm-aside').find('> .widget-publishing').outerHeight(true);
        },
        offset_change: function (offset) {
          $element.css({
            clip: 'rect(' + (200 - offset) + 'px auto auto auto)'
          });
        }
      });
    }
  });

  bsp_utils.onDomInsert(document, '.widget-publishing', {
    insert: function (element) {
      $(element).stick_in_parent({
        parent: '.contentForm',
        offset_top: function () {
          return toolHeaderBottom(true);
        }
      });
    }
  });

  bsp_utils.onDomInsert(document, '.rte2-toolbar', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: function () {
          return toolHeaderBottom(false);
        }
      });
    }
  });

  bsp_utils.onDomInsert(document, '.rte2-wrapper.rte-fullscreen', {
    insert: function (wrapper) {
      $(wrapper).scroll(bsp_utils.throttle(500, function () {
        $(document.body).trigger("sticky_kit:recalc");
      }));
    }
  });

  $(document).on('tabbed-select', function () {
    $(document.body).trigger("sticky_kit:recalc");
  });
});
