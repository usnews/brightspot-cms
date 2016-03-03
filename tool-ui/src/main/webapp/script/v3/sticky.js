define([ 'jquery', 'bsp-utils', 'sticky-kit' ], function($, bsp_utils) {
  bsp_utils.onDomInsert(document, '.withLeftNav > .leftNav, .withLeftNav > .main, .contentForm-main', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: function () {
          return $('.toolHeader').outerHeight(true);
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
          return $('.toolHeader').outerHeight(true) + $element.closest('.contentForm-aside').find('> .widget-publishing').outerHeight(true);
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
          return $('.toolHeader').outerHeight(true);
        }
      });
    }
  });

  bsp_utils.onDomInsert(document, '.rte2-toolbar', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: function () {
          var $h, offset;
          $h = $('.toolHeader');
          // The RTE in full text mode hides the toolheader,
          // so don't use an offest if it is hidden
          offset = $h.is(':visible') ? $h.outerHeight() : 0;
          return offset;
        }
      });
    }
  });

  $(document).on('tabbed-select', function () {
    $(document.body).trigger("sticky_kit:recalc");
  });
});
