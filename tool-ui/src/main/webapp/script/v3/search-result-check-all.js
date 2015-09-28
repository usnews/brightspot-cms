define([ ], function() {
  $(document).on('change', '.searchResult-checkAll', function() {
    var $checkbox = $(this);
    var checked = $checkbox.prop('checked');

    $checkbox.closest('table').find('> tbody :checkbox').each(function() {
      var $c = $(this);

      $c.prop('checked', checked);
      //$c.change();
    });
    var itemIds = $checkbox.closest('table').find('> tbody :checkbox').
        map(function() {
          return 'id=' + $(this).attr('value');
        }).
        get().
        join('&');

    // Fire off an update in bulk
    var $frame = $checkbox.closest('.frame').find('.searchResult-actions');
    var url = $checkbox.prop('checked') ?
        ($checkbox.attr('data-frame-check-base') + '&' + itemIds) :
        ($checkbox.attr('data-frame-uncheck-base') + '&' + itemIds);

    $frame.data('framePlugin').loadPage($frame, $checkbox, 'get', url, null, event);
  });
});
