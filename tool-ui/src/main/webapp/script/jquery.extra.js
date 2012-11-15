if (typeof jQuery !== 'undefined') (function($) {

// Standard plugin structure.
$.plugin = function(name, methods) {

    methods.data = function(attribute, value) {
        var data = this.data(name);
        if (!data) {
            data = { };
            this.data(name, data);
        }
        if (typeof value === 'undefined') {
            return data[attribute];
        } else {
            data[attribute] = value;
            return this;
        }
    };

    $.fn[name] = function(method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (!method || typeof method === 'object') {
            return methods.init.apply(this, arguments);
        } else {
            return $.error('[' + method + '] method does not exist on [' + name + '] plugin!');
        }
    };
};

// Executes a function at most once per context.
$.once = function(onceFunction) {
    var cache = [ ];
    return function() {
        for (var i = 0, s = cache.length; i < s; ++ i) {
            var item = cache[i];
            if (item[0] === this) {
                return item[1];
            }
        }
        var value = onceFunction.apply(this, arguments);
        cache.push([ this, value]);
        return value;
    };
};

// Throttles the excution of a function to run at most every set interval.
$.throttle = function(interval, throttledFunction) {

    if (interval <= 0) {
        return throttledFunction;
    }

    var lastTrigger = 0;
    var timeout;
    var lastArguments;
    return function() {

        lastArguments = arguments;
        if (timeout) {
            return;
        }

        var context = this;
        var now = +new Date();
        var delay = interval - now + lastTrigger;

        if (delay <= 0) {
            lastTrigger = now;
            throttledFunction.apply(context, lastArguments);

        } else {
            timeout = setTimeout(function() {
                lastTrigger = now;
                timeout = null;
                throttledFunction.apply(context, lastArguments);
            }, delay);
        }
    };
};

// Handles mouse dragging movements.
var dragMoveCallback;
var dragEndCallback;
$.drag = function(moveCallback, endCallback) {
    if (dragMoveCallback && dragEndCallback) {
        dragEndCallback();
    }
    dragMoveCallback = $.throttle(50, moveCallback);
    dragEndCallback = endCallback;
};

$(function() {
    var $window = $(window);
    $window.mousemove(function(event) {
        if (dragMoveCallback) {
            dragMoveCallback(event);
        }
    });
    $window.mouseup(function(event) {
        if (dragEndCallback) {
            dragEndCallback();
        }
        dragMoveCallback = null;
        dragEndCallback = null;
    });
});

// Similar to jQuery.livequery except it works on loaded elements and never runs more than once per element.
$.fn.liveInit = function() {
    if (!$.isFunction(arguments[0]) || arguments.length != 1) {
        $.fn.livequery.apply(this, arguments);
    } else if (this.selector) {
        $.fn.livequery.call(this, $.once(arguments[0]));
    } else {
        $.fn.each.apply(this, arguments);
    }
};

// Returns an accurate CSS z-index, taking all the parents into account.
$.fn.zIndex = function() {
    var zIndex;
    for (var $parent = this; $parent.length > 0; $parent = $parent.parent()) {
        try {
            zIndex = parseInt($parent.css('z-index'), 10);
        } catch (error) {
            break;
        }
        if (!isNaN(zIndex)) {
            break;
        }
    }
    return zIndex;
};

})(jQuery);
