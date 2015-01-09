define([
    'jquery',
    'bsp-utils' ],

function($, bsp_utils) {
    bsp_utils.onDomInsert(document, '.contentLock', {
        'insert': function(container) {
            var $container = $(container);

            if ($container.attr('data-content-locked-out') === 'true') {
                $container.find(':input, button, .event-input-disable').trigger('input-disable', [ true ]);
                $win.resize();
            }
        }
    });

    var KEY_PREFIX = "cms.contentLock.";
    var STORAGE = window.localStorage;

    function lock(contentId) {
        STORAGE.setItem(KEY_PREFIX + contentId, +new Date());
    }

    function unlock(contentId) {
        STORAGE.removeItem(KEY_PREFIX + contentId);

        $.ajax({
            'url': CONTEXT_PATH + '/contentUnlock',
            'data': { 'id': contentId },
            'async': false,
            'cache': false
        });
    }

    setInterval(function() {
        var itemIndex;
        var itemLength = STORAGE.length;
        var key;
        var now = +new Date();
        var contentId;
        var contentIdsToUnlock = [];

        for (itemIndex = 0; itemIndex < itemLength; ++ itemIndex) {
            key = STORAGE.key(itemIndex);

            if (key &&
                    key.indexOf(KEY_PREFIX, 0) === 0 &&
                    parseInt(STORAGE.getItem(key), 10) + 5000 < now) {
                contentId = key.substring(KEY_PREFIX.length);

                contentIdsToUnlock.push(contentId);
            }
        }

        itemLength = contentIdsToUnlock.length;

        for (itemIndex = 0; itemIndex < itemLength; ++ itemIndex) {
            unlock(contentIdsToUnlock[itemIndex]);
        }
    }, 1000);

    window.bspContentLock = lock;
    window.bspContentUnlcok = unlock;

    return {
        'lock': lock,
        'unlock': unlock
    };
});
