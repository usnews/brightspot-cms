define([
    'jquery',
    'bsp-utils' ],

function($, bsp_utils) {

    // Allow dashboard widgets to move around.
    bsp_utils.onDomInsert(document, '.dashboardCell', {
        'insert': function(cell) {
            var $cell = $(cell);
            var $collapse;
            var $moveContainer;
            var saveDashboard;
            var $moveUp;
            var $moveDown;
            var $moveLeft;
            var $moveRight;

            $collapse = $('<span/>', {
                'class': 'dashboardCollapse',
                'click': function() {
                    $cell.toggleClass('dashboardCell-collapse');
                    saveDashboard();
                }
            });

            $moveContainer = $('<span/>', {
                'class': 'dashboardMoveContainer',
                'click': function() {
                    $(this).toggleClass('dashboardMoveContainer-open');
                }
            });

            saveDashboard = function() {
                var $dashboard = $cell.closest('.dashboard');
                var $columns;
                var widgets = [ ];
                var widgetsCollapse = [ ];

                $dashboard.find('.dashboardColumn:empty').remove();
                $columns = $dashboard.find('.dashboardColumn');
                $dashboard.attr('data-columns', $columns.length);

                $columns.each(function() {
                    var w = widgets[widgets.length] = [ ];

                    $(this).find('.dashboardCell').each(function() {
                        var $cell = $(this),
                                name = $cell.attr('data-widget');

                        w[w.length] = name;

                        if ($cell.hasClass('dashboardCell-collapse')) {
                            widgetsCollapse[widgetsCollapse.length] = name;
                        }
                    });
                });

                $.ajax({
                    'type': 'post',
                    'url': CONTEXT_PATH + '/misc/updateUserSettings',
                    'data': {
                        'action': 'dashboardWidgets-position',
                        'widgets': JSON.stringify(widgets),
                        'widgetsCollapse': JSON.stringify(widgetsCollapse)
                    }
                });
            };

            $moveUp = $('<span/>', {
                'class': 'dashboardMoveUp',
                'click': function() {
                    $cell.prev().before($cell);
                    saveDashboard();
                }
            });

            $moveDown = $('<span/>', {
                'class': 'dashboardMoveDown',
                'click': function() {
                    $cell.next().after($cell);
                    saveDashboard();
                }
            });

            $moveLeft = $('<span/>', {
                'class': 'dashboardMoveLeft',
                'click': function() {
                    var $column = $cell.closest('.dashboardColumn');
                            $prevColumn = $column.prev();

                    if ($prevColumn.length === 0) {
                        $prevColumn = $('<div/>', {
                            'class': 'dashboardColumn'
                        });

                        $column.before($prevColumn);
                    }

                    $prevColumn.prepend($cell);
                    saveDashboard();
                }
            });

            $moveRight = $('<span/>', {
                'class': 'dashboardMoveRight',
                'click': function() {
                    var $column = $cell.closest('.dashboardColumn');
                            $nextColumn = $column.next();

                    if ($nextColumn.length === 0) {
                        $nextColumn = $('<div/>', {
                            'class': 'dashboardColumn'
                        });

                        $column.after($nextColumn);
                    }

                    $nextColumn.prepend($cell);
                    saveDashboard();
                }
            });

            $moveContainer.append($moveUp);
            $moveContainer.append($moveDown);
            $moveContainer.append($moveLeft);
            $moveContainer.append($moveRight);

            $cell.append($collapse);
            $cell.append($moveContainer);
        }
    });
});
