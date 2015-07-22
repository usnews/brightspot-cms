define([ 'jquery', 'atmosphere' ], function($, atmosphere) {
  var request = {
    url: '/_rtc',
    contentType: 'application/json',
    fallbackTransport: 'long-polling',
    trackMessageLength: true,
    transport: 'sse'
  };

  var broadcastCallbacks = { };
  var messages = [ ];
  var socket;

  request.onOpen = function() {
    var oldMessages = messages;

    messages = {
      push: function(message) {
        socket.push(JSON.stringify($.extend(message, {
          resource: socket.getUUID()
        })));
      }
    };

    $.each(oldMessages, function(i, message) {
      messages.push(message);
    });
  };

  request.onMessage = function(response) {
    var message = JSON.parse(response.responseBody);
    var callbacks = broadcastCallbacks[message.broadcast];

    if (callbacks) {
      $.each(callbacks, function(i, callback) {
        callback(message.data);
      });
    }
  };

  socket = atmosphere.subscribe(request);

  return {
    restore: function(state, data) {
      messages.push({
        state: state,
        data: data
      });
    },

    receive: function(broadcast, callback) {
      (broadcastCallbacks[broadcast] = broadcastCallbacks[broadcast] || [ ]).push(callback);
    },

    execute: function(action, data) {
      messages.push({
        action: action,
        data: data
      });
    }
  };
});
