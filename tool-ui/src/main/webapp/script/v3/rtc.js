define([ 'jquery', 'bsp-utils', 'atmosphere' ], function($, bsp_utils, atmosphere) {
  if (DISABLE_RTC) {
    return {
      restore: function () {
      },

      receive: function () {
      },

      execute: function () {
      }
    };
  }

  var request = {
    url: '/_rtc',
    contentType: 'application/json',
    fallbackTransport: 'sse',
    maxReconnectOnClose: 0,
    trackMessageLength: true,
    transport: 'sse'
  };

  var socket;
  var subscribe = bsp_utils.throttle(5000, function() {
    socket = atmosphere.subscribe(request);
  });

  var restores = [ ];

  var broadcastCallbacks = { };

  var isOnline = false;
  var offlineMessages = [ ];
  var onlineMessages = {
    push: function(message) {
      socket.push(JSON.stringify(message));
    }
  };

  request.onOpen = function() {
    isOnline = true;

    $.each(restores, function(i, restore) {
      onlineMessages.push(restore.message);

      var callback = restore.callback;

      if (callback) {
        callback();
      }
    });

    $.each(offlineMessages, function(i, message) {
      onlineMessages.push(message);
    });

    offlineMessages = [ ];

    if (localStorage) {
      var KEY_PREFIX = 'brightspot.rtc.socket.';
      var INTERVAL = 1000;

      setInterval(function() {
        localStorage.setItem(KEY_PREFIX + socket.getUUID(), '' + $.now());

        for (var i = 0, length = localStorage.length; i < length; ++ i) {
          var key = localStorage.key(i);

          if (key &&
              key.indexOf(KEY_PREFIX) === 0 &&
              parseInt(localStorage.getItem(key), 10) + (INTERVAL * 5) < $.now()) {

            localStorage.removeItem(key);
            (isOnline ? onlineMessages : offlineMessages).push({
              type: 'disconnect',
              sessionId: key.substring(KEY_PREFIX.length)
            });
          }
        }
      }, INTERVAL);
    }
  };

  request.onClose = function() {
    isOnline = false;
  };

  request.onError = function() {
    isOnline = false;

    subscribe();
  };

  function processMessage(message) {
    var messageJson = JSON.parse(message);
    var callbacks = broadcastCallbacks[messageJson.broadcast];

    if (callbacks) {
      $.each(callbacks, function(i, callback) {
        callback(messageJson.data);
      });
    }
  }

  request.onMessage = function(response) {
    processMessage(response.responseBody);
  };

  request.onMessagePublished = function(response) {
    $.each(response.messages, function(i, message) {
      processMessage(message);
    });
  };

  subscribe();

  return {
    restore: function(state, data, callback) {
      restores.push({
        callback: callback,
        message: {
          type: 'state',
          className: state,
          data: data
        }
      });
    },

    receive: function(broadcast, callback) {
      (broadcastCallbacks[broadcast] = broadcastCallbacks[broadcast] || [ ]).push(callback);
    },

    execute: function(action, data) {
      (isOnline ? onlineMessages : offlineMessages).push({
        type: 'action',
        className: action,
        data: data
      });
    }
  };
});
