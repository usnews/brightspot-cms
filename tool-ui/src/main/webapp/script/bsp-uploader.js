(function (globals, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['jquery'], factory);

  } else {
    factory(globals.$, globals);
  }

})(this, function ($, globals) {

  return {

    '_defaultOptions': {
      path: '/_upload',
      maxConcurrent: 3
    },

    'init': function (input, options) {
      var plugin = this;

      plugin.settings = $.extend({}, plugin._defaultOptions, options);
      plugin.el = $(input);
      plugin.isMultiple = plugin.el.attr('multiple') ? true : false;

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
     * @param {Deferred} deferred
     * @param {Integer} i
     */
    '_attachEventHandlers': function(request, deferred, i) {

      var plugin = this;

      request.upload.addEventListener("progress", (function (plugin, i) {
        return function (event) {
          plugin.progress(event, i)
        };
      })(plugin, i), false);

      //request.upload.addEventListener("load", (function (plugin, i) {
      //  return function (event) {
      //    plugin.loaded(event, i);
      //  }
      //})(plugin), false);

      //request.upload.addEventListener("error", (function (plugin, i) {
      //  return function (event) {
      //    plugin.error(event, i);
      //  }
      //})(plugin, i), false);

      //request.upload.addEventListener("abort", (function (plugin, i) {
      //  return function (event) {
      //    plugin.abort(event, data, i);
      //  }
      //})(plugin, i), false);

      request.onreadystatechange = function() {
        if (request.readyState !== XMLHttpRequest.DONE) {
          return;
        }

        plugin.afterEach(request.responseText, i);
        deferred.resolve();
      };
    },

    'upload': function () {

      var plugin = this;
      var $input = plugin.el;
      var inputName = $input.attr('name');

      plugin.files = $input.prop('files');

      plugin.before();

      plugin.requests = [ ];
      plugin.requestsData = [ ];
      plugin.deferreds  = [ ];

      // create requests
      $.each(plugin.files, function (i, file) {

        var request, data;
        request = new XMLHttpRequest();
        data = new FormData();

        data.append('fileParameter', inputName);
        data.append(inputName, file);

        plugin.requests.push(request);
        plugin.requestsData.push(data);
        plugin.deferreds.push($.Deferred());

        plugin._attachEventHandlers(request, plugin.deferreds[i], i);
        plugin.beforeEach(request, i);

        request.open("POST", plugin.settings.path);
        request.send(data);
      });

      $.when.apply(null, plugin.deferreds).done(function () {
        plugin.after();
      });
    },

    /**
     * Will be invoked once before ANY request.
     */
    'before': function () {
      // No default
    },

    /**
     * Will be invoked once before EACH request.
     *
     * @param {XMLHttpRequest} request
     * @param {Integer} i
     */
    'beforeEach': function (request, i) {
      // No default
    },

    /**
     * Invoked by a request event for 'progress'. Use event.loaded to
     * get the amount of work completed, and event.total to get the total
     * amount of work to be done.
     *
     * @param {ProgressEvent} event
     */
    'progress': function (event) {
      // No default
    },

    /**
     * Will be invoked once after EACH request has a response
     *
     * @param {XMLHttpRequest} request
     * @param {Integer} i
     */
    'afterEach': function (request, i) {
      console.log('AFTER EACH');
    },

    /**
     * Will be invoked once after ALL requests have been responded.
     *
     */
    'after': function () {
      console.log('AFTER');
    }
  };
});

//# sourceURL=bsp-uploader.js
