requirejs.config({
    shim: {
    }
});

require([
    'jquery',

    'bsp-autoexpand',
    'bsp-autosubmit',
    'bsp-utils'
], function() {
   var $ = arguments[0];
});
