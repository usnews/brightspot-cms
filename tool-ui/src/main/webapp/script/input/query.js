define([
    'jquery',
    'bsp-utils' ],

function($, bsp_utils) {
    var $frames;
    var $fields = $();

    bsp_utils.onDomInsert(document, '.queryField', {
        'insert': function(field) {
            var $field = $(field);
            var inputValue = $field.find('input').val();
            var search;
            var $body = $(window.document.body);
            var $frame;

            $fields = $fields.add($field);

            if (!$frames) {
                $frames = $('<div/>', {
                    'class': 'queryField_frames'
                });

                $(window.document.body).append($frames);
            }

            $frame = $('<div/>', {
                'class': 'frame',
                'html': $('<form/>', {
                    'method': 'post',
                    'action': CONTEXT_PATH + '/queryField',
                    'html': $('<input/>', {
                        'type': 'hidden',
                        'name': 'search',
                        'value': inputValue ? JSON.stringify(JSON.parse(inputValue)['cms.ui.search']) : ''
                    })
                })
            });

            $.data($field[0], 'query-$frame', $frame);
            $.data($frame[0], 'query-$field', $field);
            $frames.append($frame);
            $frames.trigger('create');
        }
    });

    setInterval(function() {
        $fields.filter(':visible').each(function() {
            var $field = $(this);
            var $frame = $.data($field[0], 'query-$frame');
            var fieldOffset = $field.offset();

            $frame.css({
                'left': fieldOffset.left,
                'position': 'absolute',
                'top': fieldOffset.top,
                'z-index': 1000000
            });

            $frame.outerWidth($field.outerWidth());
            $field.outerHeight($frame.outerHeight());
            $frame.show();
        });

        $fields.filter(':not(:visible)').each(function() {
            var $field = $(this);
            var $frame = $.data($field[0], 'query-$frame');

            $frame.hide();
        });
    }, 100);
});
