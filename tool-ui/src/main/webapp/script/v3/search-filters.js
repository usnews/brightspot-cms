define([ 'jquery' ], function ($) {

  // Disables search filter inputs if missing is checked.
  $(document).on('change', '.searchFilter > :checkbox[name$=".m"]', function () {
    var $checkbox = $(this);

    $checkbox.closest('.searchFilter').toggleClass('searchFilter-missing', $checkbox.prop('checked'));
  });
});