define([
  'jquery',
  'bsp-utils' ],

function($, bsp_utils) {
  var $win = $(window);

  // Publishing widget behaviors.
  bsp_utils.onDomInsert(document, '.widget-publishing', {
    'insert': function(widget) {
      var $widget = $(widget);
      var $dateInput = $widget.find('.dateInput');
      var $newSchedule = $widget.find('select[name="newSchedule"]');
      var $publishButton = $widget.find('[name="action-publish"]');
      var oldPublishText = $publishButton.text();
      var oldDate = $dateInput.val();
      var onChange;

      // Change the publish button label if scheduling.
      if ($dateInput.length === 0) {
        $publishButton.addClass('schedule');
        $publishButton.text('Schedule');

      } else {
        onChange = function() {
          if ($dateInput.val()) {
            $publishButton.addClass('schedule');
            $publishButton.text(oldDate && !$newSchedule.val() ? 'Reschedule' : 'Schedule');

          } else {
            $publishButton.removeClass('schedule');
            $publishButton.text(oldPublishText);
          }
        };

        onChange();

        $dateInput.change(onChange);
        $newSchedule.change(onChange);
      }

      $widget.closest('.contentForm-aside').each(function() {
        var $aside = $(this);

        function onResize() {
          var asideOffset = $aside.offset();
          var toolHeaderHeight = $('.toolHeader').outerHeight(true)

          if (asideOffset.top - $win.scrollTop() <= toolHeaderHeight) {
            var asideWidth = $aside.outerWidth();

            $widget.css({
              'position': 'fixed',
              'right': $win.width() - asideOffset.left - asideWidth,
              'top': toolHeaderHeight,
              'z-index': 1
            });

            $widget.outerWidth(asideWidth);

            // Push other areas down.
            $aside.css('padding-top', $widget.outerHeight(true));

          } else {
            $widget.css({
              'position': '',
              'right': '',
              'top': '',
              'width': '',
              'z-index': ''
            });

            $aside.css({
              'padding-top': '',
            });
          }
        }

        onResize();
        $win.resize(bsp_utils.throttle(100, onResize));
        $win.scroll(bsp_utils.throttle(100, onResize));
      });
    }
  });

  // Create tabs if the publishing widget contains both the workflow
  // and the publish areas.
  bsp_utils.onDomInsert(document, '.widget-publishing', {
    'insert': function(widget) {
      var $widget = $(widget);
      var $workflow = $widget.find('.widget-publishingWorkflow');
      var $publish = $widget.find('.widget-publishingPublish');
      var $tabs;
      var $tabWorkflow;
      var $tabPublish;

      if ($workflow.length === 0 || $publish.length === 0) {
        return;
      }

      $tabs = $('<ul/>', {
        'class': 'tabs'
      });

      $tabWorkflow = $('<li/>', {
        'html': $('<a/>', {
          'text': 'Workflow',
          'click': function() {
            $workflow.show();
            $tabWorkflow.addClass('state-selected');
            $publish.hide();
            $tabPublish.removeClass('state-selected');
            $win.resize();
            return false;
          }
        })
      });

      $tabPublish = $('<li/>', {
        'html': $('<a/>', {
          'text': 'Publish',
          'click': function() {
            $workflow.hide();
            $tabWorkflow.removeClass('state-selected');
            $publish.show();
            $tabPublish.addClass('state-selected');
            $win.resize();
            return false;
          }
        })
      });

      $tabs.append($tabWorkflow);
      $tabs.append($tabPublish);
      $workflow.before($tabs);

      if ($('.widget-publishingWorkflowState').length > 0) {
        $tabWorkflow.find('a').click();

      } else {
        $tabPublish.find('a').click();
      }
    }
  });

  (function() {
    var SUBMITTING_DATA = 'bsp-publish-submitting';

    $(document).on('submit', '.contentForm', function(event) {
      $.data($(event.target)[0], SUBMITTING_DATA, true);
    });

    $(document).on('click', '.widget-publishing button, .widget-publishing :submit', function(event) {
      return !$.data($(event.target).closest('.contentForm')[0], SUBMITTING_DATA);
    });
  })();
});
