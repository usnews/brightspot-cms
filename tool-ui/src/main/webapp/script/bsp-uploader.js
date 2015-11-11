(function (globals, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['jquery'], factory);

  } else {
    factory(globals.$, globals);
  }

})(this, function ($, globals) {

  return {

    '_defaultOptions': {
      path: '/_upload'
    },

    'init': function (input, options) {
      var plugin = this;

      plugin.settings = $.extend({}, plugin._defaultOptions, options);
      plugin.el = $(input);

      //TODO: check browser support

      $(plugin.el).on('change', function (event) {

        if (this.files.length === 0) {
          return;
        }

        plugin.upload(this);
      });
    },


    /**
     * Attaches handlers via closures to maintain
     * plugin scoping.
     *
     * @param {XMLHttpRequest} request
     */
    '_attachEventHandlers': function(request) {

      var plugin = this;

      request.upload.addEventListener("progress", (function (plugin) {
        return function (event) {
          plugin.progress(event, plugin)
        };
      })(plugin), false);

      request.upload.addEventListener("load", (function (plugin) {
        return function (event) {
          plugin.loaded(event, plugin);
        }
      })(plugin), false);

      request.upload.addEventListener("error", (function (plugin) {
        return function (event) {
          plugin.error(event, plugin);
        }
      })(plugin), false);

      request.upload.addEventListener("abort", (function (plugin) {
        return function (event) {
          plugin.abort(event, plugin);
        }
      })(plugin), false);

      request.onreadystatechange = function() {
        if (request.readyState !== XMLHttpRequest.DONE) {
          return;
        }
        if (request.status == 200) {
          plugin.success(request.responseText);
        } else {
          plugin.error(request.responseText);
        }
      };
    },

    'upload': function () {

      var plugin = this;
      var $input = plugin.el;
      var inputName = $input.attr('name');

      plugin.files = $input.prop('files');

      plugin.before();

      var request = new XMLHttpRequest();
      var data = new FormData();

      data.append('fileParameter', inputName);
      $.each(plugin.files, function (i, item) {
        data.append(inputName, item);
      });

      plugin._attachEventHandlers(request);

      request.open("POST", plugin.settings.path);
      request.send(data);
    },

    /**
     * Will be invoked once before the upload request is made.
     */
    'before': function () {
      // No default
    },

    /**
     * Invoked by the request event for 'progress'. Use event.loaded to
     * get the amount of work completed, and event.total to get the total
     * amount of work to be done.
     *
     * @param {ProgressEvent} event
     */
    'progress': function (event) {
      // No default
    },

    /**
     * Will be invoked if the upload request is aborted.
     */
    'abort': function () {
      // No default
    },

    /**
     * Will be invoked once the request has transferred data.
     */
    'loaded': function () {
      // No default
    },

    /**
     * Will be invoked once the response is returned with a successful status code
     */
    'success': function() {
      // No default
    },

    /**
     * Will be invoked on an error during the upload request.
     */
    'error': function () {
      // No default
    }
  };
});

//# sourceURL=bsp-uploader.js
