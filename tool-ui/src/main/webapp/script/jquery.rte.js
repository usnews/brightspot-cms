// Rich text editor.
if (typeof jQuery !== 'undefined') (function($) {

var $createToolbarGroup = function(label) {
    var $group = $('<span/>', { 'class': 'rte-group' });
    var $label = $('<span/>', { 'class': 'rte-group-label', 'text': label });
    $group.append($label);
    return $group;
};

var $createToolbarCommand = function(label, command) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + command,
        'data-wysihtml5-command': command,
        'text': label,
    });
};

var createToolbar = function() {
    return ($('<div/>', { 'class': 'rte-toolbar-container' })
        .append($('<div/>', { 'class': 'rte-toolbar' })
            .append($createToolbarGroup('Font')
                .append($createToolbarCommand('Bold', 'bold'))
                .append($createToolbarCommand('Italic', 'italic'))
                .append($createToolbarCommand('Underline', 'underline'))
                .append($createToolbarCommand('Strike', 'strike'))
                .append($createToolbarCommand('Superscript', 'superscript'))
                .append($createToolbarCommand('Subscript', 'subscript'))
            )
            .append($createToolbarGroup('Alignment')
                .append($createToolbarCommand('Justify Left', 'justifyLeft'))
                .append($createToolbarCommand('Justify Center', 'justifyCenter'))
                .append($createToolbarCommand('Justify Right', 'justifyRight'))
            )
            .append($createToolbarGroup('List')
                .append($createToolbarCommand('Unordered List', 'insertUnorderedList'))
                .append($createToolbarCommand('Ordered List', 'insertOrderedList'))
                .append($createToolbarCommand('Decrease Indent', 'outdent'))
                .append($createToolbarCommand('Increase Insent', 'indent'))
            )
            .append($createToolbarGroup('Enhancement')
                .append($createToolbarCommand('Link', 'createLink'))
                .append($createToolbarCommand('Add Enhancement', 'insertEnhancement'))
                .append($createToolbarCommand('Add Marker', 'insertMarker'))
            )
            .append($createToolbarGroup('Misc')
                .append($('<span/>', {
                    'class': 'rte-button rte-button-change_view',
                    'data-wysihtml5-action': 'change_view',
                    'text': 'Source',
                }))
            )
        )
        .append($(
            '<div data-wysihtml5-dialog="createLink" style="display: none;">' +
                '<b>Link:</b><br/><br/>' +
                '<label>' +
                'URL:' +
                '<input data-wysihtml5-dialog-field="href" value="http://">' +
                'Title:' +
                '<input data-wysihtml5-dialog-field="title" value="">' +
                'Target:' +
                '<select data-wysihtml5-dialog-field="target">' +
                    '<option value=""></option>' +
                    '<option value="_blank">New Window (_blank)</option>' +
                    '<option value="_top">Top-most Window (_top)</option>' +
                    '<option value="_self">Same Window (_self)</option>' +
                    '<option value="_parent">Parent Window (_parent)</option>' +
                '</select>        ' +
                '</label>' +
                '<a data-wysihtml5-dialog-action="save">OK</a>&nbsp;' +
                '<a data-wysihtml5-dialog-action="cancel">Cancel</a>' +
            '</div>'
        ))
    )[0];
};

var $createEnhancementAction = function(label, action) {
    return $('<span/>', {
        'class': 'rte-button rte-button-' + action,
        'data-action': action,
        'text': label,
    });
};

var createEnhancement = function() {
    return ($('<div/>', { 'class': 'rte-enhancement' })
        .append($('<div/>', { 'class': 'rte-toolbar' })
            .append($createToolbarGroup('Position')
                .append($createEnhancementAction('Move Left', 'moveLeft'))
                .append($createEnhancementAction('Move Up', 'moveUp'))
                .append($createEnhancementAction('Move Center', 'moveCenter'))
                .append($createEnhancementAction('Move Down', 'moveDown'))
                .append($createEnhancementAction('Move Right', 'moveRight'))
            )
            .append($createToolbarGroup('Misc')
                .append($('<span/>', { 'class': 'rte-button rte-button-editEnhancement' })
                    .append($('<a/>', {
                        'href': CONTEXT_PATH + '/content/enhancement.jsp?id=',
                        'target': 'contentEnhancement',
                        'text': 'Edit'
                    }))
                )
                .append($createEnhancementAction('Remove', 'remove'))
            )
        )
        .append($('<div/>', { 'class': 'rte-enhancement-label' }))
    )[0];
};

var createMarker = function() {
    return ($('<div/>', { 'class': 'rte-enhancement rte-marker' })
        .append($('<div/>', { 'class': 'rte-toolbar' })
            .append($createToolbarGroup('Position')
                .append($createEnhancementAction('Move Up', 'moveUp'))
                .append($createEnhancementAction('Move Down', 'moveDown'))
            )
            .append($createToolbarGroup('Misc')
                .append($('<span/>', { 'class': 'rte-button rte-button-selectMarker' })
                    .append($('<a/>', {
                        'href': CONTEXT_PATH + '/content/marker.jsp',
                        'target': 'contentEnhancement',
                        'text': 'Select'
                    }))
                )
                .append($createEnhancementAction('Remove', 'remove'))
            )
        )
        .append($('<div/>', { 'class': 'rte-enhancement-label' }))
    )[0];
};

// Wrap wysihtml5 to add functionality.
var rtes = [ ];
var enhancementId = 0;
var createEnhancementId = function() {
    ++ enhancementId;
    return enhancementId;
}

var Rte = wysihtml5.Editor.extend({

    'constructor': function(originalTextarea, config) {
        var rte = this;

        // Create container.
        var container = this.container = document.createElement('div');
        container.className = 'rte-container';
        originalTextarea.parentNode.insertBefore(container, originalTextarea);

        // Create toolbar?
        if (typeof config.toolbar === 'function') {
            config.toolbar = config.toolbar();
            container.appendChild(config.toolbar);
        }

        // Create overlay.
        var overlay = this.overlay = document.createElement('div');
        overlay.className = 'rte-overlay';
        overlay.style.position = 'relative';
        overlay.style.left = '0px';
        overlay.style.top = '0px';
        container.appendChild(overlay);

        // Handle toolbar action clicks.
        $(overlay).find('[data-action]').live('click', function() {
            var $button = $(this);
            var $placeholder = $button.closest('.rte-enhancement').data('$rte-placeholder');
            var action = $button.attr('data-action');

            if (action === 'moveRight') {
                $placeholder.attr('data-alignment', 'right');

            } else if (action === 'moveCenter') {
                $placeholder.removeAttr('data-alignment');

            } else if (action === 'moveLeft') {
                $placeholder.attr('data-alignment', 'left');

            } else if (action === 'moveUp') {
                var oldTop = $placeholder.offset().top;
                var $window = $(window);
                $placeholder.parents().andSelf().filter('body > *').prev().before($placeholder);
                $window.scrollTop($window.scrollTop() + $placeholder.offset().top - oldTop);

            } else if (action == 'moveDown') {
                var oldTop = $placeholder.offset().top;
                var $window = $(window);
                $placeholder.parents().andSelf().filter('body > *').next().after($placeholder);
                $window.scrollTop($window.scrollTop() + $placeholder.offset().top - oldTop);

            } else if (action == 'remove') {
                $placeholder.remove();
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

            // Make sure placeholder IMGs are replaced with enhancement SPANs.
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

                            callback(newNode);
                            parent.insertBefore(newNode, node);
                            parent.removeChild(node);

                        } else {
                            convertNodes(node, oldTagName, newTagName, callback);
                        }
                    }
                }
            };

            var textarea = this.textarea;

            textarea._originalGetValue = textarea.getValue;
            textarea.getValue = function() {
                var dom = wysihtml5.dom.getAsDom(this._originalGetValue(), this.element.ownerDocument);
                convertNodes(dom, 'SPAN', 'IMG', function(node) {
                    node.setAttribute('src', rte.config.spacerUrl);
                });
                return dom.innerHTML;
            };

            var composer = this.composer;

            composer._originalGetValue = composer.getValue;
            composer.getValue = function() {
                var dom = wysihtml5.dom.getAsDom(this._originalGetValue(), this.element.ownerDocument);
                convertNodes(dom, 'IMG', 'SPAN', function(node) {
                    node.innerHTML = 'Enhancement';
                });
                return dom.innerHTML;
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
            })

            setInterval(function() {
                rte.updateOverlay();
            }, 100)
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

        $(composerIframe).css('min-height', $(composerIframeWindow.document.body).height() + 40);
        $(composerIframeWindow).scrollTop(0);

        // Create enhancements based on the underlying placeholders.
        var $overlay = $(overlay);

        $overlay.children().each(function() {
            $(this).data('rte-visited', false);
        });

        $(composerIframe.contentDocument.body).find('img.enhancement').each(function() {
            var $placeholder = $(this);
            var id = $placeholder.data('rte-enhancementId');

            if (!id) {
                id = createEnhancementId();
                $placeholder.data('rte-enhancementId', id);
            }

            var $enhancement = $('#rte-enhancement-' + id);
            var isMarker = wysihtml5.dom.hasClass($placeholder[0], 'marker');

            if ($enhancement.length == 0) {
                var newEnhancement = rte.config[isMarker ? 'marker' : 'enhancement']();
                newEnhancement.style.position = 'absolute';
                newEnhancement.setAttribute('id', 'rte-enhancement-' + id);
                $enhancement = $(newEnhancement);
                $enhancement.data('$rte-placeholder', $placeholder);
                $overlay.append($enhancement);
            }

            $enhancement.data('rte-visited', true);

            // Position enhancement to cover the placeholder.
            var $window = $(window);
            var placeholderOffset = $placeholder.offset();

            $enhancement.css({
                'height': $placeholder.outerHeight(),
                'left': placeholderOffset.left - $window.scrollLeft() + 4,
                'top': placeholderOffset.top - $window.scrollTop() + 4,
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

// Remove support for insertImage so that it can't be used accidentantly,
// since insertEnhancement supercedes its functionality.
delete wysihtml5.commands.insertImage;

// Add support for adding an enhancement.
wysihtml5.commands.insertEnhancement = {

    'exec': function(composer, command, value) {
        var doc = composer.doc;
        var image = doc.createElement('IMG');

        if (value) {
            for (var i in value) {
                image.setAttribute(i, value[i]);
            }
        }

        image.setAttribute('class', 'enhancement');
        image.setAttribute('src', composer.parent.config.spacerUrl);

        composer.selection.insertNode(image);

        if (wysihtml5.browser.hasProblemsSettingCaretAfterImg()) {
            var textNode = doc.createTextNode(wysihtml5.INVISIBLE_SPACE);
            composer.selection.insertNode(textNode);
            composer.selection.setAfter(textNode);

        } else {
            composer.selection.setAfter(image);
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
        var image = doc.createElement('IMG');

        if (value) {
            for (var i in value) {
                image.setAttribute(i, value[i]);
            }
        }

        image.setAttribute('class', 'enhancement marker');
        image.setAttribute('src', composer.parent.config.spacerUrl);

        composer.selection.insertNode(image);

        if (wysihtml5.browser.hasProblemsSettingCaretAfterImg()) {
            var textNode = doc.createTextNode(wysihtml5.INVISIBLE_SPACE);
            composer.selection.insertNode(textNode);
            composer.selection.setAfter(textNode);

        } else {
            composer.selection.setAfter(image);
        }
    },

    'state': function(composer) {
        return false;
    }
};

// Expose as a jQuery plugin.
$.plugin('rte', {

// Initializes the rich text editor.
'init': function() {
    return this.liveInit(function() {
        var rte = new Rte(this, {
            'enhancement': createEnhancement,
            'marker': createMarker,
            'spacerUrl': 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==',
            'stylesheets': CONTEXT_PATH + '/style/rte-content.css',
            'toolbar': createToolbar,
            'useLineBreaks': false
        });
    });
},

// Sets data related to the enhancement.
'enhancement': function(data) {
    var $enhancement = this.closest('.rte-enhancement');
    var $placeholder = $enhancement.data('$rte-placeholder');

    if ($placeholder) {
        $.each(data, function(key, value) {
            var name = 'data-' + key;
            if (value === null || value === undefined) {
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

})(jQuery);
