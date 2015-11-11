define([
    'jquery',
    'bsp-utils',
    'bsp-uploader' ],
function ($, bsp_utils, uploader) {

  function before() {
    var plugin = this;
    var $input = plugin.el;
    var files = plugin.files;

    var $fileSelector = $input.closest('.fileSelector');
    var $inputWrapper = $input.closest('.inputSmall');

    $.each(files, function(i, file) {
      $inputWrapper.append(_createProgressHtml());
      var $uploadPreview = $inputWrapper.find('.upload-preview').eq(i);

      if (file.type.match('image.*') && !($input.attr('multiple'))) {
        _displayImgPreview($uploadPreview.find('img').first(), file);
      } else {
        _displayDefaultPreview($uploadPreview);
        $uploadPreview.addClass('loading');
      }
    });

    var $select = $fileSelector.find('select').first();

    if ($select.find('option[value="keep"]').size() < 1) {
      $select.prepend($('<option/>', {
        'data-hide': '.fileSelectorItem',
        'data-show': '.fileSelectorExisting',
        'value': 'keep',
        'text': 'Keep Existing'
      }));
    }

    $select.val('keep');
  }

  function progress(event) {
    var plugin = this;
    var $input = plugin.el;
    var $inputWrapper = $input.closest('.inputSmall');

    $inputWrapper.find('[data-progress]').attr('data-progress', Math.round(event.loaded * 100 / event.total));
  }

  function success(json) {
    var plugin = this;
    var $input = plugin.el;
    var $inputWrapper = $input.closest('.inputSmall');
    var $uploadPreview = $inputWrapper.find('.upload-preview');
    var inputName = $input.attr('data-input-name');
    var jsonParamName = inputName + '.file.json';
    //var localImg = $uploadPreview.find('img');
    //var localSrc = localImg.first().attr('src');

    var params = {};
    params['inputName'] = inputName;
    params['fieldName'] = $inputWrapper.parent().attr('data-field-name');
    params['typeId'] = $input.data('type-id');
    params[jsonParamName] = json;
    params[inputName + '.action'] = 'keep';

    $uploadPreview.removeClass('loading');

    $.ajax({
      url: window.CONTEXT_PATH + 'storageItemField',
      dataType: 'html',
      data: params
    }).done(function (response) {
      $uploadPreview.detach();
      var $response = $(response);

      $inputWrapper.replaceWith($response);
      $response.find('.fileSelectorItem:not(.fileSelectorExisting)').hide();

      //// prevents image pop-in
      //var img = $inputWrapper.find('.imageEditor-image').find('img').first();
      //img.attr('style', 'max-width: ' + width + 'px;');
      //var remoteSrc = img.attr('src');
      //img.attr('src', localSrc);
      //$.ajax({
      //  url: remoteSrc
      //}).done(function (html) {
      //  $inputWrapper.find('img').attr('src', remoteSrc);
      //});
    });
  }

  function error() {
    // TODO: error handling
  }

  // TODO: move this to bsp-uploader
  function _displayImgPreview(el, file) {

    if (!(window.File && window.FileReader && window.FileList)) {
      return;
    }

    var reader = new FileReader();
    reader.onload = (function (readFile) {
      return function (event) {
        el.attr('src', event.target.result);
        el.closest('.upload-preview').addClass('loading');
      };
    })(file);

    reader.readAsDataURL(file);
  }

  function _displayDefaultPreview(uploadPreview) {
    var $uploadPreview = $(uploadPreview);
    var $uploadPreviewWrapper = $uploadPreview.find('.upload-progress').first();

    $uploadPreview.width(150).height(150);
    $uploadPreviewWrapper.width(150).height(150);

  }

  function _createProgressHtml() {

    return $('<div/>', {'class': 'upload-preview'}).append(
      $('<div/>', {'class': 'upload-progress'}).append(
        $('<img/>')
      ).append(
        $('<div/>', {'class': 'radial-progress', 'data-progress': '0'}).append(
          $('<div/>', {'class': 'circle'}).append(
            $('<div/>', {'class': 'mask full'}).append(
              $('<div/>', {'class': 'fill'})
            )
          ).append(
            $('<div/>', {'class': 'mask half'}).append(
              $('<div/>', {'class': 'fill'})
            ).append(
              $('<div/>', {'class': 'fill fix'})
            )
          )
        ).append(
          $('<div/>', {'class': 'inset'}).append(
            $('<div/>', {'class': 'percentage'}).append(
              $('<span/>')
            )
          )
        )
      )
    );
  }

  return bsp_utils.plugin(false, 'bsp', 'uploader', {
    '_each': function (input) {
      var plugin = Object.create(uploader);
      plugin.init($(input), {
        path : window.UPLOAD_PATH
      });

      plugin.progress = progress;
      plugin.before = before;
      plugin.success = success;
      plugin.errror = error;
    }
  });

});

//# sourceURL=file.js