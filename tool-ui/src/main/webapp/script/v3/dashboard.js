define([ 'jquery', 'v3/rtc' ], function($, rtc) {
  rtc.receive('com.psddev.cms.tool.page.content.PublishBroadcast', function(data) {
    $('.dashboard-widget').each(function() {
      var $widget = $(this);

      if ($widget.find('[data-preview-url]').length === 0) {
        return;
      }

      var widgetUrl = $widget.attr('data-dashboard-widget-url');

      if (widgetUrl) {
        $.ajax({
          'cache': false,
          'type': 'get',
          'url': widgetUrl,
          'complete': function(response) {
            $widget.html(response.responseText);
            $widget.trigger('create');
            $widget.trigger('load');
            $widget.trigger('frame-load');
          }
        });
      }
    })
  });
});