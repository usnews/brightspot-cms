define([ 'jquery', 'bsp-utils' ], function($, bsp_utils) {
  bsp_utils.onDomInsert(document, '.objectInputs', {
    'insert': function(inputs) {
      var $elements = $(inputs).find('> .inputContainer[data-layout-element]');

      if ($elements.length === 0) {
        return;
      }

      var maxRight = 0;
      var maxBottom = 0;

      $elements.each(function() {
        var dim = $.parseJSON($(this).attr('data-layout-element'));
        var right = dim.left + dim.width;
        var bottom = dim.top + dim.height;

        if (maxRight < right) {
          maxRight = right;
        }

        if (maxBottom < bottom) {
          maxBottom = bottom;
        }
      });

      var $firstElement = $elements.eq(0);

      var $wrapper = $('<div/>', {
        'class': 'inputLayout-wrapper'
      });

      var $constrain = $('<div/>', {
        'class': 'inputLayout-constrain',
        'css': {
          'padding-bottom': (maxBottom / maxRight * 100) + '%'
        }
      })

      var $container = $('<div/>', {
        'class': 'inputLayout-container'
      });

      $elements.each(function() {
        var $element = $(this);
        var dim = $.parseJSON($element.attr('data-layout-element'));

        $container.append($('<div/>', {
          'class': 'inputLayout-element',

          'css': {
            'height': (dim.height / maxBottom * 100) + '%',
            'left': (dim.left / maxRight * 100) + '%',
            'top': (dim.top / maxBottom * 100) + '%',
            'width': (dim.width / maxRight * 100) + '%'
          },

          'html': $('<div/>', {
            'class': 'inputLayout-label',
            'text': $element.find('> .inputLabel > label').text(),
            'click': function() {
              var $label = $(this);

              $label.closest('.inputLayout-container').find('.inputLayout-label').removeClass('inputLayout-label-selected');
              $elements.hide();
              $label.addClass('inputLayout-label-selected');
              $element.show();
            }
          })
        }))
      });

      $wrapper.append($constrain);
      $constrain.append($container);
      $firstElement.before($wrapper);
      $elements.hide();
      $container.find('.inputLayout-element').eq(0).find('.inputLayout-label').addClass('inputLayout-label-selected');
      $firstElement.show();
    }
  });
});
