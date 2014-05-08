define([
    'jquery',
    'bsp-utils',
    'spectrum' ],

function($, bsp_utils) {
    bsp_utils.onDomInsert(document, ':text.color', {
        'insert': function(input) {
            $(input).spectrum({
                'allowEmpty': true,
                'cancelText': 'Cancel',
                'chooseText': 'OK',
                'preferredFormat': 'hex6',
                'showAlpha': true,
                'showInitial': true,
                'showInput': true
            });
        }
    });
});
