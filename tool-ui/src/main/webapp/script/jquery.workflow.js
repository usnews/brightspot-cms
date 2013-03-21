/** Workflows. */
(function($, win, undef) {

var $win = $(win);

$.plugin2('workflow', {
    '_create': function(textarea) {
        var $textarea = $(textarea),
                definition = $.parseJSON($textarea.val()) || { },
                $visual,
                drawArrow,
                $arrows,
                $transitionInputs = { },
                addTransitionTarget,
                appendTransitionAdd,
                addState,
                nextStateId = 0,
                $stateAdd,
                $stateInitial,
                $stateFinal;

        $visual = $('<div/>', {
            'class': 'workflowVisual'
        });

        drawArrow = function(context, color, $source, targetX, targetY) {
            var sourcePosition = $source.position(),
                    sourceX = sourcePosition.left,
                    sourceY = sourcePosition.top + $source.outerHeight() * 0.1,
                    sourceDirection,
                    targetDirection,
                    arrowSize;

            if (sourceX > targetX) {
                sourceDirection = -1;
                targetDirection = 1;

            } else {
                sourceX += $source.outerWidth();
                sourceDirection = 1;
                targetDirection = -1;
            }

            sourceControlX = sourceX + sourceDirection * 100;
            sourceControlY = sourceY;
            targetControlX = targetX + targetDirection * 100;
            targetControlY = targetY;

            context.strokeStyle = color;
            context.fillStyle = color;

            context.lineWidth = 1.0;
            context.beginPath();
            context.moveTo(sourceX, sourceY);
            context.bezierCurveTo(sourceControlX, sourceControlY, targetControlX, targetControlY, targetX, targetY);
            context.stroke();

            arrowSize = targetX > targetControlX ? 5 : -5;
            context.beginPath();
            context.moveTo(targetX, targetY);
            context.lineTo(targetX - 2 * arrowSize, targetY - arrowSize);
            context.lineTo(targetX - 2 * arrowSize, targetY + arrowSize);
            context.closePath();
            context.fill();

            return {
                'sourceX': sourceX,
                'sourceY': sourceY,
                'targetX': targetX,
                'targetY': targetY
            };
        };

        $visual.append($arrows = $('<canvas/>', {
            'css': {
                'left': 0,
                'position': 'absolute',
                'top': 0,
                'z-index': -1
            }
        }));
        
        $arrows.bind('redraw', function(event, $source, targetX, targetY) {
            var context = this.getContext('2d'),
                    width = $arrows.outerWidth(),
                    height = $arrows.outerHeight();

            definition = { 'states': [ ], 'transitions': [ ] };
            context.clearRect(0, 0, $visual.width(), $visual.height());
            $visual.find('.workflowTransitionName').hide();

            $visual.find('.workflowState').each(function() {
                var $source = $(this),
                        sourceId = $source.attr('data-id'),
                        sourcePosition = $source.position(),
                        targets = $.data(this, 'workflow-targets'),
                        targetYOffset = 0.9;

                if (!(sourceId === 'initial' ||
                        sourceId === 'final')) {
                    definition.states.push({
                        'id': sourceId,
                        'name': $source.find(':input').val(),
                        'left': sourcePosition.left / width,
                        'top': sourcePosition.top / height
                    });
                }

                if (targets) {
                    $.each(targets, function(index, targetData) {
                        var targetId = targetData.target,
                                $target = $visual.find('[data-id="' + targetId + '"]'),
                                transition,
                                $input,
                                targetPosition,
                                targetX,
                                targetY,
                                bound,
                                boundOffset;

                        if ($target.length === 0) {
                            return;
                        }

                        transition = sourceId + '/' + targetId;
                        $input = $transitionInputs[transition];

                        if (!$input) {
                            $visual.append($input = $('<input/>', {
                                'type': 'text',
                                'class': 'workflowTransitionName',
                                'value': targetData.name || ''
                            }));

                            $transitionInputs[transition] = $input;
                        }

                        targetPosition = $target.position();
                        targetX = targetPosition.left;
                        targetY = targetPosition.top + $target.outerHeight() * targetYOffset;
                        targetYOffset -= 0.1;
                        bound = drawArrow(context, 'black', $source, targetX, targetY);
                        boundOffset = bound.sourceX < bound.targetX ? 0.3 : 0.7;

                        $input.css({
                            'left': (bound.sourceX + bound.targetX - $input.outerWidth()) / 2,
                            'position': 'absolute',
                            'top': (bound.sourceY + bound.targetY - $input.outerHeight()) / 2,
                            'transform': 'rotate(' + (Math.atan((bound.targetY - bound.sourceY) / (bound.targetX - bound.sourceX)) * 180 / Math.PI) + 'deg)'
                        });

                        $input.show();

                        definition.transitions.push({
                            'name': $input.val(),
                            'source': sourceId,
                            'target': targetId
                        });
                    });
                }
            });

            $textarea.val(JSON.stringify(definition));

            if ($source) {
                drawArrow(context, 'black', $source, targetX, targetY);
            }
        });

        $visual.delegate(':input', 'input', function() {
            $arrows.trigger('redraw');
        });

        addTransitionTarget = function(targetData) {
            var $source = $visual.find('[data-id="' + targetData.source + '"]'),
                    targets;

            if ($source.length > 0) {
                targets = $.data($source[0], 'workflow-targets') || [ ];
                targets.push(targetData);
                $.data($source[0], 'workflow-targets', targets);
            }
        };

        appendTransitionAdd = function($state) {
            $state.append($('<a/>', {
                'class': 'workflowTransitionAdd',
                'text': 'Add ' + ($textarea.attr('data-transition-label') || 'Transition'),
                'click': function() {
                    if ($visual.is('.workflowVisual-addingTransition')) {
                        return true;
                    }

                    $visual.addClass('workflowVisual-addingTransition');

                    $win.bind('mousemove.workflows', $.throttle(50, function(event) {
                        var visualOffset = $visual.offset();

                        $arrows.trigger('redraw', [
                                $state,
                                event.pageX - visualOffset.left,
                                event.pageY - visualOffset.top ]);
                    }));

                    $win.bind('click.workflows', function(event) {
                        var $target = $(event.target).closest('.workflowState');

                        if ($target.length > 0) {
                            addTransitionTarget({
                                'source': $state.attr('data-id'),
                                'target': $target.attr('data-id')
                            });
                        }

                        $visual.removeClass('workflowVisual-addingTransition');
                        $win.unbind('.workflows');
                        $arrows.trigger('redraw');

                        return false;
                    });

                    return false;
                }
            }));
        };

        addState = function(stateData) {
            var $state;

            $visual.prepend($state = $('<div/>', {
                'class': 'workflowState',
                'data-id': stateData.id,
                'css': {
                    'left': (stateData.left * 100) + '%',
                    'top': (stateData.top * 100) + '%'
                },
                'mousedown': function(event) {
                    $.drag(this, event, function(event, data) {
                        data.delta = $state.position();
                        data.delta.left -= event.pageX;
                        data.delta.top -= event.pageY;

                    }, function(event, data) {
                        $state.css({
                            'left': event.pageX + data.delta.left,
                            'top': event.pageY + data.delta.top
                        });

                        $arrows.trigger('redraw');

                    }, function(event) {

                    });
                }
            }));

            $state.append($('<input/>', {
                'type': 'text',
                'value': stateData.name || ''
            }));

            $state.append($('<a/>', {
                'class': 'workflowStateRemove',
                'text': 'Remove',
                'click': function() {
                    $state.remove();
                    $arrows.trigger('redraw');

                    return false;
                }
            }));

            appendTransitionAdd($state);

            return $state;
        };

        $visual.append($stateAdd = $('<a/>', {
            'class': 'workflowStateAdd',
            'text': 'Add ' + ($textarea.attr('data-state-label') || 'State'),
            'click': function() {
                addState({
                    'id': nextStateId,
                    'left': Math.random() * 0.6 + 0.1,
                    'top': Math.random() * 0.7 + 0.1
                });

                ++ nextStateId;

                return false;
            }
        }));

        $visual.append($stateInitial = $('<div/>', {
            'class': 'workflowState',
            'data-id': 'initial',
            'text': $textarea.attr('data-state-initial') || 'Initial State'
        }));

        appendTransitionAdd($stateInitial);

        $visual.append($stateFinal = $('<div/>', {
            'class': 'workflowState',
            'data-id': 'final',
            'text': $textarea.attr('data-state-final') || 'Final State'
        }));

        if (definition.states) {
            $.each(definition.states, function() {
                var id = parseInt(this.id, 10);

                addState(this);

                if (id >= nextStateId) {
                    nextStateId = id + 1;
                }
            });
        }

        $textarea.after($visual);
        $textarea.hide();

        $arrows.attr({
            'width': $visual.width(),
            'height': $visual.height()
        });

        if (definition.transitions) {
            $.each(definition.transitions, function() {
                addTransitionTarget(this);
            });

            $arrows.trigger('redraw');
        }
    }
});

}(jQuery, window));
