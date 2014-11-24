define([ 'jquery', 'bsp-utils' ], function($, bsp_utils) {
  var ORIGINAL_OFFSET_DATA = 'originalData';
  var CURRENT_CSS_DATA = 'currentCss';
  var FIXED_DATA = 'fixed';
  var FIXED_BOTTOM = 'bottom';
  var FIXED_TOP = 'top';
  var BOTTOM_OFFSET = 60;

  var $window = $(window);
  var oldScrollTop = 0;
  var windowHeight;
  var $elements = $();

  function resetCss() {
    $(this).css({
      'bottom': '',
      'left': '',
      'position': '',
      'top': ''
    });
  }

  function updateOriginalOffset() {
    var $element = $(this);
    var offset = $element.offset();
    offset.marginLeft = parseInt($element.css('margin-left'), 10);

    $.data(this, ORIGINAL_OFFSET_DATA, offset);
    return offset;
  }

  function restoreCurrentCss() {
    var currentCss = $.data(this, CURRENT_CSS_DATA);

    if (currentCss) {
      $(this).css(currentCss);
    }
  }

  function getOriginalOffset($element) {
    var element = $element[0];
    var offset = $.data(element, ORIGINAL_OFFSET_DATA);

    if (!offset) {
      resetCss.call(element);
      offset = updateOriginalOffset.call(element);
      restoreCurrentCss.call(element);
    }

    return offset;
  }

  function updateElements() {
    windowHeight = $window.height() - BOTTOM_OFFSET;

    $elements.each(resetCss);
    $elements.each(updateOriginalOffset);

    $.each([
      [ '.withLeftNav', '> .leftNav', '> .main' ],
      [ '.contentForm', '> .contentForm-main', '> .contentForm-aside' ]

    ], function(i, selectors) {
      $(selectors[0]).each(function() {
        var $container = $(this);

        if ($container.closest('.popup').length === 0) {
          var $left = $container.find(selectors[1]);
          var $right = $container.find(selectors[2]);
          var leftBottom = getOriginalOffset($left).top + $left.outerHeight(true);
          var rightBottom = getOriginalOffset($right).top + $right.outerHeight(true);
          $elements = $elements[leftBottom > windowHeight && leftBottom < rightBottom ? 'add' : 'not']($left);
          $elements = $elements[rightBottom > windowHeight && rightBottom < leftBottom ? 'add' : 'not']($right);
        }
      });
    });

    $elements.each(restoreCurrentCss);
  }

  updateElements();
  setTimeout(updateElements, 1000);
  $window.resize(bsp_utils.throttle(100, updateElements));

  function updateCss($element, css) {
    $.data($element[0], CURRENT_CSS_DATA, css);
    $element.css(css);
  }

  function positionElements() {
    var newScrollTop = $window.scrollTop();

    $elements.each(function() {
      var $element = $(this);
      var fixed = $.data($element[0], FIXED_DATA);

      if (!fixed) {
        var originalOffset = getOriginalOffset($element);
        var elementOffset = $element.offset();

        if (newScrollTop > oldScrollTop) {
          var windowBottom = newScrollTop + windowHeight;
          var elementBottom = elementOffset.top + $element.outerHeight(true);

          if (windowBottom > elementBottom) {
            $.data($element[0], FIXED_DATA, FIXED_BOTTOM);
            updateCss($element, {
              'bottom': BOTTOM_OFFSET,
              'left': elementOffset.left - originalOffset.marginLeft,
              'position': 'fixed',
              'top': ''
            });
          }

        } else {
          var originalTop = originalOffset.top;
          var elementTop = elementOffset.top - originalTop;

          if (newScrollTop < elementTop) {
            $.data($element[0], FIXED_DATA, FIXED_TOP);
            updateCss($element, {
              'bottom': '',
              'left': elementOffset.left - originalOffset.marginLeft,
              'position': 'fixed',
              'top': originalTop
            });
          }
        }

      } else if ((newScrollTop > oldScrollTop && fixed === FIXED_TOP) ||
          (newScrollTop <= oldScrollTop && fixed === FIXED_BOTTOM)) {
        $.data($element[0], FIXED_DATA, null);
        updateCss($element, {
          'bottom': '',
          'left': '',
          'position': 'relative',
          'top': $element.offset().top - getOriginalOffset($element).top
        });
      }
    });

    oldScrollTop = newScrollTop;
  }

  positionElements();
  $window.scroll(bsp_utils.throttle(50, positionElements));
});
