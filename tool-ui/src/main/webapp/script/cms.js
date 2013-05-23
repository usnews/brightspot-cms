(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc);

win.cms = win.cms || { };
win.cms.startToolUserPing = function() {
    setInterval(function() {
        $.getJSON(CONTEXT_PATH + '/toolUserPing', function(response) {
            var status = response.status,
                    href = win.location.href,
                    redirect;

            if (status === 'ERROR' &&
                    !(/logIn.jsp/.exec(href))) {
                win.location = CONTEXT_PATH + '/logIn.jsp?forced=true&returnPath=' + encodeURIComponent(win.location);

            } else if (status === 'OK' &&
                    (/logIn.jsp/.exec(href))) {
                redirect = (/[?&]returnPath=([^&]+)/.exec(href) || [ ])[1];
                redirect = redirect ? decodeURIComponent(redirect) : CONTEXT_PATH + '/';
                win.location = redirect;
            }
        });
    }, 2000);
};

// Standard behaviors.
$doc.repeatable('live', '.repeatableForm, .repeatableInputs, .repeatableLayout, .repeatableObjectId');
$doc.repeatable('live', '.repeatableText', {
    'addButtonText': '',
    'removeButtonText': '',
    'restoreButtonText': ''
});

$doc.autoSubmit('live', '.autoSubmit');
$doc.calendar('live', ':text.date');
$doc.dropDown('live', 'select[multiple], select[data-searchable="true"]');
$doc.expandable('live', ':text.expandable, textarea', {
    'shadowClass': 'input'
});

$doc.frame({
    'frameClassName': 'frame',
    'loadingClassName': 'loading',
    'loadedClassName': 'loaded'
});

$doc.imageEditor('live', '.imageEditor');
$doc.objectId('live', ':input.objectId');
$doc.pageLayout('live', '.pageLayout');
$doc.pageThumbnails('live', '.pageThumbnails');
$doc.rte('live', '.richtext');
$doc.spreadsheet('live', '.spreadsheet');
$doc.taxonomy('live', '.taxonomy');
$doc.toggleable('live', '.toggleable');
$doc.workflow('live', '.workflow');

// Remove placeholder text over search input when there's text.
$doc.onCreate('.searchInput', function() {
    var $container = $(this),
            $label = $container.find('> label'),
            $input = $container.find('> :text');

    $input.bind('input', $.run(function() {
        $label.toggle(!$input.val());
    }));
});

// Automatically focus on certain elements.
$doc.onCreate('[autofocus], .autoFocus', function() {
    var focus = doc.activeElement;

    if (!focus || focus === doc || focus === doc.body) {
        $(this).focus();
    }
});

// Hide non-essential items in the permissions input.
$doc.onCreate('.inputContainer .permissions select', function() {
    var $select = $(this);

    $select.bind('change', $.run(function() {
        $select.parent().find('> h2, > ul').toggle($select.find(':selected').val() === 'some');
    }));
});

// Allow dashboard widgets to move around.
$doc.onCreate('.dashboardCell', function() {
    var $cell = $(this),
            $moveContainer,
            saveDashboard,
            $moveUp,
            $moveDown,
            $moveLeft,
            $moveRight;

    $moveContainer = $('<span/>', {
        'class': 'dashboardMoveContainer'
    });

    saveDashboard = function() {
        var $dashboard = $cell.closest('.dashboard'),
                $columns,
                widgets = [ ];

        $dashboard.find('.dashboardColumn:empty').remove();
        $columns = $dashboard.find('.dashboardColumn');
        $dashboard.attr('data-columns', $columns.length);

        $columns.each(function() {
            var w = widgets[widgets.length] = [ ];

            $(this).find('.dashboardCell').each(function() {
                w[w.length] = $(this).attr('data-widget');
            });
        });

        $.ajax({
            'type': 'post',
            'url': CONTEXT_PATH + '/misc/updateUserSettings',
            'data': 'action=dashboardWidgets-position&widgets=' + encodeURIComponent(JSON.stringify(widgets))
        });
    };

    $moveUp = $('<span/>', {
        'class': 'dashboardMoveUp',
        'click': function() {
            $cell.prev().before($cell);
            saveDashboard();

            return false;
        }
    });

    $moveDown = $('<span/>', {
        'class': 'dashboardMoveDown',
        'click': function() {
            $cell.next().after($cell);
            saveDashboard();

            return false;
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

            return false;
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

            return false;
        }
    });

    $moveContainer.append($moveUp);
    $moveContainer.append($moveDown);
    $moveContainer.append($moveLeft);
    $moveContainer.append($moveRight);

    $cell.append($moveContainer);
});

$doc.onCreate('.searchSuggestionsForm', function() {
    var $suggestionsForm = $(this),
            $source = $suggestionsForm.popup('source'),
            $contentForm = $source.closest('.contentForm'),
            search;

    if ($contentForm.length === 0) {
        return;
    }

    search = win.location.search;
    search += search.indexOf('?') > -1 ? '&' : '?';
    search += 'id=' + $contentForm.attr('data-object-id');

    $.ajax({
        'data': $contentForm.serialize(),
        'type': 'post',
        'url': CONTEXT_PATH + '/content/state.jsp' + search,
        'complete': function(request) {
            $suggestionsForm.append($('<input/>', {
                'type': 'hidden',
                'name': 'object',
                'value': request.responseText
            }));

            $suggestionsForm.append($('<input/>', {
                'type': 'hidden',
                'name': 'field',
                'value': $source.closest('.inputContainer').attr('data-field')
            }));

            $suggestionsForm.submit();
        }
    });
});

// Mark changed inputs.
$doc.on('change', '.inputContainer', function() {
    var $container = $(this),
            changed;

    $container.find('input, textarea').each(function() {
        if (this.defaultValue !== this.value) {
            changed = true;
            return;
        }
    });

    if (!changed) {
        $container.find('option').each(function() {
            if (this.defaultSelected !== this.selected) {
                changed = true;
                return;
            }
        });
    }

    $container.toggleClass('state-changed', changed);
});

// Create tabs that organize form fields.
$doc.onCreate('.objectInputs', function() {
    var $container = $(this),
            $inputs = $container.find('> .inputContainer'),
            tabItems = { },
            tabs = [ ],
            $tabs;

    $inputs.each(function() {
        var $input = $(this),
                tabName = $input.attr('data-tab'),
                items;

        if (tabName) {
            items = tabItems[tabName];

            if (!items) {
                items = tabItems[tabName] = [ ];

                tabs.push({
                    'name': tabName,
                    'items': items
                });
            }

            $input.hide();
            items.push($input);
        }
    });

    if (tabs.length > 0) {
        $tabs = $('<ul/>', { 'class': 'tabs' });

        $tabs.bind('tabs-select.tabs', function(event) {
            $(this).find('> li').removeClass('state-selected');
            $(event.target).closest('li').addClass('state-selected');
        });

        $tabs.append($('<li/>', {
            'class': 'state-selected',
            'html': $('<a/>', {
                'text': 'Main',
                'click': function() {
                    $(this).trigger('tabs-select');

                    $inputs.show();
                    $.each(tabs, function(i, tab) {
                        $.each(tab.items, function(j, $item) {
                            $item.hide();
                        });
                    });

                    return false;
                }
            })
        }));

        $.each(tabs, function(i, tab) {
            $tabs.append($('<li/>', {
                'html': $('<a/>', {
                    'text': tab.name,
                    'click': function() {
                        $(this).trigger('tabs-select');

                        $inputs.hide();
                        $.each(tab.items, function(i, $item) {
                            $item.show();
                        });

                        return false;
                    }
                })
            }));
        });

        $container.prepend($tabs);
    }
});

// Content diff with a side by side view.
$doc.onCreate('.contentDiff', function() {
    var $container = $(this),
            $tabs,
            $tabEdit,
            $tabSideBySide,
            $left = $container.find('> .contentDiffLeft'),
            $right = $container.find('> .contentDiffRight'),
            getValues;

    $tabs = $('<ul/>', {
        'class': 'tabs'
    });

    $tabEdit = $('<li/>', {
        'html': $('<a/>', {
            'text': 'Edit',
            'click': function() {
                $container.trigger('contentDiff-edit');
                return false;
            }
        })
    });

    $tabSideBySide = $('<li/>', {
        'html': $('<a/>', {
            'text': 'Side By Side',
            'click': function() {
                $container.trigger('contentDiff-sideBySide');
                return false;
            }
        })
    });

    $container.bind('contentDiff-edit', function() {
        $container.add($('.widget-publishing')).removeClass('contentDiff-sideBySide').addClass('contentDiff-edit');
        $tabs.find('li').removeClass('state-selected');
        $tabEdit.addClass('state-selected');

        $left.find('> .objectInputs > .inputContainer').css('height', '');
        $right.find('> .objectInputs > .inputContainer').css('height', '');
    });

    $container.bind('contentDiff-sideBySide', function() {
        $container.add($('.widget-publishing')).removeClass('contentDiff-edit').addClass('contentDiff-sideBySide');
        $tabs.find('li').removeClass('state-selected');
        $tabSideBySide.addClass('state-selected');

        $left.find('> .objectInputs > .inputContainer').each(function() {
            var $leftInput = $(this),
                    $rightInput = $right.find('> .objectInputs > .inputContainer[data-field="' + $leftInput.attr('data-field') + '"]');

            setTimeout(function() {
                $leftInput.add($rightInput).height(Math.max($leftInput.height(), $rightInput.height()));
            }, 500);
        });
    });

    getValues = function($input) {
        var fakeId = $input.closest('[data-fake-id]').attr('data-fake-id'),
                realId = $input.closest('[data-real-id]').attr('data-real-id'),
                values = $input.find(':input, select, textarea').serialize();

        return fakeId && realId ? values.replace(new RegExp('(^|&)' + fakeId + '%2F', 'g'), '$1' + realId + '%2F') : values;
    };

    $left.find('> .objectInputs > .inputContainer').each(function() {
        var $leftInput = $(this),
                $rightInput = $right.find('> .objectInputs > .inputContainer[data-field="' + $leftInput.attr('data-field') + '"]'),
                height = Math.max($leftInput.outerHeight(true), $rightInput.outerHeight(true));

        if (getValues($leftInput) === getValues($rightInput)) {
            $leftInput.addClass('contentDiffSame');
            $rightInput.addClass('contentDiffSame');
        }
    });

    $left.find('> .objectInputs > .inputContainer > .inputSmall > textarea:not(.richtext)').each(function() {
        var $leftText = $(this),
                $rightText = $right.find('> .objectInputs > .inputContainer[data-field="' + $leftText.closest('.inputContainer').attr('data-field') + '"] textarea:not(.richtext)'),
                left = $leftText.val(),
                right = $rightText.val(),
                diffs = JsDiff.diffWords(left, right),
                $leftCopy = $('<div/>', { 'class': 'contentDiffCopy' }),
                $rightCopy = $('<div/>', { 'class': 'contentDiffCopy' });

        $.each(diffs, function(i, diff) {
            if (!diff.added) {
                $leftCopy.append(diff.removed ?
                        $('<span/>', { 'class': 'contentDiffRemoved', 'text': diff.value }) :
                        diff.value);
            }
        });

        $.each(diffs, function(i, diff) {
            if (!diff.removed) {
                $rightCopy.append(diff.added ?
                        $('<span/>', { 'class': 'contentDiffAdded', 'text': diff.value }) :
                        diff.value);
            }
        });

        $leftText.addClass('contentDiffText');
        $leftText.before($leftCopy);

        $rightText.addClass('contentDiffText');
        $rightText.before($rightCopy);
    });

    $tabs.append($tabEdit);
    $tabs.append($tabSideBySide);
    $container.prepend($tabs);
    $container.trigger($right.is('.contentDiffCurrent') ?
            'contentDiff-sideBySide' :
            'contentDiff-edit');
});

$doc.onCreate('.searchAdvancedResult', function() {
    var $result = $(this);

    $result.on('change', ':checkbox', function() {
        $result.find('.actions .action').each(function() {
            var $action= $(this),
                    text = $action.text();

            if ($result.find(':checkbox:checked').length > 0) {
                $action.text(text.replace('All', 'Selected'));
            } else {
                $action.text(text.replace('Selected', 'All'));
            }
        });
    });
});

// Show stack trace when clicking on the exception message.
$doc.delegate('.exception > *', 'click', function() {
    $(this).find('> .stackTrace').toggle();
});

// Soft validation based on suggested sizes.
(function() {
    var TAG_RE = /<[^>]*>/g,
            TRIM_RE = /^\s+|\s+$/g,
            WHITESPACE_RE = /\s+/;

    $doc.delegate('.inputSmall-text :text, .inputSmall-text textarea', 'change.wordCount focus.wordCount input.wordCount', function() {
        var $input = $(this),
                minimum = +$input.attr('data-suggested-minimum'),
                maximum = +$input.attr('data-suggested-maximum'),
                $container = $input.closest('.inputContainer'),
                $toolbar = $container.find('.rte-toolbar').eq(0),
                value = ($input.val() || '').replace(TAG_RE, '').replace(TRIM_RE, ''),
                cc = value.length,
                wc = value ? value.split(WHITESPACE_RE).length : 0;

        if ($toolbar.length > 0) {
            $container = $toolbar;
        }

        $container.attr('data-count-message',
                cc < minimum ? 'Too Short' :
                cc > maximum ? 'Too Long' :
                wc + 'w ' + cc + 'c');
    });
})();

// Make sure that most elements are always in view.
(function() {
    var lastScrollTop = $win.scrollTop();

    $win.scroll($.throttle(100, function() {
        var scrollTop = $win.scrollTop();

        $('.leftNav, .withLeftNav > .main, .contentForm-aside').each(function() {
            var $element = $(this),
                    elementTop = $element.offset().top,
                    initialElementTop = $element.data('initialElementTop'),
                    windowHeight,
                    elementHeight,
                    alignToTop;

            if (!initialElementTop) {
                initialElementTop = elementTop;
                $element.data('initialElementTop', initialElementTop);
                $element.css({
                    'position': 'relative',
                    'top': 0
                });
            }

            windowHeight = $win.height();
            elementHeight = $element.outerHeight();
            alignToTop = function() {
                $element.stop(true);
                $element.animate({
                    'top': Math.max(scrollTop, 0)
                }, 'fast');
            };

            // The element height is less than the window height,
            // so there's no need to account for the bottom alignment.
            if (initialElementTop + elementHeight < windowHeight) {
                alignToTop();

            // The user is scrolling down.
            } else {
                if (lastScrollTop < scrollTop) {
                    var windowBottom = scrollTop + windowHeight;
                    var elementBottom = elementTop + elementHeight;
                    if (windowBottom > elementBottom) {
                        $element.stop(true);
                        $element.animate({
                            'top': Math.min(windowBottom, $('body').height()) - elementHeight - initialElementTop
                        }, 'fast');
                    }

                // The user is scrolling up.
                } else if (lastScrollTop > scrollTop) {
                    if (elementTop > scrollTop + initialElementTop) {
                        alignToTop();
                    }
                }
            }
        });

        lastScrollTop = scrollTop;
    }));
})();

// Make sure that the label for the focused input is visible.
$doc.delegate(':input', 'focus', function() {
    var $parents = $(this).parentsUntil('form');

    $parents.addClass('state-focus');

    $win.bind('scroll.focus', $.run($.throttle(100, function() {
        var $label = $('.focusLabel'),
                labelText = '',
                index,
                $parent,
                parentPosition,
                parentLabel;

        for (index = $parents.length - 1; index >= 0; -- index) {
            $parent = $($parents[index]);

            if ($parent.offset().top > $win.scrollTop() + 100) {
                if (labelText) {
                    if ($label.length === 0) {
                        $label = $('<div/>', { 'class': 'focusLabel' });
                        $(doc.body).append($label);
                    }

                    parentPosition = $parent.position();
                    $label.text(labelText);
                    $label.show();
                    return;

                } else {
                    break;
                }
            }

            parentLabel = $parent.find('> .inputLabel').text();

            if (parentLabel) {
                if (labelText) {
                    labelText += ' \u2192 ';
                }
                labelText += parentLabel;
            }
        }

        $label.hide();
    })));
});

$doc.delegate(':input', 'blur', function() {
    var $label = $('.focusLabel');

    $label.hide();
    $(this).parents('.state-focus').removeClass('state-focus');
    $win.unbind('.state-focus');
});

// Handle file uploads from drag-and-drop.
(function() {
    var docEntered;

    // Show all drop zones when the user initiates drag-and-drop.
    $doc.bind('dragenter', function() {
        var $body,
                $cover;

        if (docEntered) {
            return;
        }

        docEntered = true;
        $body = $(doc.body);

        // Cover is required to detect mouse leaving the window.
        $cover = $('<div/>', {
            'class': 'uploadableCover',
            'css': {
                'left': 0,
                'height': '100%',
                'position': 'fixed',
                'top': 0,
                'width': '100%',
                'z-index': 1999999
            }
        });

        $cover.bind('dragenter dragover', function(event) {
            event.stopPropagation();
            event.preventDefault();
            return false;
        });

        $cover.bind('dragleave', function() {
            docEntered = false;
            $cover.remove();
            $('.uploadableDrop').remove();
            $('.uploadableFile').remove();
        });

        $cover.bind('drop', function(event) {
            event.preventDefault();
            $cover.trigger('dragleave');
            return false;
        });

        $body.append($cover);

        // Valid file drop zones.
        $('.inputContainer .action-upload, .uploadable .uploadableLink').each(function() {
            var $upload = $(this),
                    $container = $upload.closest('.inputContainer, .uploadable'),
                    overlayCss,
                    $dropZone,
                    $dropLink,
                    $fileInputContainer,
                    $fileInput;

            overlayCss = $.extend($container.offset(), {
                'height': $container.outerHeight(),
                'position': 'absolute',
                'width': $container.outerWidth()
            });

            $dropZone = $('<div/>', {
                'class': 'uploadableDrop',
                'css': overlayCss
            });

            $dropLink = $upload.clone();
            $dropLink.text("Drop Files Here");

            $fileInputContainer = $('<div/>', {
                'class': 'uploadableFile',
                'css': $.extend(overlayCss, {
                    'z-index': 2000000
                })
            });

            $fileInput = $('<input/>', {
                'type': 'file',
                'multiple': 'multiple'
            });

            // On file drop, replace the appropriate input.
            $fileInput.one('change', function() {
                var dropLinkOffset = $dropLink.offset(),
                        $frame,
                        replaceFileInput;

                $cover.hide();
                $dropLink.click();
                $fileInputContainer.hide();

                $frame = $('.frame[name="' + $dropLink.attr('target') + '"]');

                // Position the popup over the drop link.
                $frame.popup('source', $upload, {
                    'pageX': dropLinkOffset.left + $dropLink.outerWidth() / 2,
                    'pageY': dropLinkOffset.top + $dropLink.outerHeight()
                });

                // Closing the popup resets the drag-and-drop.
                $frame.popup('container').bind('close', function() {
                    $cover.trigger('dragleave');
                });

                replaceFileInput = function() {
                    var $frameFileInput = $frame.find(':file');

                    if ($frameFileInput.length !== 1) {
                        setTimeout(replaceFileInput, 20);

                    } else {
                        $.each([ 'class', 'id', 'name', 'style' ], function(index, name) {
                            $fileInput.attr(name, $frameFileInput.attr(name) || '');
                        });

                        $frameFileInput.after($fileInput);
                        $frameFileInput.remove();
                        $frameFileInput = $fileInput;
                        $frameFileInput.change();
                    }
                };

                replaceFileInput();
            });

            $dropZone.append($dropLink);
            $body.append($dropZone);
            $fileInputContainer.append($fileInput);
            $body.append($fileInputContainer);
        });
    });
})();

$doc.on('click', 'button[name="action-delete"], :submit[name="action-delete"]', function() {
    return confirm('Are you sure you want to permanently delete this item?');
});

$doc.on('click', 'button[name="action-trash"], :submit[name="action-trash"]', function() {
    return confirm('Are you sure you want to trash this item?');
});

$doc.ready(function() {
    $(doc.activeElement).focus();
});

$doc.ready(function() {
    $(this).trigger('create');

    // Add the name of the sub-selected item on the main nav.
    $('.toolNav .selected').each(function() {
        var $selected = $(this),
                $subList = $selected.find('> ul'),
                $subSelected = $subList.find('> .selected > a'),
                $selectedLink;

        if ($subSelected.length > 0) {
            $selectedLink = $selected.find('> a');
            $selectedLink.text($selectedLink.text() + ' \u2192 ' + $subSelected.text());
        }

        $subList.css('min-width', $selected.outerWidth());
    });

    // Don't allow main nav links to be clickable if they have any children.
    $('.toolNav li.isNested > a').click(function() {
        return false;
    });

    // Sync the search input in the tool header with the one in the popup.
    (function() {
        var previousValue;

        $('.toolSearch :text').bind('focus input', $.throttle(500, function(event) {
            var $headerInput = $(this),
                    $headerForm = $headerInput.closest('form'),
                    $searchFrame,
                    $searchInput,
                    headerInputValue = $headerInput.val();

            $headerInput.attr('autocomplete', 'off');
            $searchFrame = $('.frame[name="' + $headerForm.attr('target') + '"]');

            if ($searchFrame.length === 0 ||
                    (event.type === 'focus' &&
                    headerInputValue &&
                    $searchFrame.find('.searchResultList .message-warning').length > 0)) {
                $headerForm.submit();

            } else {
                $searchFrame.popup('open');
                $searchInput = $searchFrame.find('.searchFilters :input[name="q"]');

                if (headerInputValue !== $searchInput.val()) {
                    $searchInput.val(headerInputValue).trigger('input');
                }
            }
        }));
    }());

    // Update repeatable labels as the user edits the related sections.
    $('.contentForm .repeatableForm').delegate(':input, textarea', 'change input', $.throttle(1000, function() {
        var $container = $(this).closest('li'),
                inputs = '_=' + (+new Date()),
                id;

        $container.find(':input:not([disabled])').each(function() {
            var $input = $(this);
            inputs += '&' + encodeURIComponent($input.attr('name')) + '=' + encodeURIComponent($input.val());
        });

        if ($container.data('repeatableLabels-lastInputs') !== inputs) {
            $container.data('repeatableLabels-lastInputs', inputs);

            id = $container.find('> :hidden[name$=".id"]').val();
            inputs += '&id=' + id;
            inputs += '&typeId=' + $container.find('> :hidden[name$=".typeId"]').val();

            $.ajax({
                'data': inputs,
                'type': 'post',
                'url': CONTEXT_PATH + 'content/repeatableLabels.jsp',
                'complete': function(request) {
                    $container.find('> .inputLabel').text($container.attr('data-type') + ': ' + $.parseJSON(request.responseText)[id]);
                }
            });
        }
    }));

    // Publishing widget behaviors.
    $('.widget-publishing').each(function() {
        var $widget = $(this),
                $dateInput = $widget.find('.dateInput'),
                $publishButton = $widget.find('[name="action-publish"]'),
                oldPublishText = $publishButton.text(),
                oldDate = $dateInput.val();

        // Change the publish button label if scheduling.
        if ($dateInput.length === 0) {
            $publishButton.addClass('schedule');
            $publishButton.text('Schedule');

        } else {
            $dateInput.change($.run(function() {
                if ($dateInput.val()) {
                    $publishButton.addClass('schedule');
                    $publishButton.text(oldDate ? 'Reschedule' : 'Schedule');

                } else {
                    $publishButton.removeClass('schedule');
                    $publishButton.text(oldPublishText);
                }
            }));
        }

        // Move the widget to the top if within aside section.
        $widget.closest('.contentForm-aside').each(function() {
            var $aside = $(this),
                    asideTop = $aside.offset().top;

            $win.resize($.throttle(100, $.run(function() {
                $widget.css({
                    'left': $aside.offset().left,
                    'position': 'fixed',
                    'top': asideTop,
                    'width': $widget.width()
                });

                // Push other areas down.
                $aside.css('padding-top', $widget.outerHeight(true));
            })));
        });
    });
});

}(jQuery, window));
