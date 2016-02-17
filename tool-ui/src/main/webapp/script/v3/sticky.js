define([ 'jquery', 'bsp-utils', 'sticky-kit' ], function($, bsp_utils) {
  var toolHeaderHeight = $('.toolHeader').outerHeight(true);

  bsp_utils.onDomInsert(document, '.withLeftNav > .leftNav, .withLeftNav > .main, .contentForm-main, .contentForm-aside', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: toolHeaderHeight,
        recalc_every: 100
      });
    }
  });

  bsp_utils.onDomInsert(document, '.widget-publishing', {
    insert: function (element) {
      $(element).stick_in_parent({
        offset_top: toolHeaderHeight,
        parent: '.contentForm',
        recalc_every: 100
      });
    }
  });

  $(document).on('tabbed-select', function () {
    $(document.body).trigger("sticky_kit:recalc");
  });
});