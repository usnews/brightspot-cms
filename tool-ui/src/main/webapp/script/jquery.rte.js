/** Rich text editor based on wysihtml5. */
(function($, win, undef) {

var $win = $(win),
        doc = win.document;

$.each(CSS_CLASS_GROUPS, function() {
    var command = 'cms-' + this.internalName;
    var prefix = command + '-';
    var regex = new RegExp(prefix + '[0-9a-z\-]+', 'g');

    wysihtml5.commands[command] = {
        'exec': function(composer, command, optionsString) {
            var options = optionsString ? $.parseJSON(optionsString) : { };
            var format = options.tag === 'span' ? 'formatInline' : 'formatBlock';
            return wysihtml5.commands[format].exec(composer, command, options.tag, prefix + options.internalName, regex);
        },

        'state': function(composer, command, optionsString) {
            var options = optionsString ? $.parseJSON(optionsString) : { };
            var format = options.tag === 'span' ? 'formatInline' : 'formatBlock';
            return wysihtml5.commands[format].state(composer, command, options.tag , prefix + options.internalName, regex);
        }
    };
});

var $createToolbarGroup = function(label) {
    var $group = $('<span/>', { 'class': 'rte-group', 'data-group-name': label });
    var $label = $('<span/>', { 'class': 'rte-group-label', 'text': label });
    $group.append($label);
    return $group;
};

var $createToolbarCommand = function(label, command) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + command,
        'data-wysihtml5-command': command,
        'text': label
    });
};

var createToolbar = function(inline) {
    var $container = $('<div/>', { 'class': 'rte-toolbar-container' });

    var $toolbar = $('<div/>', { 'class': 'rte-toolbar' });
    $container.append($toolbar);

    var $font = $createToolbarGroup('Font');
    $toolbar.append($font);

    $font.append($createToolbarCommand('Bold', 'bold'));
    $font.append($createToolbarCommand('Italic', 'italic'));
    $font.append($createToolbarCommand('Underline', 'underline'));
    $font.append($createToolbarCommand('Strike', 'strike'));
    $font.append($createToolbarCommand('Superscript', 'superscript'));
    $font.append($createToolbarCommand('Subscript', 'subscript'));

    $.each(CSS_CLASS_GROUPS, function() {
        var $group = $createToolbarGroup(this.displayName);
        var command = 'cms-' + this.internalName;
        $group.addClass('rte-group-cssClass');
        $toolbar.append($group);

        $.each(this.cssClasses, function() {
            var $cssClass = $createToolbarCommand(this.displayName, command);
            $cssClass.attr('data-wysihtml5-command-value', JSON.stringify(this));
            $group.append($cssClass);
        });
    });

    if (!inline) {
        var $alignment = $createToolbarGroup('Alignment') ;
        $toolbar.append($alignment);

        $alignment.append($createToolbarCommand('Justify Left', 'textAlign').attr('data-wysihtml5-command-value', 'left'));
        $alignment.append($createToolbarCommand('Justify Center', 'textAlign').attr('data-wysihtml5-command-value', 'center'));
        $alignment.append($createToolbarCommand('Justify Right', 'textAlign').attr('data-wysihtml5-command-value', 'right'));

        var $list = $createToolbarGroup('List');
        $toolbar.append($list);

        $list.append($createToolbarCommand('Unordered List', 'insertUnorderedList'));
        $list.append($createToolbarCommand('Ordered List', 'insertOrderedList'));
        $list.append($createToolbarCommand('Decrease Indent', 'outdent'));
        $list.append($createToolbarCommand('Increase Insent', 'indent'));
    }

    var $enhancement = $createToolbarGroup('Enhancement');
    $toolbar.append($enhancement);

    $enhancement.append($createToolbarCommand('Link', 'createLink'));

    if (!inline) {
        $enhancement.append($createToolbarCommand('Add Enhancement', 'insertEnhancement'));
        $enhancement.append($createToolbarCommand('Add Marker', 'insertMarker'));
    }

    var $misc = $createToolbarGroup('Misc');
    $toolbar.append($misc);

    $misc.append($('<span/>', {
        'class': 'rte-button rte-button-html',
        'data-wysihtml5-action': 'change_view',
        'text': 'HTML'
    }));

    $toolbar.append($(
        '<div class="rte-dialog" data-wysihtml5-dialog="createLink" style="display: none;">' +
            ' <label>URL:' +
            ' <input type="text" data-wysihtml5-dialog-field="href" value="http://"></label>' +
            ' <label>Window:' +
            ' <select data-wysihtml5-dialog-field="target">' +
                '<option value="_self">Same</option>' +
                '<option value="_blank">New</option>' +
            '</select></label>' +
            ' <a data-wysihtml5-dialog-action="save">OK</a>' +
            ' <a data-wysihtml5-dialog-action="cancel">Cancel</a>' +
        '</div>'));

    return $container[0];
};

var $createEnhancementAction = function(label, action) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + action,
        'data-action': action,
        'text': label
    });
};

var createEnhancement = function() {
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

    var $misc = $createToolbarGroup('Misc');
    $toolbar.append($misc);

    $misc.append($('<span/>', {
        'class': 'rte-button rte-button-editEnhancement',
        'html': $('<a/>', {
            'href': CONTEXT_PATH + '/content/enhancement.jsp?id=',
            'target': 'contentEnhancement',
            'text': 'Edit'
        })
    }));

    $misc.append($createEnhancementAction('Remove', 'remove'));

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
            'target': 'contentEnhancement',
            'text': 'Select'
        })
    }));

    $misc.append($createEnhancementAction('Remove', 'remove'));

    $marker.append($('<div/>', { 'class': 'rte-enhancement-label' }));

    return $marker[0];
};

// Wrap wysihtml5 to add functionality.
var rtes = [ ];
var enhancementId = 0;
var createEnhancementId = function() {
    ++ enhancementId;
    return enhancementId;
};

var Rte = wysihtml5.Editor.extend({

    'constructor': function(originalTextarea, config) {
        var rte = this;

        // Create container.
        var container = this.container = doc.createElement('div');
        container.className = 'rte-container';
        originalTextarea.parentNode.insertBefore(container, originalTextarea);

        // Create toolbar?
        if (typeof config.toolbar === 'function') {
            config.toolbar = config.toolbar(config.useLineBreaks);
            container.appendChild(config.toolbar);
        }

        // Create overlay.
        var overlay = this.overlay = doc.createElement('div');
        overlay.className = 'rte-overlay';
        overlay.style.position = 'relative';
        overlay.style.left = '0px';
        overlay.style.top = '0px';
        container.appendChild(overlay);

        // Handle toolbar action clicks.
        $(overlay).delegate('[data-action]', 'click', function() {
            var $button = $(this);
            var $placeholder = $button.closest('.rte-enhancement').data('$rte-placeholder');
            var action = $button.attr('data-action');

            if (action == 'remove') {
                $placeholder.remove();

            } else if (action === 'moveCenter') {
                $placeholder.removeAttr('data-alignment');

            } else if (action === 'moveLeft') {
                $placeholder.attr('data-alignment', 'left');

            } else if (action === 'moveRight') {
                $placeholder.attr('data-alignment', 'right');

            } else {
                var oldTop = $placeholder.offset().top;

                if (action === 'moveDown') {
                    $placeholder.parents().andSelf().filter('body > *').next().after($placeholder);
                    $win.scrollTop($win.scrollTop() + $placeholder.offset().top - oldTop);

                } else if (action === 'moveUp') {
                    $placeholder.parents().andSelf().filter('body > *').prev().before($placeholder);
                    $win.scrollTop($win.scrollTop() + $placeholder.offset().top - oldTop);
                }
            }

            rte.updateOverlay();

            return false;
        });

        // Initialize wysihtml5.
        container.appendChild(originalTextarea);
        originalTextarea.className += ' rte-textarea';

        rtes[rtes.length] = this;
        this.base(originalTextarea, config);

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
            textarea.element.className += ' rte-source';

            // Make sure only one toolbar is visible at a time.
            this.toolbar.hide();

            this.on('focus', function() {
                for (var i = 0, length = rtes.length; i < length; ++ i) {
                    rtes[i].toolbar.hide();
                }
                this.toolbar.show();
            });

            setInterval(function() {
                rte.updateOverlay();
            }, 100);
        });
    },

    // Updates the overlay to show enhancements above the underlying
    // placeholders.
    'updateOverlay': function() {
        var rte = this;

        // Hide if viewing source.
        var textarea = this.textarea;
        var overlay = this.overlay;

        overlay.style.display = this.currentView === textarea ? 'none' : 'block';

        // Automatically size the iframe height to match the content.
        var composer = this.composer;
        var composerIframe = composer.iframe;
        var composerIframeWindow = composerIframe.contentWindow;

        $(composerIframe).css('min-height', $(composerIframeWindow.document.body).height() + (rte.config.useLineBreaks ? 10 : 40));
        $(composerIframeWindow).scrollTop(0);

        // Create enhancements based on the underlying placeholders.
        var $overlay = $(overlay);

        $overlay.children().each(function() {
            $(this).data('rte-visited', false);
        });

        $(composerIframe.contentDocument.body).find('button.enhancement').each(function() {
            var $placeholder = $(this);
            var id = $placeholder.data('rte-enhancementId');

            if (!id) {
                id = createEnhancementId();
                $placeholder.data('rte-enhancementId', id);
            }

            var $enhancement = $('#rte-enhancement-' + id);
            var isMarker = wysihtml5.dom.hasClass($placeholder[0], 'marker');

            if ($enhancement.length === 0) {
                var newEnhancement = rte.config[isMarker ? 'marker' : 'enhancement']();
                newEnhancement.style.position = 'absolute';
                newEnhancement.setAttribute('id', 'rte-enhancement-' + id);
                $enhancement = $(newEnhancement);
                $enhancement.data('$rte-placeholder', $placeholder);
                $overlay.append($enhancement);

                $enhancement.find('.rte-button-editEnhancement a').each(function() {
                    var $anchor = $(this),
                            href = $anchor.attr('href');

                    href = href.replace(/([?&])id=[^&]*/, '$1');
                    href += '&id=' + $placeholder.attr('data-id');

                    $anchor.attr('href', href);
                });
            }

            $enhancement.data('rte-visited', true);

            // Position enhancement to cover the placeholder.
            var placeholderOffset = $placeholder.offset();

            $enhancement.css({
                'height': $placeholder.outerHeight(),
                'left': placeholderOffset.left,
                'top': placeholderOffset.top,
                'width': $placeholder.outerWidth()
            });

            // Copy the enhancement label.
            $enhancement.find('.rte-enhancement-label').text(
                    $placeholder.attr('data-label') ||
                    'Empty ' + (isMarker ? 'Marker' : 'Enhancement'));
        });

        // Remove orphaned enhancements.
        $overlay.children().each(function() {
            var $enhancement = $(this);
            if (!$enhancement.data('rte-visited')) {
                $enhancement.remove();
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

        composer.selection.insertNode(button);

        if (wysihtml5.browser.hasProblemsSettingCaretAfterImg()) {
            var textNode = doc.createTextNode(wysihtml5.INVISIBLE_SPACE);
            composer.selection.insertNode(textNode);
            composer.selection.setAfter(textNode);

        } else {
            composer.selection.setAfter(button);
        }
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

        composer.selection.insertNode(button);

        if (wysihtml5.browser.hasProblemsSettingCaretAfterImg()) {
            var textNode = doc.createTextNode(wysihtml5.INVISIBLE_SPACE);
            composer.selection.insertNode(textNode);
            composer.selection.setAfter(textNode);

        } else {
            composer.selection.setAfter(button);
        }
    },

    'state': function(composer) {
        return false;
    }
};

// Expose as a jQuery plugin.
$.plugin2('rte', {
    '_defaultOptions': {
        'enhancement': createEnhancement,
        'marker': createMarker,
        'style': false,
        'stylesheets': [ CONTEXT_PATH + '/style/rte-content.css', CONTEXT_PATH + '/style/rte-cssClasses.jsp' ],
        'toolbar': createToolbar,
        'useLineBreaks': false
    },

    '_create': function(element) {
        var options = $.extend(true, { }, this.option());

        if (typeof options.useLineBreaks === 'undefined') {
            options.useLineBreaks = !!$(element).attr('data-use-line-breaks');
        }

        new Rte(element, options);
    },

    'enable': function() {
        var container = this[0];

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

        var label = data.label;
        if (label) {
            $enhancement.find('.rte-enhancement-label').text(label);
        }

        return this;
    }
});

// Make sure that the editorial toolbar is visible as long as possible.
$win.bind('resize.rte scroll.rte', $.throttle(100, function() {
    $.each(rtes, function() {
        var $toolbar = $(this.config.toolbar),
                $header,
                headerBottom,
                $container,
                containerTop,
                windowTop;

        if (!$toolbar.is(':visible')) {
            return;
        }

        $header = $('.toolHeader');
        headerBottom = $header.offset().top + $header.outerHeight() - ($header.css('position') === 'fixed' ? $win.scrollTop() : 0);
        $container = $(this.container);
        containerTop = $container.offset().top;
        windowTop = $win.scrollTop() + headerBottom;

        // Completely in view.
        if (windowTop < containerTop) {
            $container.css('padding-top', 0);
            $toolbar.attr('style', this._toolbarOldStyle);
            this._toolbarOldStyle = null;

        } else {
            this._toolbarOldStyle = this._toolbarOldStyle || $toolbar.attr('style') || ' ';

            // Partially in view.
            if (windowTop < containerTop + $container.height()) {
                $container.css('padding-top', $toolbar.outerHeight());
                $toolbar.css({
                    'left': $toolbar.offset().left,
                    'position': 'fixed',
                    'top': headerBottom,
                    'width': $toolbar.width(),
                    'z-index': $container.zIndex() + 1
                });

            // Completely out of view.
            } else {
                $toolbar.css({
                    'top': -10000,
                    'position': 'fixed'
                });
            }
        }
    });
}));

}(jQuery, window));
