define('jquery', [ ], function() { return $; });
define('jquery.extra', [ ], function() { });
define('jquery.handsontable.full', [ ], function() { });
define('d3', [ ], function() { return d3; });

requirejs.config({
  shim: {
    'leaflet.common': [ 'leaflet' ],
    'leaflet.draw': [ 'leaflet' ],
    'l.control.geosearch': [ 'leaflet' ],
    'l.geosearch.provider.openstreetmap': [ 'l.control.geosearch' ],
    'nv.d3': [ 'd3' ],
    'pixastic/actions/blurfast': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/brightness': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/crop': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/desaturate': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/fliph': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/flipv': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/invert': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/rotate': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/sepia': [ 'pixastic/pixastic.core' ],
    'pixastic/actions/sharpen': [ 'pixastic/pixastic.core' ]
  }
});

require([
  'jquery',
  'jquery.extra',

  'bsp-autoexpand',
  'bsp-autosubmit',
  'bsp-utils',
  'jquery.mousewheel',

  'input',

  'jquery.calendar',
  'jquery.dropdown',
  'jquery.editableplaceholder',
  'jquery.popup',
  'jquery.fixedscrollable',
  'jquery.frame',
  'jquery.lazyload',
  'jquery.pagelayout',
  'jquery.pagethumbnails',
  'jquery.repeatable',
  'jquery.sortable',
  'jquery.tabbed',
  'jquery.toggleable',
  'jquery.widthaware',
  'nv.d3',

  'dashboard',
  'content',
  'v3/constrainedscroll' ],

function() {
  var $ = arguments[0];
  var bsp_autoExpand = arguments[2];
  var bsp_autoSubmit = arguments[3];
  var bsp_utils = arguments[4];
  var win = window;
  var undef;
  var $win = $(win),
      doc = win.document,
      $doc = $(doc),
      toolChecks = [ ],
      toolCheckActionCallbacks = [ ];

  $.addToolCheck = function(check) {
    toolCheckActionCallbacks.push(check.actions);
    delete check.actions;
    toolChecks.push(check);
  };

  $.addToolCheck({
    'check': 'kick',
    'actions': {
      'kickIn': function(parameters) {
        win.location = win.location.protocol + '//' + win.location.host + parameters.returnPath;
      },

      'kickOut': function() {
        win.location = CONTEXT_PATH + '/logIn.jsp?forced=true&returnPath=' + encodeURIComponent(win.location.pathname + win.location.search);
      }
    }
  });

  // Standard behaviors.
  $doc.repeatable('live', '.repeatableForm, .repeatableInputs, .repeatableLayout, .repeatableObjectId');
  $doc.repeatable('live', '.repeatableText', {
    'addButtonText': '',
    'removeButtonText': '',
    'restoreButtonText': ''
  });

  bsp_autoExpand.live(document, ':text.expandable, textarea');
  bsp_autoSubmit.live(document, '.autoSubmit');

  $doc.calendar('live', ':text.date');
  $doc.dropDown('live', 'select[multiple], select[data-searchable="true"]');
  $doc.editablePlaceholder('live', ':input[data-editable-placeholder]');
  $doc.fixedScrollable('live', '.fixedScrollable, .searchResult > .searchResultList, .popup[name="miscSearch"] .searchFiltersRest');

  $doc.frame({
    'frameClassName': 'frame',
    'loadingClassName': 'loading',
    'loadedClassName': 'loaded'
  });

  bsp_utils.onDomInsert(document, '[data-bsp-autosubmit], .autoSubmit', {
    'insert': function(item) {
      var $form = $(item).closest('form');
      var $targetFrame = $('.frame[name=' + $form.attr('target') + ']:not(.loading):not(.loaded)');

      if ($targetFrame.length > 0) {
        $form.submit();
      }
    }
  });

  $doc.lazyLoad('live', '.lazyLoad');
  $doc.locationMap('live', '.locationMap');
  $doc.objectId('live', ':input.objectId');
  $doc.pageLayout('live', '.pageLayout');
  $doc.pageThumbnails('live', '.pageThumbnails');
  $doc.regionMap('live', '.regionMap');
  $doc.rte('live', '.richtext');
  $doc.tabbed('live', '.tabbed, .objectInputs');
  $doc.toggleable('live', '.toggleable');
  $doc.widthAware('live', '[data-widths]');

  // Remove placeholder text over search input when there's text.
  $doc.onCreate('.searchInput', function() {
    var $container = $(this),
        $label = $container.find('> label'),
        $input = $container.find('> :text');

    $input.bind('input', $.run(function() {
      $label.toggle(!$input.val());
    }));
  });

  // Hide non-essential items in the permissions input.
  $doc.onCreate('.inputContainer .permissions select', function() {
    var $select = $(this);

    $select.bind('change', $.run(function() {
      $select.parent().find('> h2, > ul').toggle($select.find(':selected').val() === 'some');
    }));
  });

  $doc.onCreate('.searchSuggestionsForm', function() {
    var $suggestionsForm = $(this),
        $source = $suggestionsForm.popup('source'),
        $contentForm = $source.closest('.contentForm'),
        search;

    if ($contentForm.length === 0) {
      return;
    }

    search = win.location.search;
    search += search.indexOf('?') > -1 ? '&' : '?';
    search += 'id=' + $contentForm.attr('data-object-id');

    $.ajax({
      'data': $contentForm.serialize(),
      'type': 'post',
      'url': CONTEXT_PATH + '/content/state.jsp' + search,
      'complete': function(request) {
        if ($suggestionsForm.closest('body').length === 0) {
          return;
        }

        $suggestionsForm.append($('<input/>', {
          'type': 'hidden',
          'name': 'object',
          'value': request.responseText
        }));

        $suggestionsForm.append($('<input/>', {
          'type': 'hidden',
          'name': 'field',
          'value': $source.closest('.inputContainer').attr('data-field')
        }));

        $suggestionsForm.submit();
      }
    });
  });

  $doc.on('click', '.taxonomyExpand', function() {
    var $this = $(this);
    var selectedClass = 'state-selected';
    $this.closest('ul').find('.' + selectedClass).removeClass(selectedClass);
    $this.closest('li').addClass(selectedClass);
  });

  $doc.onCreate('.searchAdvancedResult', function() {
    var $result = $(this),
        checked;

    $result.find('thead tr:first th:first').append($('<div/>', {
      'html': $('<span/>', {
        'class': 'icon icon-check icon-only'
      }),

      'css': {
        'cursor': 'pointer',
        'text-align': 'center',
        'user-select': 'none',
        'vertical-align': 'middle'
      },

      'click': function() {
        var $div = $(this);

        checked = !checked;

        $div.closest('table').find(':checkbox').prop('checked', checked);
        $div.find('.icon').removeClass('icon-check icon-check-empty').addClass(checked ? 'icon-check-empty' : 'icon-check');
        return false;
      }
    }));

    $result.on('change', ':checkbox', function() {
      $result.find('.actions .action').each(function() {
        var $action= $(this),
            text = $action.text();

        if ($result.find(':checkbox:checked').length > 0) {
          $action.text(text.replace('All', 'Selected'));
        } else {
          $action.text(text.replace('Selected', 'All'));
        }
      });
    });
  });

  // Show stack trace when clicking on the exception message.
  $doc.delegate('.exception > *', 'click', function() {
    $(this).find('> .stackTrace').toggle();
  });

  // Soft validation based on suggested sizes.
  (function() {
    var TRIM_RE = /^\s+|\s+$/g,
        WHITESPACE_RE = /\s+/;

    function updateWordCount($container, $input, value) {
      var minimum = +$input.attr('data-suggested-minimum'),
          maximum = +$input.attr('data-suggested-maximum'),
          cc,
          wc;

      value = (value || '').replace(TRIM_RE, '');
      cc = value.length;
      wc = value ? value.split(WHITESPACE_RE).length : 0;

      $container.attr('data-wc-message',
          cc < minimum ? 'Too Short' :
          cc > maximum ? 'Too Long' :
          wc + 'w ' + cc + 'c');
    }

    $doc.delegate(
        '.inputSmall-text :text, .inputSmall-text textarea:not(.richtext)',
        'change.wordCount focus.wordCount input.wordCount',
        $.throttle(100, function() {

      var $input = $(this);

      updateWordCount(
          $input.closest('.inputContainer'),
          $input,
          $input.val());
    }));

    $doc.onCreate('.wysihtml5-sandbox', function() {
      var iframe = this,
          $iframe = $(iframe),
          $container = $iframe.closest('.rte-container'),
          $textarea = $container.find('textarea.richtext'),
          $toolbar = $container.find('.rte-toolbar');

      $(iframe.contentDocument).on('input', $.throttle(100, function() {
        if ($textarea.length > 0) {
          var $bodyClone = $(iframe.contentDocument.body).clone();

          $bodyClone.find('del, .rte').remove();
          updateWordCount(
              $toolbar,
              $textarea,
              $bodyClone.text());
        }
      }));
    });
  })();

  // Handle file uploads from drag-and-drop.
  (function() {
    var docEntered;

    // Show all drop zones when the user initiates drag-and-drop.
    $doc.bind('dragenter', function() {
      var $body,
          $cover;

      if (docEntered) {
        return;
      }

      docEntered = true;
      $body = $(doc.body);

      // Cover is required to detect mouse leaving the window.
      $cover = $('<div/>', {
        'class': 'uploadableCover',
        'css': {
          'left': 0,
          'height': '100%',
          'position': 'fixed',
          'top': 0,
          'width': '100%',
          'z-index': 1999999
        }
      });

      $cover.bind('dragenter dragover', function(event) {
        event.stopPropagation();
        event.preventDefault();
        return false;
      });

      $cover.bind('dragleave', function() {
        docEntered = false;
        $cover.remove();
        $('.uploadableDrop').remove();
        $('.uploadableFile').remove();
      });

      $cover.bind('drop', function(event) {
        event.preventDefault();
        $cover.trigger('dragleave');
        return false;
      });

      $body.append($cover);

      // Valid file drop zones.
      $('.inputContainer .action-upload, .uploadable .uploadableLink').each(function() {
        var $upload = $(this),
            $container = $upload.closest('.inputContainer, .uploadable'),
            overlayCss,
            $dropZone,
            $dropLink,
            $fileInputContainer,
            $fileInput;

        overlayCss = $.extend($container.offset(), {
          'height': $container.outerHeight(),
          'position': 'absolute',
          'width': $container.outerWidth()
        });

        $dropZone = $('<div/>', {
          'class': 'uploadableDrop',
          'css': overlayCss
        });

        $dropLink = $upload.clone();
        $dropLink.text("Drop Files Here");

        $fileInputContainer = $('<div/>', {
          'class': 'uploadableFile',
          'css': $.extend(overlayCss, {
            'z-index': 2000000
          })
        });

        $fileInput = $('<input/>', {
          'type': 'file',
          'multiple': 'multiple'
        });

        // On file drop, replace the appropriate input.
        $fileInput.one('change', function() {
          var dropLinkOffset = $dropLink.offset(),
              $frame,
              replaceFileInput;

          $cover.hide();
          $dropLink.click();
          $fileInputContainer.hide();

          $frame = $('.frame[name="' + $dropLink.attr('target') + '"]');

          // Position the popup over the drop link.
          $frame.popup('source', $upload, {
            'pageX': dropLinkOffset.left + $dropLink.outerWidth() / 2,
            'pageY': dropLinkOffset.top + $dropLink.outerHeight()
          });

          // Closing the popup resets the drag-and-drop.
          $frame.popup('container').bind('close', function() {
            $cover.trigger('dragleave');
          });

          replaceFileInput = function() {
            var $frameFileInput = $frame.find(':file').eq(0);

            if ($frameFileInput.length !== 1) {
              setTimeout(replaceFileInput, 20);

            } else {
              $.each([ 'class', 'id', 'name', 'style' ], function(index, name) {
                $fileInput.attr(name, $frameFileInput.attr(name) || '');
              });

              $frameFileInput.after($fileInput);
              $frameFileInput.remove();
              $frameFileInput = $fileInput;
              $frameFileInput.change();
            }
          };

          replaceFileInput();
        });

        $dropZone.append($dropLink);
        $body.append($dropZone);
        $fileInputContainer.append($fileInput);
        $body.append($fileInputContainer);
      });
    });

    $doc.bind('dragend', function(event) {
      if (docEntered) {
        docEntered = false;
        $('.uploadableCover').remove();
        $('.uploadableDrop').remove();
        $('.uploadableFile').remove();
      }
    });
  })();

  $doc.on('click', 'button[name="action-delete"], :submit[name="action-delete"]', function() {
    return confirm('Are you sure you want to permanently delete this item?');
  });

  $doc.on('click', 'button[name="action-trash"], :submit[name="action-trash"]', function() {
    return confirm('Are you sure you want to archive this item?');
  });

  $doc.on('input-disable', ':input', function(event, disable) {
    $(this).prop('disabled', disable);
  });

  $doc.onCreate('.inputContainer-readOnly', function() {
    $(this).find(":input, div").trigger('input-disable', [ true ]);
  });

  (function() {
    function sync() {
      var $input = $(this),
          $output = $('output[for="' + $input.attr('id') + '"]');

      $output.prop('value', $input.prop('value'));
    }

    $doc.onCreate('input[type="range"]', sync);
    $doc.on('change input', 'input[type="range"]', sync);

    function fix() {
      var $container = $(this).closest('.inputContainer'),
          $inputs = $container.find('.inputVariation input[type="range"]'),
          total,
          max;

      if ($inputs.length === 0) {
        return;
      }

      total = 0.0;
      max = 0.0;

      $inputs.each(function() {
        var $input = $(this),
            inputMax = parseFloat($input.prop('max'));

        total += parseFloat($input.prop('value'));

        if (max < inputMax) {
          max = inputMax;
        }
      });

      $inputs.each(function() {
        var $input = $(this);

        $input.prop('value', parseFloat($input.prop('value')) / total * max);
        sync.call(this);
      });
    }

    $doc.onCreate('.inputContainer', fix);
    $doc.on('change input', '.inputVariation input[type="range"]', fix);
  })();

  // Key bindings.
  $doc.on('keydown', ':input', function(event) {
    if (event.which === 27) {
      $(this).blur();
    }
  });

  $doc.on('keypress', function(event) {
    var $searchInput;

    if (event.which === 47 && $(event.target).closest(':input').length === 0) {
      $searchInput = $('.toolSearch .searchInput :text');

      $searchInput.val('');
      $searchInput.focus();
      return false;
    }
  });

  // Synchronizes main search input with the hidden one in the type select form.
  $doc.on('input', '.searchFiltersRest > .searchInput > :text', function() {
    var $input = $(this),
        $otherInput = $input.closest('.searchFilters').find('.searchFiltersType > input[name="' + $input.attr('name') + '"]');

    if ($otherInput.length > 0) {
      $otherInput.val($input.val());
    }
  });

  $doc.on('open', '.popup[name="miscSearch"]', function() {
    $(document.body).addClass('toolSearchOpen');
  });

  $doc.on('close', '.popup[name="miscSearch"]', function() {
    $(document.body).removeClass('toolSearchOpen');
  });

  $doc.ready(function() {
    $(this).trigger('create');

    // Add the name of the sub-selected item on the main nav.
    $('.toolNav .selected').each(function() {
      var $selected = $(this),
          $subList = $selected.find('> ul'),
          $subSelected = $subList.find('> .selected > a'),
          $selectedLink;

      if ($subSelected.length > 0) {
        $selectedLink = $selected.find('> a');
        $selectedLink.text($selectedLink.text() + ' \u2192 ' + $subSelected.text());
      }

      $subList.css('min-width', $selected.outerWidth());
    });

    // Don't allow main nav links to be clickable if they have any children.
    $('.toolNav li.isNested > a').click(function(event) {
      event.preventDefault();
    });

    // Sync the search input in the tool header with the one in the popup.
    (function() {
      var previousValue;

      $('.toolSearch :text').bind('focus input', $.throttle(500, function(event) {
        var $headerInput = $(this),
            $headerForm = $headerInput.closest('form'),
            $searchFrame,
            $searchInput,
            headerInputValue = $headerInput.val();

        $headerInput.attr('autocomplete', 'off');
        $searchFrame = $('.frame[name="' + $headerForm.attr('target') + '"]');

        if ($searchFrame.length === 0 ||
            (event.type === 'focus' &&
            headerInputValue &&
            $searchFrame.find('.searchResultList .message-warning').length > 0)) {
          $headerForm.submit();

        } else {
          $searchFrame.popup('open');
          $searchInput = $searchFrame.find('.searchFilters :input[name="q"]');

          if (headerInputValue !== $searchInput.val()) {
            $searchInput.val(headerInputValue).trigger('input');
          }
        }
      }));

      $('.toolSearch button').bind('click', function() {
        $('.toolSearch :text').focus();
        return false;
      });
    }());

    // Starts all server-side tool checks.
    if (!DISABLE_TOOL_CHECKS) {
      (function() {
        var toolCheckPoll = function() {
          $.ajax({
            'method': 'post',
            'url': CONTEXT_PATH + '/toolCheckStream',
            'cache': false,
            'dataType': 'json',
            'data': {
              'url': win.location.href,
              'r': JSON.stringify(toolChecks)
            }

          }).done(function(responses) {
            if (!responses) {
              return;
            }

            $.each(responses, function(i, response) {
              if (response) {
                toolCheckActionCallbacks[i][response.action].call(toolChecks[i], response);
              }
            });

          }).done(function() {
            setTimeout(toolCheckPoll, 100);

          }).fail(function() {
            setTimeout(toolCheckPoll, 10000);
          });
        };

        toolCheckPoll();
      })();
    }
  });
});
