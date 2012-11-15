// Rich text editor.
if (typeof jQuery !== 'undefined') (function($) {

var lastFocus;
var lastBlur;
var padding = 4;

$.plugin('editor', {

// Sets data related to the enhancement.
'enhancement': function(data) {

    var $overlay = this.closest('.enhancement');
    var $original = $overlay.data('$originalEnhancement');

    if ($original) {
        var $enhancement = $(decodeURIComponent($original.attr('_cke_realelement')));
        $.each(data, function(key, value) {

            var name = 'data-' + key;
            if (value !== null) {
                $original.attr(name, value);
                $enhancement.attr(name, value);
            } else {
                $original.removeAttr(name);
                $enhancement.removeAttr(name);
            }

            var $temp = $('<span/>');
            $temp.append($enhancement);
            $original.attr('_cke_realelement', encodeURIComponent($temp.html()));
        });

        var label = data.label;
        if (label) {
            $overlay.find('> .label').text(label);
        }
    }

    return this;
},

// Initializes the rich text editor.
'init': function() {
    return this.liveInit(function() {

        // Main toolbar that's shared across multiple editors.
        var $mainToolbar = $('#editorMainToolbar');
        if ($mainToolbar.length === 0) {
            $mainToolbar = $('<div/>', { 'id': 'editorMainToolbar' });
            $mainToolbar.hide();
            $(document.body).append($mainToolbar);
        }

        $(this).ckeditor(function() {

            // So that each toolbar can be styled individually.
            $mainToolbar.find('.cke_editor').each(function() {
                $(this).find('.cke_toolbar').addClass(function(index) {
                    return 'cke_toolbar_' + index;
                });
            });

            this.updateEnhancements();
            this.updateHeight();

            var $container = this.getContainer();
            var $iframe = $container.find('iframe');
            var $content = $iframe.parent();
            var updateHeightTimer;

            $iframe.css('overflow', 'hidden');
            $content.css('height', 'auto');

            this.on('focus', function() {

                // Make sure the focus is on this editor and blur the last if necessary.
                if (lastFocus == this) {
                    return;

                } else {
                    $container.addClass('focus');
                    if (lastBlur) {
                        lastBlur.call(lastFocus);
                    }

                    // Set up for blurring this editor when a new editor is selected.
                    lastFocus = this;
                    lastBlur = function() {
                        $container.removeClass('focus');
                        $content.css('padding-top', $content.data('originalPaddingTop') || 0);
                        $mainToolbar.hide();
                        $(window).unbind('scroll.editor');
                        this.updateEnhancements();
                        if (updateHeightTimer) {
                            clearInterval(updateHeightTimer);
                            updateHeightTimer = null;
                        }
                    };
                }

                // Move the toolbar to the top of the editor content area.
                var contentOffset = $content.offset();
                $mainToolbar.css({
                    'left': contentOffset.left + padding,
                    'position': 'absolute',
                    'top': contentOffset.top + padding
                });
                var mainToolbarHeight = $mainToolbar.outerHeight(true);
                var contentPaddingTop = parseFloat($content.css('padding-top')) || 0;
                $content.data('originalPaddingTop', contentPaddingTop);
                $content.css('padding-top', contentPaddingTop + mainToolbarHeight + padding);
                $mainToolbar.show();

                // Make sure the main toolbar is always in view.
                var $toolHeader = $('.toolHeader');
                var headerBottom = $toolHeader.offset().top + $toolHeader.outerHeight() - ($toolHeader.css('position') === 'fixed' ? $(window).scrollTop() : 0) + padding;
                var oldCss;
                $(window).bind('resize.editor scroll.editor', $.throttle(100, function() {
                    var contentTop = $content.offset().top;
                    if (oldCss) {

                        // Completely in view.
                        var windowTop = $(window).scrollTop() + headerBottom;
                        if (windowTop < contentTop) {
                            $mainToolbar.css(oldCss);
                            oldCss = null;

                        // Completely out of view.
                        } else if (windowTop > contentTop + $content.height()) {
                            $mainToolbar.hide();
                            return;
                        }

                    // Partially in view.
                    } else if ($(window).scrollTop() + headerBottom > contentTop) {
                        oldCss = {
                            'left': $mainToolbar.css('left'),
                            'position': $mainToolbar.css('position'),
                            'top': $mainToolbar.css('top')
                        };
                        $mainToolbar.css({
                            'left': $mainToolbar.offset().left,
                            'position': 'fixed',
                            'top': headerBottom
                        });
                    }

                    $mainToolbar.css({
                        'top': contentTop + padding,
                        'width': $content.outerWidth(true) - padding * 2,
                        'z-index': $container.zIndex() + 1
                    });
                    $mainToolbar.show();
                }));
                $(window).resize();

                // Make sure that all the content in the editor area is in the correct place and visible.
                this.updateEnhancements();
                $(window).bind('resize.editor', $.throttle(100, $.proxy(this.updateEnhancements, this)));
                this.updateHeight();
                updateHeightTimer = setInterval($.proxy(this.updateHeight, this), 200);
            });
        }, {

            'contentsCss': CONTEXT_PATH + 'style/contents.css?1',
            'dialog_backgroundCoverOpacity': 1.0,
            'disableObjectResizing': true,
            'extraPlugins': 'enhancement',
            'ignoreEmptyParagraph': true,
            'removePlugins': 'contextmenu,maximize,menu,resize',
            'sharedSpaces': { 'top': 'editorMainToolbar' },
            'skin': 'cms,' + CONTEXT_PATH + 'style/',
            'toolbarCanCollapse': false,
            'toolbar': [
                [ 'Bold', 'Italic', 'Underline', 'Strike', 'Superscript', 'Subscript' ],
                [ 'JustifyLeft', 'JustifyCenter', 'JustifyRight' ],
                [ 'BulletedList', 'NumberedList', 'Outdent', 'Indent' ],
                [ 'Link', 'Unlink', 'AddEnhancement', 'AddMarker' ],
                [ 'Source' ]
            ]
        });
    });
}

});

// Creates an enhancement plugin that creates a fake placeholder element on demand.
CKEDITOR.plugins.add('enhancement', {

    'requires': [ 'fakeobjects' ],

    'afterInit': function(editor) {
        var dataProcessor = editor.dataProcessor;
        var dataFilter = dataProcessor && dataProcessor.dataFilter;
        if (!dataFilter) {
            return;
        }
        dataFilter.addRules({
            'elements': {
                'span': function(element) {
                    var originalAttributes = element.attributes;
                    var className = originalAttributes['class'];
                    if ((' ' + className + ' ').indexOf(' enhancement ') > -1) {
                        element = editor.createFakeParserElement(element, 'cke_enhancement ' + className, 'span', false);
                        $.each(originalAttributes, function(key, value) {
                            if (key.substring(0, 5) == 'data-') {
                                element.attributes[key] = value;
                            }
                        });
                    }
                    return element;
                }
            }
        }, 5);
    },

    'init': function(editor) {
        editor.addCommand('addEnhancement', {
            'exec': function() {
                var enhancementNode = CKEDITOR.dom.element.createFromHtml('<span>Enhancement</span>', editor.document);
                enhancementNode.setAttributes({ 'class': 'enhancement' });
                editor.insertElement(editor.createFakeElement(enhancementNode, 'cke_enhancement', 'span'));
                editor.updateEnhancements();
            }
        });
        editor.ui.addButton('AddEnhancement', {
            'command': 'addEnhancement',
            'label': 'Add Enhancement'
        });

        editor.addCommand('addMarker', {
            'exec': function() {
                var enhancementNode = CKEDITOR.dom.element.createFromHtml('<span>Marker</span>', editor.document);
                enhancementNode.setAttributes({ 'class': 'enhancement marker' });
                editor.insertElement(editor.createFakeElement(enhancementNode, 'cke_enhancement', 'span'));
                editor.updateEnhancements();
            }
        });
        editor.ui.addButton('AddMarker', {
            'command': 'addMarker',
            'label': 'Add Marker'
        });

        // Workaround for CKEditor not having an event for mode change.
        var oldSetMode = editor.setMode;
        editor.setMode = function() {
            oldSetMode.apply(editor, arguments);
            if (arguments[0] == 'wysiwyg' && editor.focusManager && editor.focusManager.hasFocus) {
                var updatesTimer = setInterval(function() {
                    if (editor.mode == 'wysiwyg') {
                        editor.updateEnhancements();
                        editor.updateHeight();
                        clearInterval(updatesTimer);
                    }
                }, 100);
            }
        };
    }
});

// Returns the outermost element that contains the editor.
CKEDITOR.editor.prototype.getContainer = function() {
    return $('#cke_' + this.name);
};

var createGroup = function($toolbar, name) {
    var $group = $('<li/>', { 'class': 'group ' + name });
    var $list = $('<ul/>');
    $group.append($list);
    $toolbar.append($group);
    return $group;
};

// Updates the visual display of all the enhancement overlays.
CKEDITOR.editor.prototype.updateEnhancements = function() {

    var editor = this;
    var $container = this.getContainer();
    var $iframe = $container.find('iframe');
    var $content = $iframe.parent();

    // Create an overlay container used for positioning if one doesn't already exist.
    var $overlayContainer = $content.find('> .overlayContainer');
    if ($overlayContainer.length === 0) {
        $overlayContainer = $('<div/>', { 'class': 'overlayContainer' });
        $content.prepend($overlayContainer);
    }

    $iframe.contents().find('.cke_enhancement').each(function() {
        var $original = $(this);
        var $overlay = $original.data('$enhancementOverlay');
        if (!$overlay) {

            // No overlay for this enhancement already so create one.
            $overlay = $(decodeURIComponent($original.attr('_cke_realelement')));
            $original.data('$enhancementOverlay', $overlay);

            $overlay.empty();
            $overlay.css({ 'position': 'absolute' });
            $overlay.data('$originalEnhancement', $original);
            $overlay.click(function() {
                editor.focus();
            });

            var createAction = function(name, label, command, extra) {
                return $('<li/>', {
                    'class': 'action ' + name,
                    'title': label,
                    'click': function() {
                        if (typeof command === 'function') {
                            command();
                        } else {
                            editor.getContainer().find('iframe')[0].contentWindow.document.execCommand(commanse, false, extra);
                        }
                    }
                });
            };

            var isMarker = $overlay.is('.marker');
            var $enhancementToolbar = $('<ul/>', { 'class': 'toolbar' });

            // Position toolbar group.
            var $positionGroup = createGroup($enhancementToolbar, 'position');
            if (!isMarker) {
                $positionGroup.append(createAction('moveLeft', 'Move left', function() {
                    $overlay.editor('enhancement', { 'alignment': 'left' });
                    editor.updateEnhancements();
                }));
            }
            $positionGroup.append(createAction('moveUp', 'Move up', function() {
                var oldTop = $original.offset().top;
                $original.parents().andSelf().filter('body > *').prev().before($original);
                $(window).scrollTop($(window).scrollTop() + $original.offset().top - oldTop);
                editor.updateEnhancements();
            }));
            if (!isMarker) {
                $positionGroup.append(createAction('moveCenter', 'Fill', function() {
                    $overlay.editor('enhancement', { 'alignment': null });
                    editor.updateEnhancements();
                }));
            }
            $positionGroup.append(createAction('moveDown', 'Move down', function() {
                var oldTop = $original.offset().top;
                $original.parents().andSelf().filter('body > *').next().after($original);
                $(window).scrollTop($(window).scrollTop() + $original.offset().top - oldTop);
                editor.updateEnhancements();
            }));
            if (!isMarker) {
                $positionGroup.append(createAction('moveRight', 'Move right', function() {
                    $overlay.editor('enhancement', { 'alignment': 'right' });
                    editor.updateEnhancements();
                }));
            }

            // Miscellaneous toolbar group.
            var $miscGroup = createGroup($enhancementToolbar, 'misc');
            $miscGroup.append($('<li/>', { 'class': 'action edit' }).append($('<a/>', isMarker ? {
                'href': CONTEXT_PATH + 'content/marker.jsp',
                'target': 'contentMarker',
                'text': 'Select',
                'title': 'Select a marker'
            } : {
                'href': CONTEXT_PATH + 'content/enhancement.jsp?id=' + ($overlay.attr('data-id') || ''),
                'target': 'contentEnhancement',
                'text': 'Edit',
                'title': 'Edit this enhancement'
            })));
            $miscGroup.append(createAction('remove', "Remove", function() {
                $original.remove();
                $overlay.remove();
                editor.updateEnhancements();
            }));

            $overlay.append($enhancementToolbar);
            $overlay.append($('<div/>', {
                'class': 'label',
                'text': $overlay.attr('data-label') || $overlay.attr('data-id') || 'Empty ' + (isMarker ? 'Marker' : 'Enhancement')
            }));
            $overlayContainer.append($overlay);
        }

        // Update overlay position and dimension to match the original in the iframe.
        var originalOffset = $original.offset();
        $overlay.css({
            'height': $original.height(),
            'left': originalOffset.left - $(window).scrollLeft(),
            'top': originalOffset.top - $(window).scrollTop(),
            'width': $original.width()
        });
    });
};

// Updates the height of the editor area to fit the content.
CKEDITOR.editor.prototype.updateHeight = function() {
    var $container = this.getContainer();
    var $iframe = $container.find('iframe');
    if ($iframe.length > 0) {
        var iframeWindow = $iframe[0].contentWindow;
        $iframe.height($(iframeWindow.document.body).height() + 40);
        $(iframeWindow).scrollTop(0);
    }
};

})(jQuery);
