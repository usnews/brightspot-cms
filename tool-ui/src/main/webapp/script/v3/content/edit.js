define([ 'jquery', 'v3/rtc' ], function($, rtc) {

  rtc.receive('com.psddev.cms.tool.page.content.EditFieldUpdateBroadcast', function(data) {
    var userId = data.userId;
    var userName = data.userName;
    var fieldNamesByObjectId = data.fieldNamesByObjectId;

    $('.inputStatus[data-user-id="' + userId + '"]').each(function() {
      var $status = $(this);
      var $container = $status.closest('.inputContainer');

      if (!$container.is('.inputContainer-readOnly')) {
        $container.find(':input').prop('disabled', false);
      }

      $status.remove();
    });

    if (!fieldNamesByObjectId) {
      return;
    }

    $.each(fieldNamesByObjectId, function (objectId, fieldNames) {
      var $inputs = $('.objectInputs[data-id="' + objectId + '"]');

      $.each(fieldNames, function (i, fieldName) {
        var $container = $inputs.find('> .inputContainer[data-field="' + fieldName + '"]');

        if (!$container.is('.inputContainer-readOnly')) {
          $container.find(':input').prop('disabled', true);
        }

        $container.find('> .inputLabel').after($('<div/>', {
          'class': 'inputStatus',
          'data-user-id': userId,
          'text': 'Pending edit from ' + userName
        }));
      });
    });
  });

  $('.contentForm').each(function() {
    var $form = $(this);
    var contentId = $form.attr('data-o-id');

    rtc.restore('com.psddev.cms.tool.page.content.EditFieldUpdateState', {
      contentId: contentId
    });

    function update() {
      var fieldNamesByObjectId = { };

      $form.find('.inputContainer.state-changed, .inputContainer.state-focus').each(function () {
        var $container = $(this);
        var objectId = $container.closest('.objectInputs').attr('data-id');

        (fieldNamesByObjectId[objectId] = fieldNamesByObjectId[objectId] || [ ]).push($container.attr('data-field'));
      });

      if (fieldNamesByObjectId) {
        rtc.execute('com.psddev.cms.tool.page.content.EditFieldUpdateAction', {
          contentId: contentId,
          fieldNamesByObjectId: fieldNamesByObjectId
        });
      }
    }

    var updateTimeout;

    $(document).on('blur focus change', '.contentForm :input', function() {
      if (updateTimeout) {
        clearTimeout(updateTimeout);
      }

      updateTimeout = setTimeout(function() {
        updateTimeout = null;
        update();
      }, 50);
    });

    $form.submit(function() {
      $form.find('.inputContainer').each(function() {
        var $container = $(this);

        if (!$container.is('.inputContainer-readOnly')
            && $container.find('> .inputStatus').length > 0) {

          $container.find(':input').prop('disabled', false);
        }
      })
    });
  });
});
