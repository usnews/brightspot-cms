define(['jquery'], function ($) {

  $(document).on('change', '.searchResultFields input[name="fieldNames"]', function() {
    var $input = $(this);
    var $li = $input.closest('li');
    var hiddenFieldsContainerClass = 'searchResultFields-hide';
    var displayFieldsContainerClass = 'searchResultFields-display';

    // locked fields cannot currently be removed
    //if (!$input.val()) {
    //  return false;
    //}

    if ($li.closest('div').hasClass(hiddenFieldsContainerClass)) {
      $('.' + displayFieldsContainerClass + ' > ul').append($li);
    } else {
      $('.' + hiddenFieldsContainerClass + ' > ul').append($li);
    }
  });
});