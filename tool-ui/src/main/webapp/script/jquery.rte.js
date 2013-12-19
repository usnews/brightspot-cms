/** Rich text editor based on wysihtml5. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document,
        $doc = $(doc),
        targetIndex = 0;

function getContentEnhancementTarget() {
    ++ targetIndex;
    return 'contentEnhancement-' + targetIndex;
}

$.each(CSS_CLASS_GROUPS, function() {
    var command = 'cms-' + this.internalName;
    var prefix = command + '-';
    var regex = new RegExp(prefix + '[0-9a-z\-]+', 'g');

    wysihtml5.commands[command] = {
        'exec': function(composer, command, optionsString) {
            var options = optionsString ? $.parseJSON(optionsString) : { };
            var tag = options.tag || 'span';
            var format = tag === 'span' ? 'formatInline' : 'formatBlock';
            return wysihtml5.commands[format].exec(composer, command, tag, prefix + options.internalName, regex);
        },

        'state': function(composer, command, optionsString) {
            var options = optionsString ? $.parseJSON(optionsString) : { };
            var tag = options.tag || 'span';
            var format = tag === 'span' ? 'formatInline' : 'formatBlock';
            return wysihtml5.commands[format].state(composer, command, tag , prefix + options.internalName, regex);
        }
    };
});

var $createToolbarGroup = function(label) {
    var $group = $('<span/>', { 'class': 'rte-group', 'data-group-name': label });
    var $label = $('<span/>', { 'class': 'rte-group-label', 'text': label });
    var $buttons = $('<span/>', { 'class': 'rte-group-buttons' });
    $group.append($label);
    $group.append($buttons);
    return $group;
};

var $createToolbarCommand = function(label, command) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + command,
        'data-wysihtml5-command': command,
        'text': label
    });
};

var createToolbar = function(rte, inline, firstDraft, finalDraft) {
    var $container = $('<div/>', { 'class': 'rte-toolbar-container' });

    var $toolbar = $('<div/>', { 'class': 'rte-toolbar' });
    $container.append($toolbar);

    var $font = $createToolbarGroup('Font');
    $toolbar.append($font);
    $font = $font.find('.rte-group-buttons');

    $font.append($createToolbarCommand('Bold', 'bold'));
    $font.append($createToolbarCommand('Italic', 'italic'));
    $font.append($createToolbarCommand('Underline', 'underline'));
    $font.append($createToolbarCommand('Strike', 'strike'));
    $font.append($createToolbarCommand('Superscript', 'superscript'));
    $font.append($createToolbarCommand('Subscript', 'subscript'));

    $.each(CSS_CLASS_GROUPS, function() {
        var $group = $createToolbarGroup(this.displayName);
        var command = 'cms-' + this.internalName;

        if (this.dropDown) {
            $group.addClass('rte-group-dropDown');
        }

        $group.addClass('rte-group-cssClass');
        $group.addClass('rte-group-cssClass-' + this.internalName);
        $toolbar.append($group);
        $group = $group.find('.rte-group-buttons');

        $.each(this.cssClasses, function() {
            var $cssClass = $createToolbarCommand(this.displayName, command);
            $cssClass.attr('data-wysihtml5-command-value', JSON.stringify(this));
            $group.append($cssClass);
        });
    });

    if (!inline) {
        var $alignment = $createToolbarGroup('Alignment') ;
        $toolbar.append($alignment);
        $alignment = $alignment.find('.rte-group-buttons');

        $alignment.append($createToolbarCommand('Justify Left', 'textAlign').attr('data-wysihtml5-command-value', 'left'));
        $alignment.append($createToolbarCommand('Justify Center', 'textAlign').attr('data-wysihtml5-command-value', 'center'));
        $alignment.append($createToolbarCommand('Justify Right', 'textAlign').attr('data-wysihtml5-command-value', 'right'));

        var $list = $createToolbarGroup('List');
        $toolbar.append($list);
        $list = $list.find('.rte-group-buttons');

        $list.append($createToolbarCommand('Unordered List', 'insertUnorderedList'));
        $list.append($createToolbarCommand('Ordered List', 'insertOrderedList'));
    }

    var $enhancement = $createToolbarGroup('Enhancement');
    $toolbar.append($enhancement);
    $enhancement = $enhancement.find('.rte-group-buttons');

    $enhancement.append($('<span/>', {
        'class': 'rte-button rte-button-link',
        'text': 'Link'
    }));

    if (!inline) {
        $enhancement.append($createToolbarCommand('Enhancement', 'insertEnhancement'));
        $enhancement.append($createToolbarCommand('Marker', 'insertMarker'));
    }

    if (win.cmsRteImportOptions && win.cmsRteImportOptions.length > 0) {
        var $importGroup = $createToolbarGroup('Import');

        $importGroup.addClass('rte-group-dropDown');
        $toolbar.append($importGroup);

        $importGroup = $importGroup.find('.rte-group-buttons');

        $.each(win.cmsRteImportOptions, function(i, importOptions) {
            $importGroup.append($('<span/>', {
                'class': 'rte-button rte-button-import',
                'text': importOptions.name,
                'click': function() {
                    var $button = $(this);

                    google.load('picker', '1', {
                        'callback': function() {
                            new google.picker.PickerBuilder().
                                    enableFeature(google.picker.Feature.NAV_HIDDEN).
                                    setAppId(importOptions.clientId).
                                    setOAuthToken(importOptions.accessToken).
                                    addView(google.picker.ViewId.DOCUMENTS).
                                    setCallback(function(data) {
                                        if (data[google.picker.Response.ACTION] === google.picker.Action.PICKED) {
                                            $.ajax({
                                                'method': 'get',
                                                'url': '/social/googleDriveFile',
                                                'data': { 'id': data[google.picker.Response.DOCUMENTS][0][google.picker.Document.ID] },
                                                'cache': false,
                                                'success': function(data) {
                                                    rte.composer.setValue(data, true);
                                                    rte.composer.parent.updateOverlay();
                                                }
                                            });
                                        }
                                    }).
                                    build().
                                    setVisible(true);
                        }
                    });
                }
            }));
        });
    }

    var $changes = $createToolbarGroup('Changes');
    $toolbar.append($changes);
    $changes = $changes.find('.rte-group-buttons');

    $changes.append($createToolbarCommand('Track', 'changesTrack'));
    $changes.append($createToolbarCommand('Accept', 'changesAccept'));
    $changes.append($createToolbarCommand('Reject', 'changesReject'));

    var $comment = $createToolbarGroup('Comment');
    $toolbar.append($comment);
    $comment = $comment.find('.rte-group-buttons');

    $comment.append($createToolbarCommand('Comment', 'commentAdd'));
    $comment.append($createToolbarCommand('Collapse', 'commentCollapse'));
    $comment.append($createToolbarCommand('Remove', 'commentRemove'));

    var $misc = $createToolbarGroup('Misc');
    $toolbar.append($misc);
    $misc = $misc.find('.rte-group-buttons');

    $misc.append($('<span/>', {
        'class': 'rte-button rte-button-html',
        'data-wysihtml5-action': 'change_view',
        'text': 'HTML'
    }));

    $misc.append($createToolbarCommand('Fullscreen', 'fullscreen'));

    return $container[0];
};

var $createEnhancementAction = function(label, action) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + action,
        'data-action': action,
        'text': label
    });
};

var createEnhancement = function(rte) {
    var $enhancement = $('<div/>', { 'class': 'rte-enhancement' });

    var $toolbar = $('<div/>', { 'class': 'rte-toolbar' });
    $enhancement.append($toolbar);

    var $position = $createToolbarGroup('Position');
    $toolbar.append($position);

    $position.append($createEnhancementAction('Move Left', 'moveLeft'));
    $position.append($createEnhancementAction('Move Up', 'moveUp'));
    $position.append($createEnhancementAction('Move Center', 'moveCenter'));
    $position.append($createEnhancementAction('Move Down', 'moveDown'));
    $position.append($createEnhancementAction('Move Right', 'moveRight'));

    var $imageSize = $createToolbarGroup('Image Size');
    $imageSize.addClass('rte-group-dropDown');
    $toolbar.append($imageSize);
    $imageSize = $imageSize.find('.rte-group-buttons');

    var sizes = $(rte.container).closest('.inputContainer').attr('data-standard-image-sizes');

    if (sizes) {
        sizes = ' ' + sizes + ' ';
    }

    $imageSize.append($createEnhancementAction('None', 'imageSize-'));

    $.each(STANDARD_IMAGE_SIZES, function() {
        if (sizes.indexOf(' ' + this.internalName + ' ') > -1) {
            $imageSize.append($createEnhancementAction(this.displayName, 'imageSize-' + this.internalName));
        }
    });

    var $misc = $createToolbarGroup('Misc');
    $toolbar.append($misc);

    $misc.append($('<span/>', {
        'class': 'rte-button rte-button-editEnhancement',
        'html': $('<a/>', {
            'href': CONTEXT_PATH + '/content/enhancement.jsp?id=',
            'target': getContentEnhancementTarget(),
            'text': 'Edit'
        })
    }));

    $misc.append($createEnhancementAction('Remove', 'remove'));
    $misc.append($createEnhancementAction('Restore', 'restore'));
    $misc.append($createEnhancementAction('Remove Completely', 'removeCompletely'));

    $enhancement.append($('<div/>', { 'class': 'rte-enhancement-label' }));

    return $enhancement[0];
};

var createMarker = function() {
    var $marker = $('<div/>', { 'class': 'rte-enhancement rte-marker' });

    var $toolbar = $('<div/>', { 'class': 'rte-toolbar' });
    $marker.append($toolbar);

    var $position = $createToolbarGroup('Position');
    $toolbar.append($position);

    $position.append($createEnhancementAction('Move Up', 'moveUp'));
    $position.append($createEnhancementAction('Move Down', 'moveDown'));

    var $misc = $createToolbarGroup('Misc');
    $toolbar.append($misc);

    $misc.append($('<span/>', {
        'class': 'rte-button rte-button-selectMarker',
        'html': $('<a/>', {
            'href': CONTEXT_PATH + '/content/marker.jsp',
            'target': getContentEnhancementTarget(),
            'text': 'Select'
        })
    }));

    $misc.append($createEnhancementAction('Remove', 'remove'));
    $misc.append($createEnhancementAction('Restore', 'restore'));
    $misc.append($createEnhancementAction('Really Remove', 'removeCompletely'));

    $marker.append($('<div/>', { 'class': 'rte-enhancement-label' }));

    return $marker[0];
};

// Wrap wysihtml5 to add functionality.
var rtes = [ ],
        getEnhancementId,
        keepToolbarInView;

(function() {
    var enhancementIndex = 0;

    getEnhancementId = function(placeholder) {
        var enhancementId = $.data(placeholder, 'rte-enhancement-id');

        if (!enhancementId) {
            ++ enhancementIndex;
            enhancementId = 'rte-enhancement-' + enhancementIndex;
            $.data(placeholder, 'rte-enhancement-id', enhancementId);
        }

        return enhancementId;
    };
})();

var Rte = wysihtml5.Editor.extend({

    'constructor': function(originalTextarea, config) {
        var rte = this,
                firstDraft = $(originalTextarea).attr('data-first-draft') === 'true',
                finalDraft = $(originalTextarea).attr('data-final-draft') === 'true';

        // Create container.
        var container = this.container = doc.createElement('div');
        container.className = 'rte-container';
        originalTextarea.parentNode.insertBefore(container, originalTextarea);

        // Create overlay.
        var overlay = this.overlay = doc.createElement('div');
        overlay.className = 'rte-overlay';
        overlay.style.position = 'relative';
        overlay.style.left = '0px';
        overlay.style.top = '0px';
        container.appendChild(overlay);

        // Changes the focus method to retain the scrolling position.
        (function() {
            var originalFocus = rte.focus;

            rte.focus = function() {
                var scrollTop = $win.scrollTop();

                originalFocus.apply(rte, arguments);
                $win.scrollTop(scrollTop);
            };
        })();

        $(overlay).on('click', function() {
            rte.focus();
        });

        // Create toolbar?
        if (typeof config.toolbar === 'function') {
            config.toolbar = config.toolbar(rte, config.inline, firstDraft, finalDraft);
            container.appendChild(config.toolbar);
        }

        // Handle toolbar action clicks.
        $(overlay).delegate('[data-action]', 'click', function() {
            var $button = $(this);
            var $enhancement = $button.closest('.rte-enhancement');
            var $placeholder = $enhancement.data('$rte-placeholder');
            var $editButtonAnchor = $enhancement.find('.rte-button-editEnhancement a');
            var action = $button.attr('data-action');
            var refData, refDataString, href;

            if (action.indexOf('imageSize-') === 0) {
                var imageSize = action.substring(10);

                if (imageSize) {
                    $enhancement.attr('data-image-size', $button.text());
                    $placeholder.attr('data-image-size', imageSize);

                } else {
                    $enhancement.removeAttr('data-image-size');
                    $placeholder.removeAttr('data-image-size');
                }

            } else if (action == 'remove') {
                $enhancement.addClass('state-removing');
                $placeholder.addClass('state-removing');

            } else if (action == 'restore') {
                $enhancement.removeClass('state-removing');
                $placeholder.removeClass('state-removing');

            } else if (action == 'removeCompletely') {
                $enhancement.remove();
                $placeholder.remove();

            } else if (action === 'moveCenter' || action === 'moveLeft' || action === 'moveRight') {

                // update css attribute
                $placeholder.removeAttr('data-alignment');
                if (action === 'moveCenter') {
                    $placeholder.removeAttr('data-alignment');

                } else if (action === 'moveLeft') {
                    $placeholder.attr('data-alignment', 'left');

                } else if (action === 'moveRight') {
                    $placeholder.attr('data-alignment', 'right');
                }

                // update main ref attribute
                refData = $.parseJSON($placeholder.attr('data-reference') || '{}');
                if (action === 'moveCenter') {
                    delete refData['alignment'];

                } else if (action === 'moveLeft') {
                    refData['alignment'] = 'left';

                } else if (action === 'moveRight') {
                    refData['alignment'] = 'right';
                }

                refDataString = JSON.stringify(refData);
                $placeholder.attr('data-reference', refDataString);

                // update edit button link URL
                if ($editButtonAnchor) {
                    href = $editButtonAnchor.attr('href');

                    href = (href+'&').replace(/([?&])reference=[^&]*[&]/, '$1');
                    href += 'reference=' + (refDataString || '');

                    $editButtonAnchor.attr('href', href);
                }

            } else {
                var oldTop = $placeholder.offset().top;
                var $parent, $prev, $next;

                if (action === 'moveDown') {
                    $placeholder.closest('body').find('br + br, h1, h2, h3, h4, h5, h6, p').each(function() {
                        if ($placeholder[0].compareDocumentPosition(this) & Node.DOCUMENT_POSITION_FOLLOWING) {
                            $(this).after($placeholder);
                            return false;
                        }
                    });

                    $win.scrollTop($win.scrollTop() + $placeholder.offset().top - oldTop);

                } else if (action === 'moveUp') {
                    var precedings = [ ],
                            precedingsLength;

                    $placeholder.closest('body').find('br + br, h1, h2, h3, h4, h5, h6, p').each(function() {
                        if ($placeholder[0].compareDocumentPosition(this) & Node.DOCUMENT_POSITION_PRECEDING) {
                            precedings.push(this);
                        }
                    });

                    precedingsLength = precedings.length;

                    if (precedingsLength >= 2) {
                        $(precedings[precedingsLength - 2]).after($placeholder);

                    } else {
                        $placeholder.closest('body').prepend($placeholder);
                    }

                    $win.scrollTop($win.scrollTop() + $placeholder.offset().top - oldTop);
                }
            }

            rte.updateOverlay();

            return false;
        });

        // Create dialogs.
        var $dialogs = $('<div/>', {
            'class': 'rte-dialogs',
            'css': {
                'left': 0,
                'position': 'relative',
                'top': 0
            }
        });

        var $linkDialog = $(
                '<div>' +
                    '<h2>Link</h2>' +
                    '<div class="rte-dialogLine">' +
                        '<input type="text" class="rte-dialogLinkHref">' +
                        '<a class="rte-dialogLinkContent" target="linkById" href="' + CONTEXT_PATH + '/content/linkById.jsp?p=true">Content</a>' +
                    '</div>' +
                    '<div class="rte-dialogLine">' +
                        '<select class="rte-dialogLinkTarget">' +
                            '<option value="">Same Window</option>' +
                            '<option value="_blank">New Window</option>' +
                        '</select>' +
                        ' <select class="rte-dialogLinkRel">' +
                            '<option value="">Relation</option>' +
                            '<option value="nofollow">nofollow</option>' +
                        '</select>' +
                    '</div>' +
                    '<a class="rte-dialogLinkSave">Save</a>' +
                    ' <a class="rte-dialogLinkOpen" target="_blank">Open</a>' +
                    ' <a class="rte-dialogLinkUnlink">Unlink</a>' +
                '</div>');

        var $lastAnchor = $();

        $linkDialog.on('click', '.rte-dialogLinkSave', function() {
            var target = $linkDialog.find('.rte-dialogLinkTarget').val(),
                    rel = $linkDialog.find('.rte-dialogLinkRel').val();

            $lastAnchor.attr('href', $linkDialog.find('.rte-dialogLinkHref').val() || '');

            if (target) {
                $lastAnchor.attr('target', target);

            } else {
                $lastAnchor.removeAttr('target');
            }

            if (rel) {
                $lastAnchor.attr('rel', rel);

            } else {
                $lastAnchor.removeAttr('rel');
            }

            $linkDialog.popup('close');
        });

        $linkDialog.on('keydown', '.rte-dialogLinkHref', function(event) {
            if (event.which === 13) {
                $linkDialog.find('.rte-dialogLinkSave').click();
                return false;

            } else {
                return true;
            }
        });

        $linkDialog.on('input', '.rte-dialogLinkHref', function(event) {
            $linkDialog.find('.rte-dialogLinkOpen').attr('href', $(event.target).val());
        });

        $linkDialog.on('click', '.rte-dialogLinkUnlink', function() {
            $lastAnchor.after($lastAnchor.html());
            $lastAnchor.remove();
            $linkDialog.popup('close');
        });

        $(doc.body).append($linkDialog);
        $linkDialog.popup();
        $linkDialog.popup('close');

        $linkDialog.popup('container').bind('close', function() {
            if (!$lastAnchor.attr('href')) {
                $lastAnchor.after($lastAnchor.html());
                $lastAnchor.remove();
            }
        });

        var openLinkDialog = function($anchor) {
            var composerOffset = $(rte.composer.iframe).offset(),
                    $href = $linkDialog.find('.rte-dialogLinkHref'),
                    $popup,
                    popupWidth,
                    anchorOffset = $anchor.offset(),
                    left,
                    leftDelta;

            $lastAnchor = $anchor;

            $linkDialog.popup('open');
            $href.val($anchor.attr('href') || 'http://');
            $linkDialog.find('.rte-dialogLinkTarget').val($anchor.attr('target') || '');
            $linkDialog.find('.rte-dialogLinkRel').val($anchor.attr('rel') || '');
            $linkDialog.find('.rte-dialogLinkOpen').attr('href', $href.val());
            $href.focus();

            $popup = $linkDialog.popup('container');
            popupWidth = $popup.outerWidth();
            left = anchorOffset.left + ($anchor.outerWidth() - popupWidth) / 2;

            if (left < 35) {
                left = 35;

            } else {
                leftDelta = left + popupWidth - $(doc).width() + 35;

                if (leftDelta > 0) {
                    left -= leftDelta;
                }
            }

            $popup.css({
                'left': left,
                'margin-left': 0,
                'position': 'absolute',
                'top': composerOffset.top + $anchor.offset().top + $anchor.outerHeight()
            });
        };

        (function() {
            var tempIndex = 0;

            $(config.toolbar).find('.rte-button-link').click(function() {
                var tempClass,
                        $anchor;

                ++ tempIndex;
                tempClass = 'rte-link-temp-' + tempIndex;

                wysihtml5.commands.createLink.exec(rte.composer, 'createLink', { 'class': tempClass });

                $anchor = $('a.' + tempClass, rte.composer.element);

                $anchor.removeClass(tempClass);
                openLinkDialog($anchor);
            });
        })();

        // Initialize wysihtml5.
        container.appendChild(originalTextarea);
        originalTextarea.className += ' rte-textarea';

        rtes[rtes.length] = this;
        this.base(originalTextarea, config);

        var getSelectedElement = function() {
            var range = rte.composer.selection.getRange(),
                    selected = rte.composer.selection.getSelectedNode(),
                    $next;

            if (range.collapsed &&
                    selected.nodeType == 3 &&
                    selected.length == range.endOffset) {
                $next = $(selected.nextSibling);

                if ($next.is('code, del, ins')) {
                    return $next;
                }
            }

            while (!selected.tagName) {
                selected = selected.parentNode;
            }

            return $(selected);
        };

        this.observe('show:dialog', function(options) {
            var $selected = getSelectedElement(),
                    selectedOffset = $selected.offset(),
                    $dialog = $(options.dialogContainer);

            $dialog.css({
                'left': selectedOffset.left,
                'position': 'absolute',
                'top': selectedOffset.top + $selected.outerHeight() + 10
            });
        });

        this.observe('load', function() {

            // Make sure placeholder BUTTONs are replaced with enhancement SPANs.
            var convertNodes = function(parent, oldTagName, newTagName, callback) {
                var childNodes = parent.childNodes;

                if (parent.childNodes) {
                    var childNodesLength = childNodes.length;

                    for (var i = 0; i < childNodesLength; ++ i) {
                        var node = childNodes[i];

                        if (node &&
                                node.tagName &&
                                node.tagName === oldTagName &&
                                wysihtml5.dom.hasClass(node, 'enhancement')) {
                            var newNode = parent.ownerDocument.createElement(newTagName);
                            var nodeAttributes = node.attributes;
                            var nodeAttributesLength = nodeAttributes.length;

                            for (var j = 0; j < nodeAttributesLength; ++ j) {
                                var attribute = nodeAttributes[j];
                                newNode.setAttribute(attribute.name, attribute.value);
                            }

                            if (callback) {
                                callback(newNode);
                            }

                            parent.insertBefore(newNode, node);
                            parent.removeChild(node);

                        } else {
                            convertNodes(node, oldTagName, newTagName, callback);
                        }
                    }
                }
            };

            var textarea = this.textarea;
            var lastTextareaValue;

            if (!$(textarea.element).val()) {
                $(textarea.element).val('<br>');
            }

            textarea._originalGetValue = textarea.getValue;
            textarea.getValue = function(parse) {
                var value = this._originalGetValue(parse),
                        dom;

                if (lastTextareaValue === value) {
                    return value;

                } else {
                    lastTextareaValue = value;
                    dom = wysihtml5.dom.getAsDom(value, this.element.ownerDocument);
                    convertNodes(dom, 'SPAN', 'BUTTON');
                    return dom.innerHTML;
                }
            };

            var composer = this.composer;
            var lastComposerValue;

            composer._originalGetValue = composer.getValue;
            composer.getValue = function(parse) {
                var value = this._originalGetValue(parse);

                if (lastComposerValue === value) {
                    return value;

                } else {
                    lastComposerValue = value;
                    dom = wysihtml5.dom.getAsDom(value, this.element.ownerDocument);
                    convertNodes(dom, 'BUTTON', 'SPAN', function(node) {
                        node.innerHTML = 'Enhancement';
                    });
                    return dom.innerHTML;
                }
            };

            composer.setValue(textarea.getValue());

            // Some style clean-ups.
            composer.iframe.style.overflow = 'hidden';
            composer.iframe.contentDocument.body.style.overflow = 'hidden';
            composer.iframe.contentDocument.body.className += ' rte-loaded';
            textarea.element.className += ' rte-source';

            this.on('focus', function() {
                $(textarea.element).parentsUntil('form').addClass('state-focus');

                for (var i = 0, length = rtes.length; i < length; ++ i) {
                    var rte = rtes[i];

                    $(rte.container).css('padding-top', 0);
                    $(rte.config.toolbar).removeClass('rte-toolbar-fixed');
                    $(rte.config.toolbar).attr('style', rte._toolbarOldStyle);
                    $(rte.overlay).css('top', 0);
                    rte._toolbarOldStyle = null;
                }

                $(this.overlay).css('top', $(this.config.toolbar).outerHeight());
                keepToolbarInView();
            });

            // Hack to make sure that the proper focus fires when clicking
            // on an 'empty' region.
            $(composer.iframe.contentWindow).on('focus', function() {
                rte.focus();
            });

            this.on('blur', function() {
                $(textarea.element).parentsUntil('form').removeClass('state-focus');
                $(textarea.element).trigger('change');
            });

            $(composer.element).on('click', 'a', function(event) {
                openLinkDialog($(event.target).closest('a'));
            });

            // Keyboard navigation into comments should skip over them.
            (function() {
                var lastRange,
                        inComment;

                $(composer.element).keydown(function() {
                    lastRange = composer.selection.getRange().cloneRange();
                    inComment = $(lastRange.commonAncestorContainer).closest('.rte-comment').length > 0;
                });

                $(composer.element).keyup(function() {
                    var range,
                            $comment;

                    if (!inComment) {
                        range = composer.selection.getRange();
                        $comment = $(range.commonAncestorContainer).closest('.rte-comment');

                        if ($comment.length > 0) {
                            composer.selection[lastRange && lastRange.compareBoundaryPoints(range.START_TO_START, range) > 0 ? 'setBefore' : 'setAfter']($comment[0]);
                        }
                    }
                });
            })();

            // Track changes.
            (function() {
                var down = false,
                        downRange,
                        downLength = -1,
                        downContents,
                        tempIndex = 0;

                function wrapCurrentSelectionRange(tag) {
                    var tempClass,
                            $element;

                    if (!wysihtml5.commands.formatInline.state(composer, null, tag)) {
                        ++ tempIndex;
                        tempClass = 'rte-wrap-temp-' + tempIndex;

                        wysihtml5.commands.formatInline.exec(composer, null, tag, tempClass, /foobar/g);

                        $element = $(tag + '.' + tempClass, composer.element);

                        if ($element.length > 0) {
                            $element.removeClass(tempClass);
                        }
                    }
                }

                function cleanUp() {
                    var $composerElement = $(composer.element),
                            merged;

                    $composerElement.find('ins del').remove();

                    // Move <ins> elements out of <del> elements.
                    $composerElement.find('del ins').each(function() {
                        var $ins = $(this),
                                $del = $ins.closest('del');

                        $del.after($ins);
                    });

                    // Flatten <del> and <ins> elements.
                    $composerElement.find('del:has(del), ins:has(ins)').each(function() {
                        var $element = $(this);

                        $element.text($element.text());
                    });

                    // Remove empty <del> and <ins> elements.
                    $composerElement.find('del, ins').each(function() {
                        var $current = $(this),
                                firstLetter = $current.text()[0];

                        if (!firstLetter || firstLetter.charCodeAt(0) === 65279) {
                            $current.remove();
                        }
                    });

                    // Merge consecutive <del> and <ins> elements.
                    do {
                        merged = false;

                        $composerElement.find('del, ins').each(function() {
                            var $current = $(this),
                                    currentTagName,
                                    next,
                                    $next;

                            if ($.contains(composer.element, $current[0])) {
                                currentTagName = this.tagName;
                                next = this;

                                while (!!(next = next.nextSibling) &&
                                        next.nodeType === 1 &&
                                        next.tagName === currentTagName) {
                                    merged = true;
                                    $next = $(next);

                                    $current.append($next.html());
                                    $next.remove();
                                }
                            }
                        });
                    } while (merged);
                }

                $(composer.element).bind('keydown', function(event) {
                    var which,
                            selection;

                    if (event.metaKey ||
                            !$(composer.element).hasClass('rte-changesTracking') ||
                            $(composer.selection.getRange().commonAncestorContainer).closest('.rte-comment').length > 0) {
                        return true;
                    }

                    which = event.which;
                    selection = composer.selection;

                    function doDelete(direction) {
                        var rangySelection = selection.getSelection();

                        if (selection.getRange().collapsed) {
                            rangySelection.nativeSelection.modify('extend', direction, 'character');
                        }

                        wrapCurrentSelectionRange('del');

                        if (direction === 'forward') {
                            rangySelection.collapseToEnd();

                        } else {
                            rangySelection.collapseToStart();
                        }

                        cleanUp();
                    }

                    // BACKSPACE.
                    if (which === 8) {
                        doDelete('backward');
                        return false;

                    // DELETE.
                    } else if (which === 46) {
                        doDelete('forward');
                        return false;

                    // Save state for later on any other key.
                    } else if (!down) {
                        down = true;
                        downRange = selection.getRange().cloneRange();
                        downLength = $(composer.element).text().length;
                        downContents = downRange.collapsed ? null : downRange.cloneContents();
                    }

                    return true;
                });

                $(composer.element).bind('keyup', function(event) {
                    var selection,
                            range,
                            $del;

                    if (!down ||
                            event.metaKey ||
                            !$(composer.element).hasClass('rte-changesTracking') ||
                            $(composer.selection.getRange().commonAncestorContainer).closest('.rte-comment').length > 0) {
                        return;
                    }

                    if ($(composer.element).text().length !== downLength) {
                        selection = composer.selection;
                        range = selection.getRange();

                        // Previously selected text was somehow removed so clone
                        // and wrap it in <del>.
                        if (downContents && range.collapsed) {
                            $del = $('<del/>', composer.iframe.contentDocument);

                            $del.append(downContents);
                            range.insertNode($del[0]);
                        }

                        // Wrap newly inserted text in <ins>.
                        selection.executeAndRestore(function() {
                            range.setStart(downRange.startContainer, downRange.startOffset);
                            selection.setSelection(range);
                            wrapCurrentSelectionRange('ins');
                        });
                    }

                    down = false;
                    downRange = null;
                    downLength = -1;
                    downContents = null;

                    cleanUp();
                });
            })();

            setInterval(function() {
                rte.updateOverlay();
            }, 100);
        });
    },

    // Updates the overlay to show enhancements above the underlying
    // placeholders.
    'updateOverlay': function() {
        var rte = this,
                textarea = rte.textarea,
                $overlay = $(rte.overlay),
                composer = rte.composer,
                composerIframe = composer.iframe,
                composerWindow = composerIframe.contentWindow,
                $composerBody;

        if (!composerWindow) {
            return;
        }

        $composerBody = $(composerWindow.document.body);

        // Hide if viewing source HTML.
        $overlay.toggle(rte.currentView !== textarea);

        // Automatically size the iframe height to match the content.
        $(composerIframe).css('min-height', Math.max(28, $composerBody.outerHeight(true)));
        $(composerWindow).scrollTop(0);

        // Overlay enhancements on top of the placeholders in the composer.
        $overlay.children().each(function() {
            $.data(this, 'rte-visited', false);
        });

        $composerBody.find('button.enhancement').each(function() {
            var $placeholder = $(this),
                    isMarker = $placeholder.hasClass('marker'),
                    enhancementId = getEnhancementId(this),
                    $enhancement = $('#' + enhancementId),
                    $editTrigger,
                    placeholderOffset = $placeholder.offset(),
                    $enhancementLabel,
                    newLabel,
                    $oldImage,
                    oldPreview,
                    newPreview,
                    refData;

            // Create the enhancement if it doesn't exist already.
            if ($enhancement.length === 0) {
                $enhancement = $(rte.config[isMarker ? 'marker' : 'enhancement'](rte));

                $enhancement.attr('id', enhancementId);
                $enhancement.css('position', 'absolute');
                $.data($enhancement[0], '$rte-placeholder', $placeholder);
                $overlay.append($enhancement);

                $enhancement.find('.rte-button-editEnhancement a').each(function() {
                    var $anchor = $(this),
                            href = $anchor.attr('href'),
                            id = $placeholder.attr('data-id'),
                            reference = $placeholder.attr('data-reference');

                    href = (href+'&').replace(/([?&])id=[^&]*[&]/, '$1');
                    href += 'id=' + (id || '');

                    href = (href+'&').replace(/([?&])reference=[^&]*[&]/, '$1');
                    href += 'reference=' + (reference || '');

                    $anchor.attr('href', href);

                    if (!id) {
                        $editTrigger = $anchor;
                    }
                });
            }

            $.data($enhancement[0], 'rte-visited', true);

            // Position the enhancement to cover the placeholder.
            $placeholder.css('padding-top', $enhancement.find('> .rte-toolbar').outerHeight() - 42);

            $enhancement.css({
                'height': $placeholder.outerHeight(),
                'left': placeholderOffset.left,
                'top': placeholderOffset.top,
                'width': $placeholder.width()
            });

            // Copy the enhancement label.
            $enhancementLabel = $enhancement.find('.rte-enhancement-label');

            refData = $.parseJSON($placeholder.attr('data-reference') || '{}');
            newLabel = refData.label;
            newPreview = refData.preview;

            if (newPreview) {
                if ($enhancementLabel.find('figure img').attr('src') !== newPreview) {
                    $enhancementLabel.html($('<figure/>', {
                        'html': [
                            $('<img/>', {
                                'src': newPreview
                            }),
                            $('<figcaption/>')
                        ]
                    }));
                }

                $enhancementLabel.find('figure img').attr('alt', newLabel);
                $enhancementLabel.find('figure figcaption').text(newLabel);

            } else {
                if (!newLabel) {
                    newLabel = 'Empty ' + (isMarker ? 'Marker' : 'Enhancement');
                }

                $enhancementLabel.text(newLabel);
            }

            if ($editTrigger) {
                $editTrigger.click();
            }
        });

        // Remove orphaned enhancements.
        $overlay.children().each(function() {
            if (!$.data(this, 'rte-visited')) {
                $(this).remove();
            }
        });
    }
});

// Add support for strike command.
wysihtml5.commands.strike = {

    'exec': function(composer, command) {
        return wysihtml5.commands.formatInline.exec(composer, command, 'strike');
    },

    'state': function(composer, command) {
        return wysihtml5.commands.formatInline.state(composer, command, 'strike');
    }
};

var textAlignRegex = /cms-textAlign-[0-9a-z\-]+/g;
wysihtml5.commands.textAlign = {

    'exec': function(composer, command, alignment) {
        return wysihtml5.commands.formatBlock.exec(composer, command, null, 'cms-textAlign-' + alignment, textAlignRegex);
    },

    'state': function(composer, command, alignment) {
        return wysihtml5.commands.formatBlock.state(composer, command, null, 'cms-textAlign-' + alignment, textAlignRegex);
    }
};

// Remove support for insertImage so that it can't be used accidentantly,
// since insertEnhancement supercedes its functionality.
delete wysihtml5.commands.insertImage;

var insertButton = function(composer, button) {
    var $selected = $(composer.selection.getSelectedNode()),
            precedings,
            precedingsLength;

    if ($selected.is('body')) {
        $($selected[0].childNodes[composer.selection.getRange().startOffset]).after(button);

    } else {
        precedings = [ ];

        $selected.closest('body').find('br + br, h1, h2, h3, h4, h5, h6, p').each(function() {
            if ($selected[0].compareDocumentPosition(this) & Node.DOCUMENT_POSITION_PRECEDING) {
                precedings.push(this);
            }
        });

        precedingsLength = precedings.length;

        if (precedingsLength >= 1) {
            $(precedings[precedingsLength - 1]).after(button);

        } else {
            $selected.closest('body').prepend(button);
        }
    }
};

// Add support for adding an enhancement.
wysihtml5.commands.insertEnhancement = {

    'exec': function(composer, command, value) {
        var doc = composer.doc;
        var button = doc.createElement('BUTTON');

        if (value) {
            for (var i in value) {
                button.setAttribute(i, value[i]);
            }
        }

        button.setAttribute('class', 'enhancement');
        insertButton(composer, button);
    },

    'state': function(composer) {
        return false;
    }
};

// Add support for adding a marker.
wysihtml5.commands.insertMarker = {

    'exec': function(composer, command, value) {
        var doc = composer.doc;
        var button = doc.createElement('BUTTON');

        if (value) {
            for (var i in value) {
                button.setAttribute(i, value[i]);
            }
        }

        button.setAttribute('class', 'enhancement marker');
        insertButton(composer, button);
    },

    'state': function(composer) {
        return false;
    }
};

// Add support for toggling all annotation comments.
wysihtml5.commands.allComments = {

    'exec': function(composer) {
        $(composer.element).toggleClass('rte-allComments');
    },

    'state': function(composer) {
        return $(composer.element).hasClass('rte-allComments');
    }
};

// Changes and comment support.
(function() {
    function iterateElements(composer, selector, callback) {
        var selection = composer.selection,
                range = selection.getRange();

        $(range.collapsed ?
                $(range.commonAncestorContainer).closest(selector) :
                range.getNodes([ Node.ELEMENT_NODE ])).each(function() {
            if ($(this).is(selector)) {
                callback.call(this);
            }
        });
    }

    function acceptOrReject(removeTag, unwrapTag) {
        return {
            'exec': function(composer) {
                iterateElements(composer, 'del, ins', function() {
                    var $element = $(this);

                    if ($element.is(removeTag)) {
                        $element.remove();

                    } else if ($element.is(unwrapTag)) {
                        $element.after($element.html());
                        $element.remove();
                    }
                });
            },

            'state': function() {
                return false;
            }
        };
    }

    $.extend(wysihtml5.commands, {
        'changesTrack': {
            'exec': function(composer) {
                $(composer.element).toggleClass('rte-changesTracking');
            },

            'state': function(composer) {
                return $(composer.element).hasClass('rte-changesTracking');
            }
        },

        'changesAccept': acceptOrReject('del', 'ins'),
        'changesReject': acceptOrReject('ins', 'del'),

        'commentAdd': {
            'exec': function(composer) {
                var selection = composer.selection,
                        $comment = $(selection.getRange().commonAncestorContainer).closest('.rte-comment');

                if ($comment.length === 0) {
                    wysihtml5.commands.formatInline.exec(composer, null, 'span', 'rte rte-comment');
                }
            },

            'state': function(composer) {
                $(composer.config.toolbar).toggleClass(
                        'rte-toolbarContainer-inComment',
                        $(composer.selection.getRange().commonAncestorContainer).closest('.rte-comment').length > 0);
                return false;
            }
        },

        'commentCollapse': {
            'exec': function(composer) {
                iterateElements(composer, '.rte-comment', function() {
                    var $selected = $(this),
                            comment = $selected.attr('data-comment');

                    if (comment) {
                        $selected.text(comment);
                        $selected.removeAttr('data-comment');

                    } else {
                        $selected.attr('data-comment', $selected.text());
                        $selected.text('\u2026');
                    }
                });
            },

            'state': function(composer) {
                return $(composer.selection.getRange().commonAncestorContainer).closest('.rte-comment-collapsed').length > 0;
            }
        },

        'commentRemove': {
            'exec': function(composer) {
                iterateElements(composer, '.rte-comment', function() {
                    var $selected = $(this),
                            text,
                            textLength;

                    if ($selected.length > 0) {
                        text = $selected.text();
                        textLength = text.length;

                        if (textLength === 0 ||
                                (textLength === 1 &&
                                text.charCodeAt(0) === 65279)) {
                            $selected.remove();

                        } else {
                            $selected.toggleClass('rte-comment-removed');
                        }
                    }
                });
            },

            'state': function(composer) {
                return $(composer.selection.getRange().commonAncestorContainer).closest('.rte-comment-removed').length > 0;
            }
        }
    });
})();

// Add support for toggling 'Fullscreen' mode.
wysihtml5.commands.fullscreen = {

    'exec': function(composer) {
        $('.toolBroadcast').toggle();
        $('.toolHeader').toggle();
        $(composer.parent.container).toggleClass('rte-fullscreen');
        $(composer.element).toggleClass('rte-fullscreen');
        $(doc.body).toggleClass('rte-fullscreen');
    },

    'state': function(composer) {
        return $(composer.element).hasClass('rte-fullscreen');
    }
};

// Expose as a jQuery plugin.
$.plugin2('rte', {
    '_defaultOptions': {
        'enhancement': createEnhancement,
        'iframeSrc': CONTEXT_PATH + '/style/rte-content.jsp',
        'marker': createMarker,
        'style': false,
        'toolbar': createToolbar,
        'useLineBreaks': !RTE_LEGACY_HTML,

        'parserRules': RTE_LEGACY_HTML ? { } : {
            'tags': {
                'font': { 'rename_tag': 'span' },
                'script': { 'remove': true },
                'style': { 'remove': true },
                'p': {
                    'rename_tag': 'span',
                    'callback': function(node) {
                        var $node = $(node);

                        $node.append($('<br>'));
                        $node.append($('<br>'));
                    }
                }
            }
        }
    },

    '_create': function(element) {
        var $element = $(element),
                options = $.extend(true, { }, this.option()),
                rte;

        if ($element.attr('data-inline') === 'true') {
            options.inline = true;
        }

        rte = new Rte(element, options);

        $element.bind('input-disable', function(event, disable) {
            $element.closest('.rte-container').toggleClass('state-disabled', disable);
            rte[disable ? 'disable' : 'enable']();
        });
    },

    'enable': function() {
        var container = this.$caller[0];

        if (container) {
            $.each(rtes, function() {
                var textarea = this.textareaElement;
                if (textarea && $.contains(container, textarea)) {
                    this.enable();
                }
            });
        }

        return this;
    },

    // Sets data related to the enhancement.
    'enhancement': function(data) {
        var $enhancement = this.$caller.closest('.rte-enhancement');
        var $placeholder = $enhancement.data('$rte-placeholder');

        if ($placeholder) {
            $.each(data, function(key, value) {
                var name = 'data-' + key;
                if (value === null || value === undef) {
                    $placeholder.removeAttr(name);
                } else {
                    $placeholder.attr(name, value);
                }
            });
        }

        var refData = $.parseJSON(data.reference || '{}');
        var label = refData.label;
        if (label) {
            $enhancement.find('.rte-enhancement-label').text(label);
        }

        return this;
    }
});

// Make sure that the editorial toolbar is visible as long as possible.
$win.bind('resize.rte scroll.rte', keepToolbarInView = $.throttle(100, function() {
    $.each(rtes, function() {
        var $toolbar = $(this.config.toolbar),
                $header,
                headerBottom,
                $container = $(this.container),
                $overlay = $(this.overlay),
                containerTop,
                windowTop;

        if (!$toolbar.is(':visible')) {
            $container.css('padding-top', 0);
            $overlay.css('top', 0);
            return;
        }

        if ($toolbar.closest('.rte-container').length === 0) {
            return;
        }

        $header = $('.toolHeader');
        headerBottom = $header.offset().top + $header.outerHeight() - ($header.css('position') === 'fixed' ? $win.scrollTop() : 0);
        containerTop = $container.offset().top;
        windowTop = $win.scrollTop() + headerBottom;

        // Completely in view.
        if (windowTop < containerTop) {
            $container.css('padding-top', 0);
            $overlay.css('top', $toolbar.outerHeight());
            $toolbar.removeClass('rte-toolbar-fixed');
            $toolbar.attr('style', this._toolbarOldStyle);
            this._toolbarOldStyle = null;

        } else {
            this._toolbarOldStyle = this._toolbarOldStyle || $toolbar.attr('style') || ' ';

            // Partially in view.
            if (windowTop < containerTop + $container.height()) {
                $container.css('padding-top', $toolbar.outerHeight());
                $overlay.css('top', 0);
                $toolbar.addClass('rte-toolbarContainer-fixed');
                $toolbar.css({
                    'left': $toolbar.offset().left,
                    'position': 'fixed',
                    'top': headerBottom,
                    'width': $toolbar.width()
                });

            // Completely out of view.
            } else {
                $toolbar.addClass('rte-toolbarContainer-fixed');
                $toolbar.css({
                    'top': -10000,
                    'position': 'fixed'
                });
            }
        }
    });
}));

}(jQuery, window));
