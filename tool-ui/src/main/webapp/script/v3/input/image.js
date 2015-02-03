define([
    'jquery',
    'bsp-utils',
    'pixastic/pixastic.core',
    'pixastic/actions/blurfast',
    'pixastic/actions/brightness',
    'pixastic/actions/crop',
    'pixastic/actions/desaturate',
    'pixastic/actions/fliph',
    'pixastic/actions/flipv',
    'pixastic/actions/invert',
    'pixastic/actions/rotate',
    'pixastic/actions/sepia',
    'pixastic/actions/sharpen'
], function($, bsp_utils) {
    
    var imageEditorUtility;

    // Singleton object for the image editor.
    //
    // Do not directly use this object - instead create your own instance using Object.create():
    //   myImageEditor = Object.create(imageEditorUtility);
    //   myImageEditor.init(element, settings);

    imageEditorUtility = {

        /**
         * The data for each image size comes over in multiple different hidden inputs.
         * This is an array of the hidden input names.
         */
        inputNames: 'x y width height texts textSizes textXs textYs textWidths'.split(' '),

        
        /**
         * The text overlay information comes over in a single string.
         * Split on this delimiter to separate the data.
         */
        textDelimiter: 'aaaf7c5a9e604daaa126f11e23e321d8',

        
        /**
         * Each size will have an entry in this object.
         * Key = name of the size
         * Value = an object containing information about the size such as a description,
         * a jquery object containing the inputs, etc.
         *
         * @example
         * {
         *   'thumb_109x73': {description:'Thumbnail 109 x 73', inputs:{}},
         *   'promo_177x118' {description:'Small Promo 177 x 118', inputs:{}}
         * }
         */
        sizeInfos: {},


        /**
         * We want to group together sizes that have approximately the same aspect ratio.
         * Key = the aspect ratio, or the name of an individual size
         * Value = an object that contains the element for the group, and a sizeInfos object
         * with all the sizes in the group.
         *   
         * @example
         * {
         *   '1.5': {
         *     $element: e,
         *     $sizeBox: e,
         *     sizeInfos: {}
         *   }
         * }
         */
        sizeGroups: {},


        /**
         * Call to initialize the image editor.
         *
         * @param Element element
         * The element that contains the input information.
         * This element must contain the various HTML elements that will be used by the image editor.
         *
         * @param Object settings
         * And object of key value pairs to override the image editor default settings.
         */
        init: function(element, settings) {

            var self = this;

            // Save the main element for future use
            self.$element = $(element);

            // Get other stuff from the DOM
            self.initDOM();

            self.initEdit();
            self.initScrollFix();
            self.initCover();
            self.initSizes();
            self.initText();

            // Process all current adjustments on the image
            self.adjustmentProcess();

            // Automatically select the first size group if there is only one
            self.sizesSelectSingle()
        },

        
        /**
         * Find stuff on the page and save it for future reference.
         */
        initDOM: function() {
            
            var self = this;
            var $el = self.$element;
            var dom = {};
            var scale;

            // Set up an object to hold all dom information
            self.dom = dom;

            dom.$form = $el.closest('form');

            // Find the image. Note this will be removed from the DOM and
            // replaced by an adjusted image.
            dom.$image = $el.find('.imageEditor-image img');
            dom.imageSrc = dom.$image.attr('src'),

            // Save a copy of the original image so we will always have
            // it after adjustments are made.
            dom.$originalImage = dom.$image;
            
            // Create a clone of the image so we always have a copy of the original image.
            dom.$imageClone = dom.$image.clone();
            dom.imageClone = dom.$imageClone[0];

            // Get the data-scale attribute to use for ???
            scale = dom.$image.attr('data-scale');
            if (scale === undefined || scale === '') {
                scale = 1.0;
            }
            self.scale = scale;

            dom.$aside = $el.find('> .imageEditor-aside');
            dom.$tools = $el.find('.imageEditor-tools ul');
            dom.$edit = $el.find('.imageEditor-edit');

            dom.$sizes = $el.find('.imageEditor-sizes table');
            
            // The data-name attribute is used to create new hidden inputs when needed
            self.dataName = $el.closest('.inputContainer').attr('data-name');
        },


        /**
         * Sets up the "Edit Image" controls for modifying image adjustments.
         */
        initEdit: function() {
            var self;

            self = this;
            
            self.initEditButton();
            self.initEditInputs();
        },

        
        /**
         * Adds the "Edit Image" button to pop up the image adjustments.
         */
        initEditButton: function() {

            var self;
            
            self = this;
            
            // Create the edit button and add it to the page
            self.dom.$editButton = $('<li/>', {
                html: $('<a/>', {
                    'class': 'action-image-edit',
                    'text': 'Edit Image',
                    'click': function(event) {
                        self.editOpenAdjustments(this);
                        return false;
                    }
                })
            }).prependTo(self.dom.$tools);

            // Create the reset button
            self.dom.$editResetButton = $('<button/>', {
                'text': 'Reset',
                'click': function() {
                    self.editReset();
                    return false;
                }
            }).appendTo(self.dom.$edit);

            // Set up the popup
            self.dom.$edit.popup();
            self.dom.$edit.popup('container').addClass('imageEditor-editPopup');
            self.dom.$edit.popup('close');
            
            // Set up the adjustment popup so when the popup is open,
            // the image is automatically updated to match the changes.
            // When the popup is closed we stop automatically updating.
            
            self.dom.$edit.popup('container').bind('open', function() {
                self.adjustmentProcessTimerStart();
            });

            self.dom.$edit.popup('container').bind('close', function() {
                self.adjustmentProcessTimerStop();
            });

        },

        
        //--------------------------------------------------
        // IMAGE ADJUSTMENTS
        //--------------------------------------------------
        
        /**
         * Creates a place to store some additional hidden inputs for the edit controls.
         * These additional inputs will be destroyed and rewritten each time we change the image adjustments.
         *
         * Note some of the adjustments are checkboxes in which case the checkbox is directly modified.
         * Other adjustments are ranges (<input type="range">) in which case an additional hidden input
         * is created to hold the actual value.
         */
        initEditInputs: function() {
            
            var self;

            self = this;

            self.dom.$editInputs = $('<div/>').appendTo(self.dom.$editButton);
        },

        
        /**
         * Open the Adjustments pop up.
         */
        editOpenAdjustments: function() {
            
            var $edit, self;

            self = this;
            $edit = self.dom.$edit;
            $edit.popup('source', self.dom.$editButton).popup('open');

            // If there are any blurred areas defined, then show them on the image
            // ??? How do these get removed if the adjustments are closed?
            self.adjustmentBlurShow();
        },


        /**
         * Reset all adjustments on the image.
         */
        editReset: function() {

            var self;

            self = this;

            // Clear all the checkboxes and inputs for the adjustments
            self.dom.$edit.find(':input').each(function() {
                
                var $input = $(this);

                if ($input.is(':checkbox')) {
                    $input.removeAttr('checked');
                } else {
                    $input.val(0);
                }
            });

            // Remove all the blur adjustments for the image
            // Note this only appears when using the java image editor
            // TODO: move to separate blur function
            self.$element.find(".imageEditor-blurOverlay").remove();

            // Reprocess the image to remove the adjustments
            self.adjustmentProcess();
        },

        
        /**
         * Start repeatedly processing the image adjustments.
         */
        adjustmentProcessTimerStart: function() {
            var self;

            self = this;

            // Kill any existing timer just in case
            self.adjustmentProcessTimerStop();
            
            // Run the image adjustments, then when they are done delay a bit
            // and run them again. Continue until the timer is killed.
            self.adjustmentProcess().always(function(){

                self.adjustmentProcessTimer = setTimeout(function(){
                    self.adjustmentProcessTimerStart();
                }, 100);
                
            });
        },

        
        /**
         * Stop repeatedly processing the image adjustments.
         */
        adjustmentProcessTimerStop: function() {
            
            var self;
            
            self = this;
            
            clearTimeout(self.adjustmentProcessTimer);
        },

        
        /**
         * Process the image to perform adjustments based on the adjustment inputs.
         * Only actually processes the image if the adjustment settings have changed
         * since the last time this function was called.
         *
         * @return Promise
         * Returns a promise that can be used to run additional code
         * after all the processing is done.
         */
        adjustmentProcess: function() {
            
            var operations, operationsJson, self;

            self = this;

            // Get the list of operations that should be performed
            operations = self.adjustmentGetOperations();

            // Now turn the operations object into a JSON string so we
            // can compare it to previous operations that have already
            // been applied, to avoid running adjustments unnecessarily
            operationsJson = JSON.stringify(operations);
            if (operationsJson !== self.operationsPrevious) {

                // The operations are different from the previous operations,
                // so perform the processing

                // Save the operations so they can be compared later
                self.operationsPrevious = operationsJson;

                // Create hidden inputs to save all the adjustment values
                self.adjustmentSetInputs();

                // Now perform the operations and return a promise that can
                // be used to run more code after the operations have completed
                return self.adjustmentProcessExecuteAll();
            }
            
            // No operations have to be performed, so just return
            // an already resolved promise
            return $.Deferred().resolve().promise();
        },


        /**
         * After the operations object has been set up, this function executes all the image adjustments.
         * It does each operation sequentially.
         *
         * @returns Promise
         * Returns a promise that can be used to run additional code after the processing has completed.
         */
        adjustmentProcessExecuteAll: function() {

            var promise, operationKeys, self;

            self = this;
            
            // Create an already resolved promise to start things off.
            // We will modify this promise and add a task for each adjustment.
            promise = $.Deferred().resolve().promise();

            // Create the processed image - start from a copy of the original image
            self.dom.processedImage = self.dom.imageClone;
            
            // Loop through each of the operations and perform them sequentially
            $.each(self.operations, function(name, value) {

                var data;

                // If the operation has a "data" parameter that means there is an array
                // of operations to perform. To keep things simple let's convert even
                // the single operations into an array then loop through them.
                data = value.data || [value];

                // Loop through the array of operations
                $.each(data, function(index, value) {

                    var previousPromise;

                    previousPromise = promise;
                    
                    // Chain a new promise to run after the last one
                    promise = previousPromise.then(function(){
                    
                        // Run a single operation.
                        // This will return a new promise that will be used to chain
                        // multiple operations one after another.
                        return self.adjustmentProcessExecuteSingle(name, value);
                    });
                });

            });

            // When all the adjustments are done replace the original image with the adjusted image
            promise.done(function(){

                // Remove the old image from the dom unless this is the first time making an adjustment.
                // If this is the first time, then the original image will be hidden so we don't remove it.
                if (self.dom.$image !== self.dom.$originalImage) {
                    self.dom.$image.remove();
                }

                // Add the adjusted image to the dom
                self.dom.$originalImage.hide().before( self.dom.processedImage );

                // Save the adjusted image for later comparison
                self.dom.$image = $( self.dom.processedImage );
                
            });
            
            // Return a promise that can be used to run additional code
            // after the processing has completed.
            return promise;
        },

        
        /**
         * Run a single adjustment operation
         *
         * @param String operationName
         * The name of the operation (used by Pixastic)
         *
         * @param Object operationValue
         * The operation values to be sent to Pixastic.
         *
         * @returns Promise
         * Returns a promise that can be used to run additional code after the processing has completed.
         * Also puts the processed image into self.dom.processedImage.
         */
        adjustmentProcessExecuteSingle: function(operationName, operationValue) {
            var deferred, self;

            self = this;

            deferred = $.Deferred();

            // Run the Pixastic process to create a new image, then mark the deferred as resolved when it completes
            Pixastic.process(self.dom.processedImage, operationName, operationValue, function(newImage) {

                // Save the processed image so additional operations can be performed on it
                self.dom.processedImage = newImage;
                    
                // Resolve the deferred object to indicate it is done
                deferred.resolve();
            });

            // Return a promise so additional code can run after the processing is done
            // to let us chain one operation after another
            return deferred.promise();
        },
        
        
        /**
         * Create hidden inputs for each of the adjustment values.
         */
        adjustmentSetInputs: function() {

            var self;

            self = this;
            
            // Remove any of the previous hidden inputs for the adjustments
            self.dom.$editInputs.empty();

            self.dom.$edit.find(':input').each(function() {
                
                var $input = $(this);

                // Determine if this is a selected checkbox, or an input that is not a checkbox
                if (!$input.is(':checkbox') || $input.is(':checked')) {

                    // Create a hidden input to store the value
                    self.dom.$editInputs.append($('<input/>', {
                        'type': 'hidden',
                        'name': $input.attr('name'),
                        'value': $input.val()
                    }));
                }
            });

        },

        
        /**
         * Get the Pixastic operations needed for the image adjustment.
         * @return Object
         * An object with multiple operations that should be performed by Pixastic.
         * This object is also saved in this.operations.
         */
        adjustmentGetOperations: function() {
            
            var self;
            
            self = this;
            
            // Create a list of operations
            self.operations = {};

            // Loop through all the inputs in the edit section
            // and add operations for each one
            self.dom.$edit.find(":input").each(function(){
                
                var $input, name, value, processFunction, processFunctionName;
                
                $input = $(this);
                
                // Get the name of the input and remove the extra junk
                // to just get the part at the end like "brightness"
                name = $input.attr('name') || '';
                name = /([^.]+)$/.exec(name);
                name = name ? name[1] : '';

                if (!name) {
                    return;
                }
                
                // Get the value of the input
                value = $input.is(':checkbox') ? $input.is(':checked') : parseFloat($input.val());

                // Skip this if the checkbox is not checked, or if the value was invalid
                if (value === false || isNaN(value)) {
                    return;
                }

                // Get the function that will be used to process this adjustment
                // such as self.adjustmentProcess_brightness
                processFunctionName = 'adjustmentGetOperation_' + name;
                if (!self[processFunctionName]) {
                    return;
                }

                // Call the process function and pass in the value.
                // Each function will add to the self.operations object.
                self[processFunctionName](value);
            });

            // All the operations are now in self.operations,
            // but we'll go ahead and pass them as a return value
            return self.operations;
        },
        
        adjustmentGetOperation_brightness: function(value) {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.brightness = operations.brightness || {};
            operations.brightness.brightness = value * 150;
            operations.brightness.legacy = true;
        },
        
        adjustmentGetOperation_contrast: function(value) {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.brightness = operations.brightness || { };
            operations.brightness.contrast = value < 0 ? value : value * 3;
        },
        
        adjustmentGetOperation_flipH: function() {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.fliph = operations.fliph || { };
        },
        
        adjustmentGetOperation_flipV: function() {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.flipv = operations.flipv || { };
        },
        
        adjustmentGetOperation_grayscale: function() {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.desaturate = operations.desaturate || { };
        },
        
        adjustmentGetOperation_sepia: function() {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.sepia = operations.sepia || { };
        },
        
        adjustmentGetOperation_invert: function() {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.invert = operations.desaturate || { };
        },
        
        adjustmentGetOperation_rotate: function(value) {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.rotate = operations.rotate|| { };
            operations.rotate.angle = -value;
        },
        
        adjustmentGetOperation_sharpen: function(value) {
            var operations, self;
            self = this;
            operations = self.operations;
            operations.sharpen = operations.sharpen || { };
            operations.sharpen.amount = value;
        },
        
        adjustmentGetOperation_blur: function(value) {
            var operations, rect, self, values;
            self = this;
            operations = self.operations;
            operations.blurfast = operations.blurfast || {
                count: 0,
                data: []
            };
            values = $input.val().split("x");
            rect = {
                left: values[0],
                top: values[1],
                width: values[2],
                height: values[3]
            };
            operations.blurfast.data[operations.blurfast.count] = {"amount" : 1.0, "rect" :rect};
            operations.blurfast.count++;
        },

        
        //--------------------------------------------------
        // BLUR ADJUSTMENT
        //--------------------------------------------------
        
        adjustmentBlurShow: function() {

            var self;

            self = this;
            
            // Check if there are hidden inputs with ".blur", which will contain
            // dimensions for an overlay box to blur part of the image
            self.dom.$edit.find("input[name=\'" + self.dataName + ".blur\']").each(function(){
                var attributes = $(this).val().split("x");
                // TODO: change addSizeBox
                addSizeBox(this, attributes[0], attributes[1], attributes[2], attributes[3]);
            });
        },

        
        //--------------------------------------------------
        // COVER
        // Used to indicate the crop area for a particular size.
        // These divs darken the outside of the crop area.
        // There is only one set of cover divs, which are readjusted each time
        // a size group is selected.
        //--------------------------------------------------

        /**
         * Create the cover divs.
         */
        initCover: function() {

            var $cover, self;

            self = this;
            
            $cover = $('<div/>', {
                'class': 'imageEditor-cover',
                'css': {
                    'display': 'none',
                    'left': '0',
                    'position': 'absolute',
                    'top': '0'
                }
            });

            self.dom.$coverTop = $cover;
            self.dom.$coverLeft = $cover.clone(true);
            self.dom.$coverRight = $cover.clone(true);
            self.dom.$coverBottom = $cover.clone(true);
            self.dom.$covers = $().add(self.dom.$coverTop).add(self.dom.$coverLeft).add(self.dom.$coverRight)
                .add(self.dom.$coverBottom).appendTo(self.$element);
        },

        
        /**
         * Update the size and position of the cover.
         *
         * @param Object bounds
         * @param Number bounds.top
         * @param Number bounds.left
         * @param Number bounds.width
         * @param Number bounds.height
         */
        coverUpdate: function(bounds) {
            
            var self, imageWidth, imageHeight, boundsRight, boundsBottom;

            self = this;
            
            imageWidth = self.dom.$image.width();
            imageHeight = self.dom.$image.height();
            boundsRight = bounds.left + bounds.width;
            boundsBottom = bounds.top + bounds.height;

            self.dom.$coverTop.css({
                'height': bounds.top,
                'width': imageWidth
            });
            self.dom.$coverLeft.css({
                'height': bounds.height,
                'top': bounds.top,
                'width': bounds.left
            });
            self.dom.$coverRight.css({
                'height': bounds.height,
                'left': boundsRight,
                'top': bounds.top,
                'width': imageWidth - boundsRight
            });
            self.dom.$coverBottom.css({
                'height': imageHeight - boundsBottom,
                'top': boundsBottom,
                'width': imageWidth
            });

            self.coverShow();
        },

        
        /**
         * Hide the cover.
         */
        coverHide: function() {
            var self;
            self = this;
            self.dom.$covers.hide();
        },

        
        /**
         * Show the cover.
         */
        coverShow: function() {
            var self;
            self = this;
            self.dom.$covers.show();
        },

        
        //--------------------------------------------------
        // SIZES
        //--------------------------------------------------

        initSizes: function() {
            
            var self;

            self = this;

            // Extract all the info about sizes from the DOM
            // After this self.sizeInfos and self.sizeGroups should be available
            self.sizesGetSizeInfo();

            // Create a list to hold all the size groups
            self.dom.$sizeSelectors = $('<ul/>', { 'class': 'imageEditor-sizeSelectors' });

            // Loop through all the sizeGroups info and set up the inferface for selecting thumbnails
            $.each(self.sizeGroups, function(groupName, groupInfo) {

                var groupLabel, $groupElement, addBreak;

                // Create a new list item for this size group
                $groupElement = $('<li/>', {
                    'class': 'imageEditor-sizeButton'
                }).appendTo(self.dom.$sizeSelectors);

                // Save the group name on the group element, so we can retrieve it later on
                $groupElement.attr('data-group-name', groupName);
                
                // Save the group LI for later use
                groupInfo.$element = $groupElement;
                
                // We'll get the label for the group by combining all the individual size descriptions
                groupLabel = $('<span/>', {
                    'class': 'imageEditor-sizeLabel'
                }).appendTo($groupElement);
                
                // Save the group label so we can use it later when creating the "Add Text" link
                groupInfo.$groupLabel = groupLabel;

                // Loop through all the individual sizes within the group
                $.each(groupInfo.sizeInfos, function(sizeName, sizeInfo) {

                    // Determine if we need to have a line break before this label.
                    // addBreak will be undefined on the first loop so we will not add a break,
                    // but on subsequent loops it will be true.
                    if (addBreak) {
                        groupLabel.append('<br/>');
                    } else {
                        addBreak = true;
                    }

                    $('<span/>', {
                        title: sizeInfo.description,
                        html: sizeInfo.description
                    }).appendTo(groupLabel);

                });

                // Create the preview image for the group
                $('<div/>', {
                    'class': 'imageEditor-sizePreview',
                    'html': $('<img/>', {
                        'alt': groupLabel.text(),
                        'src': self.dom.imageSrc,
                        'load': function() {
                            // After the preview image loads, update the cropping based on the size info
                            self.sizesUpdatePreview(groupName);
                        }
                    })
                }).appendTo($groupElement);

                // Add click event to the size group
                $groupElement.on('click', function(event) {

                    // Toggle the selected state for the size group
                    if (self.sizesIsSelected(groupName)) {
                        self.sizesUnselect();
                    } else {
                        self.sizesSelect(groupName);
                    }
                });

                // Create a size box for the group (initially hidden)
                self.initSizeBox(groupName);

            });

            // Now add the size selector to the dom,
            // and hide the table that contained all the size inputs
            self.dom.$sizes.before(self.dom.$sizeSelectors).hide();

        },

        
        /**
         * Search the DOM to get all the size information.
         * Combine sizes with similar aspect ratios into groups.
         * When this is done you should have two sets of information:
         * self.sizeInfos and self.sizeGroups
         */
        sizesGetSizeInfo: function() {
            
            var groupsApproximate, self;

            self = this;

            // We want to group together sizes that have approximately the same aspect ratio.
            // So we will create a list of groups based on an approximate aspect ratio.
            //
            // Key = an aspect ratio
            // Value = a group name from self.sizeGroups
            //
            // When we find an aspect ratio we will create multiple keys:
            // aspect, +/- 0.1, +/- 0.2
            //
            // So there will be multiple aspect ratios listed for a single group name.
            // This is the most efficient way of finding aspect ratios that are close in size.
            //
            // Example:
            // {
            //   '1.18': 'thumb_109x73',
            //   '1.19': 'thumb_109x73',
            //   '1.2': 'thumb_109x73',
            //   '1.21': 'thumb_109x73',
            //   '1.22': 'thumb_109x73',
            // }
            groupsApproximate = { };
            
            // Loop through all the TH elements. These are the names of the sizes.
            // From there we can get to the other information about the size.
            self.dom.$sizes.find('th').each(function(){
                
                var group, independent, inputs, sizeAspectRatio, sizeAspectRatioApproximate,
                    sizeDescription, sizeHeight, sizeInfo, sizeName, sizeWidth, sizes, $source, $th, $tr;

                $th = $(this);
                $tr = $th.closest('tr');
                sizeName = $tr.attr('data-size-name');
                sizeDescription = $th.text();
                independent = $tr.attr('data-size-independent') === 'true';
                sizeWidth = parseFloat($tr.attr('data-size-width'));
                sizeHeight = parseFloat($tr.attr('data-size-height'));
                sizeAspectRatio = sizeWidth / sizeHeight;

                // If we are inside a popup, only make this size selectable
                // if it a "standard image size" for the page it is on.
                $source = $th.popup('source');
                if ($source) {
                    var sizes = $source.closest('.inputContainer').attr('data-standard-image-sizes');
                    if (sizes) {
                        if ($.inArray(sizeName, sizes.split(' ')) < 0) {
                            // skip this and continue with the next item in the each loop
                            return;
                        }
                    }
                }

                // Get all the hidden inputs for the size
                inputs = { };
                $.each(self.inputNames, function(index, name) {
                    inputs[name] = $tr.find(':input[name$=".' + name + '"]');
                });

                // Save the size information so we can use it later
                sizeInfo = self.sizeInfos[sizeName] = {
                    name: sizeName,
                    description: sizeDescription,
                    inputs: inputs,
                    independent: independent,
                    width: sizeWidth,
                    height: sizeHeight,
                    aspectRatio: sizeAspectRatio
                };

                // Group the sizes according to aspect ratio
                // (unless this size is marked as an independent size which should be presented on its own)
                
                if (independent) {
                    
                    // Create a new group just for this individual item
                    group = self.sizesCreateGroup(sizeName);

                } else {

                    // Calculate an approximate aspect ratio for this size, down to two decimal places
                    sizeAspectRatioApproximate = Math.floor(sizeAspectRatio * 100) / 100;

                    // If there is not an existing group for this aspect ratio create a new group
                    group = groupsApproximate[ sizeAspectRatioApproximate ];
                    if (!group) {

                        // Create a new group for this aspect ratio
                        group = self.sizesCreateGroup( sizeAspectRatioApproximate );

                        // Also group together other aspect ratios that are nearly the same
                        // (making sure not to overwrite if already existing, not likely but just in case)
                        groupsApproximate[ sizeAspectRatioApproximate ] = group;
                        groupsApproximate[sizeAspectRatioApproximate - 0.02] = groupsApproximate[sizeAspectRatioApproximate - 0.02] || group;
                        groupsApproximate[sizeAspectRatioApproximate - 0.01] = groupsApproximate[sizeAspectRatioApproximate - 0.01] || group;
                        groupsApproximate[sizeAspectRatioApproximate + 0.01] = groupsApproximate[sizeAspectRatioApproximate + 0.01] || group;
                        groupsApproximate[sizeAspectRatioApproximate + 0.02] = groupsApproximate[sizeAspectRatioApproximate + 0.02] || group;
                    }
                }
                
                // Now add the current size to the group
                group.sizeInfos[sizeName] = sizeInfo;

            }); // END find th elements

            // Finished: self.sizeInfos and self.sizeGroups objects are now available with all sizing info
        },


        /**
         * Select a size to let the user edit it.
         *
         * @param String groupName
         */
        sizesSelect: function(groupName) {
            
            var self;

            self = this;

            // Unselect any other size groups
            self.sizesUnselect();

            // Mark the size selector so it appears selected
            self.sizeGroups[groupName].$element.addClass('imageEditor-sizeSelected');

            // Show the size box for this group
            self.sizeBoxShow(groupName);

            // Show the text overlays for this group
            self.textSelectGroup(groupName);
        },

        
        /**
         * Unselect the currently selected size.
         */
        sizesUnselect: function() {

            var self;

            self = this;
            
            self.dom.$sizeSelectors.find('li').removeClass('imageEditor-sizeSelected');

            self.sizeBoxHide();

            // Remove the "Add Text" button
            self.textUnselect();
        },

        
        /**
         * Determine if a group of sizes is currently selected.
         *
         * @param String groupName
         *
         * @returns Boolean
         */
        sizesIsSelected: function(groupName) {
            
            var self;

            self = this;

            return self.sizeGroups[groupName].$element.hasClass('imageEditor-sizeSelected');
        },

        
        /**
         * If there is only a single size group, select it.
         */
        sizesSelectSingle: function() {
            var self;
            self = this;
            if (self.sizeGroups.length === 1) {
                $.each(self.sizeGroups, function(groupName) {
                    self.sizesSelect(groupName);
                    return false;
                });
            }
        },

        
        /**
         * Determine which size group is currently selected.
         *
         * @returns String
         * Returns the group name of the currently selected size, or a blank string if none is selected.
         */
        sizesGetSelected: function() {
            
            var self;
            self = this;
            return self.dom.$sizeSelectors.find('.imageEditor-sizeSelected').attr('data-group-name') || '';
        },

        
        /**
         * Creates a new group of sizes.
         *
         * @param String groupName
         * The name to use for the group. This can be the size name or an aspect ratio.
         */
        sizesCreateGroup: function(groupName) {
            
            var self;

            self = this;

            return self.sizeGroups[groupName] = {
                $element: $(),
                sizeInfos: {}
            };
        },


        /**
         * Update the preview image for a single size group.
         *
         * @param String groupName
         * The name of the group you are resizing.
         */
        sizesUpdatePreview: function(groupName) {
            
            var bounds, groupInfo, $imageWrapper, self, sizeInfoFirst;

            self = this;

            // Get the group info from the group name
            groupInfo = self.sizeGroups[groupName];

            // Get the sizeInfo from the first size in the group
            sizeInfoFirst = self.sizesGetGroupFirstSizeInfo(groupName);
            
            // Find the element wrapping the preview image
            $imageWrapper = groupInfo.$element.find('.imageEditor-sizePreview');
            
            // Temporarily add the original image to the document because the image
            // must be in the DOM for us to retrieve the size
            self.dom.$imageClone.appendTo(document.body);

            // Get the current crop dimensions for this group
            bounds = self.sizesGetSizeBounds(self.dom.$imageClone, sizeInfoFirst);

            // Now remove the temporary image from the document
            self.dom.$imageClone.remove();
            
            // Crop the image based on the current crop dimension,
            // then replace the thumbnail image with the newly cropped image
            Pixastic.process(self.dom.imageClone, 'crop', bounds, function(newImage) {
                $imageWrapper.empty().append(newImage);
            });
        },


        /**
         * For an image and an individual size, get the current crop dimension information.
         *
         * @param Element|jQuery image
         * The image. Note this image must be in the DOM (or the image dimensions will be incorrect).
         *
         * @returns Object bounds
         * An object of size bounds, consisting of the following parameters:
         * @returns Number bounds.left
         * @returns Number bounds.top
         * @returns Number bounds.width
         * @returns Number bounds.height
         */
        sizesGetSizeBounds: function(image, sizeInfo) {
            
            var aspectRatio, height, $image, imageHeight, imageWidth, left, self, top, width;

            self = this;

            $image = $(image);
            
            imageWidth = $image.width();
            imageHeight = $image.height();

            left = parseFloat(sizeInfo.inputs.x.val()) || 0.0;
            top = parseFloat(sizeInfo.inputs.y.val()) || 0.0;
            width = parseFloat(sizeInfo.inputs.width.val()) || 0.0;
            height = parseFloat(sizeInfo.inputs.height.val()) || 0.0;
            aspectRatio = sizeInfo.aspectRatio;
            
            if (width === 0.0 || height === 0.0) {
                
                width = imageHeight * sizeInfo.aspectRatio;
                height = imageWidth / sizeInfo.aspectRatio;

                if (width > imageWidth) {
                    width = height * aspectRatio;
                } else {
                    height = width / aspectRatio;
                }

                left = (imageWidth - width) / 2;
                top = 0;

            } else {
                left *= imageWidth;
                top *= imageHeight;
                width *= imageWidth;
                height *= imageHeight;
            }

            return {
                'left': left,
                'top': top,
                'width': width,
                'height': height
            };
        },

        
        /**
         * For a size group, update all the inputs.
         *
         * @param String groupName
         * @param Object bounds
         * @param Number bounds.x
         * @param Number bounds.y
         * @param Number bounds.width
         * @param Number bounds.height
         */
        sizesSetGroupBounds: function(groupName, bounds) {

            var self, groupInfo;

            self = this;

            groupInfo = self.sizeGroups[groupName];

            $.each(groupInfo.sizeInfos, function() {
                
                var sizeInfo = this;

                $.each(bounds, function(inputName, value) {
                    sizeInfo.inputs[inputName].val(value);
                });
            });
        },

        
        /**
         * Get the first sizeInfo object for a size group.
         * @returns Object
         * The sizeInfo object for the first size in the group.
         */
        sizesGetGroupFirstSizeInfo: function(groupName) {
            var groupInfo, self, firstSize;
            self = this;
            groupInfo = self.sizeGroups[groupName];
            $.each(groupInfo.sizeInfos, function(sizeName, sizeInfo){
                firstSize = sizeInfo;
                return false; // return after first one
            });
            return firstSize;
        },

        
        /**
         * Get the aspect ratio for a group.
         * @returns Number
         * The aspect ratio of the first size in the group.
         */
        sizesGetGroupAspectRatio: function(groupName) {
            var self;
            self = this;
            return self.sizesGetGroupFirstSizeInfo(groupName).aspectRatio;
        },

        
        //--------------------------------------------------
        // SIZEBOX - showing and setting the cropped area of an image.
        // The sizebox cgan be dragged by clicking the box,
        // or resized by clicking the left/top handle
        // or the bottom/right handle.
        // Each size group creates a separate size box.
        // The size box will also contain other items such as
        // text overlays and hotspots.
        //--------------------------------------------------

        /**
         * Create the size box that shows the cropped area of an image,
         * and lets the user edit the cropped area.
         *
         * @param String groupName
         * The name of the group from self.sizeGroups for this size box.
         */
        initSizeBox: function(groupName) {
            
            var groupInfo, self, $sizeBox, $sizeBoxTopLeft, $sizeBoxBottomRight;

            self = this;

            groupInfo = self.sizeGroups[groupName];
            
            // Create the sizebox
            $sizeBox = $('<div/>', {
                'class': 'imageEditor-sizeBox',
                'css': { 'position': 'absolute' }
            }).hide().appendTo(self.$element);

            // Save the size box along with the group info so we can access it later
            groupInfo.$sizeBox = $sizeBox;
            
            // Create the top/left sizebox handle
            $sizeBoxTopLeft = $('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-topLeft',
            }).appendTo($sizeBox);

            // Create the top/right sizebox handle
            $sizeBoxBottomRight = $('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-bottomRight',
            }).appendTo($sizeBox);

            // Set up  event handlers
            
            // Event handler to support dragging the left/top handle
            $sizeBoxTopLeft.on('mousedown', self.sizeBoxMousedownDragHandler(groupName, function(event, original, delta) {
                
                // When user drags the top left handle,
                // adjust the top and left position of the size box,
                // and adjust the width and height
                return {
                    'left': original.left + delta.constrainedX,
                    'top': original.top + delta.constrainedY,
                    'width': original.width - delta.constrainedX,
                    'height': original.height - delta.constrainedY
                };
            }));

            // Event handler to support dragging the bottom/right handle
            $sizeBoxBottomRight.on('mousedown', self.sizeBoxMousedownDragHandler(groupName, function(event, original, delta) {
                
                // When user drags the bottom right handle, adjust the width and height of the size box
                return {
                    'width': original.width + delta.constrainedX,
                    'height': original.height + delta.constrainedY
                };
            }));

            // Event handler to support moving the size box
            $sizeBox.on('mousedown', self.sizeBoxMousedownDragHandler(groupName, function(event, original, delta) {
                
                // Set the "moving" parameter to prevent the size box from being moved
                // outside the bounds of the image, and adjust the left and top position
                return {
                    'moving': true,
                    'left': original.left + delta.x,
                    'top': original.top + delta.y
                };
                
            }));
        },

        
        /**
         * Show the size box and resize it to show the crop settings
         * for a particular group size.
         *
         * @param String groupName
         */
        sizeBoxShow: function(groupName) {
            
            var bounds, self, sizeInfo;
            
            self = this;

            // Get the first sizeInfo object for this group
            sizeInfo = self.sizesGetGroupFirstSizeInfo(groupName);

            // Resize the size box for this group
            bounds = self.sizesGetSizeBounds(self.dom.$image, sizeInfo);
            
            self.coverUpdate(bounds);
            self.coverShow();
            
            self.sizeBoxUpdate(groupName, bounds);

            self.sizeGroups[groupName].$sizeBox.show();
        },


        /**
         * Set the bounds for the size box.
         * @param Object bounds
         * Object that contains CSS settings for the size box:
         * @param Object top
         * @param Object left
         * @param Object width
         * @param Object height
         */
        sizeBoxUpdate: function(groupName, bounds) {
            var self;
            self = this;
            self.sizeGroups[groupName].$sizeBox.css(bounds);
        },

        
        /**
         * Hide all the size boxes.
         */
        sizeBoxHide: function() {
            var self;
            self = this;
            self.coverHide();
            $.each(self.sizeGroups, function(groupName, groupInfo) {
                groupInfo.$sizeBox.hide();
            });
        },

        
        /**
         * Create a mousedown handler function that lets the user drag the size box
         * or the size box handles.
         *
         * @param String groupName
         * Name of the size group that we are modifying.
         *
         * @param Function filterBoundsFunction(event, original, delta)
         * A function that will modify the bounds of the size box,
         * and adjust it according to what is being dragged. For example,
         * if the left/top handle is being dragged.
         * Ths function must return a modified bounds object.
         * Also the function can set moving:true in the bounds object if
         * the entire size box is being moved (instead of resizing the size box).
         *
         */
        sizeBoxMousedownDragHandler: function(groupName, filterBoundsFunction) {

            var mousedownHandler, self, $sizeBox;

            self = this;

            $sizeBox = self.sizeGroups[groupName].$sizeBox;
            
            mousedownHandler = function(mousedownEvent) {

                var aspectRatio, element, imageWidth, imageHeight, original, sizeBoxPosition;

                // The element that was dragged
                element = this;

                // Get the aspect ratio for this group
                aspectRatio = self.sizesGetGroupAspectRatio(groupName);

                sizeBoxPosition = $sizeBox.position();
                
                original = {
                    'left': sizeBoxPosition.left,
                    'top': sizeBoxPosition.top,
                    'width': $sizeBox.width(),
                    'height': $sizeBox.height(),
                    'pageX': mousedownEvent.pageX,
                    'pageY': mousedownEvent.pageY
                };

                imageWidth = self.dom.$image.width();
                imageHeight = self.dom.$image.height();

                // Get the aspect ratio 
                // On mousedown, let the user start dragging the element
                // The .drag() function takes the following parameters:
                // (element, event, startCallback, moveCallback, endCallback)
                $.drag(element, mousedownEvent, function(dragEvent) {
                    
                    // This is the start callback for .drag()
                    
                }, function(dragEvent) {
                    
                    // This is the move callback for .drag()

                    var bounds, deltaX, deltaY, overflow;
                    
                    deltaX = dragEvent.pageX - original.pageX;
                    deltaY = dragEvent.pageY - original.pageY;

                    // Use the filterBoundsFunction to adjust the value of the bounds
                    // based on what is being dragged.
                    bounds = filterBoundsFunction(dragEvent, original, {
                        'x': deltaX,
                        'y': deltaY,
                        'constrainedX': Math.max(deltaX, deltaY * aspectRatio),
                        'constrainedY': Math.max(deltaY, deltaX / aspectRatio)
                    });

                    // Fill out the missing bounds
                    bounds = $.extend({}, original, bounds);

                    // The sizebox can be resized or moved.
                    // The filterBoundsFunction should have told us if it is being moved.
                    if (bounds.moving) {

                        // The sizebox is being moved,
                        // but we can't let it move outside the range of the image.

                        if (bounds.left < 0) {
                            bounds.left = 0;
                        }

                        if (bounds.top < 0) {
                            bounds.top = 0;
                        }

                        overflow = bounds.left + bounds.width - imageWidth;
                        if (overflow > 0) {
                            bounds.left -= overflow;
                        }

                        overflow = bounds.top + bounds.height - imageHeight;
                        if (overflow > 0) {
                            bounds.top -= overflow;
                        }

                    } else {
                        
                        // We're not moving the sizebox so we must be resizing.
                        // We still need to make sure we don't resize past the boundaries of the image.
                        
                        if (bounds.width < 10 || bounds.height < 10) {
                            if (aspectRatio > 1.0) {
                                bounds.width = aspectRatio * 10;
                                bounds.height = 10;
                            } else {
                                bounds.width = 10;
                                bounds.height = 10 / aspectRatio;
                            }
                        }

                        if (bounds.left < 0) {
                            bounds.width += bounds.left;
                            bounds.height = bounds.width / aspectRatio;
                            bounds.top -= bounds.left / aspectRatio;
                            bounds.left = 0;
                        }

                        if (bounds.top < 0) {
                            bounds.height += bounds.top;
                            bounds.width = bounds.height * aspectRatio;
                            bounds.left -= bounds.top * aspectRatio;
                            bounds.top = 0;
                        }

                        overflow = bounds.left + bounds.width - imageWidth;
                        if (overflow > 0) {
                            bounds.width -= overflow;
                            bounds.height = bounds.width / aspectRatio;
                        }

                        overflow = bounds.top + bounds.height - imageHeight;
                        if (overflow > 0) {
                            bounds.height -= overflow;
                            bounds.width = bounds.height * aspectRatio;
                        }
                    }

                    // Now that the bounds have been sanitized,
                    // update the sizebox display
                    self.coverUpdate(bounds);
                    self.sizeBoxUpdate(groupName, bounds);

                    // Trigger an event to tell others the size box has changed size
                    if (!bounds.moving) {
                        $sizeBox.trigger('sizeBoxResize');
                    }

                }, function() {

                    // .drag() end callback

                    // Now that we're done dragging, update the size box
                    
                    var sizeBoxPosition = $sizeBox.position();
                    var sizeBoxWidth = $sizeBox.width();
                    var sizeBoxHeight = $sizeBox.height();

                    // Set the hidden inputs to the current bounds.
                    self.sizesSetGroupBounds(groupName, {
                        x: sizeBoxPosition.left / imageWidth,
                        y: sizeBoxPosition.top / imageHeight,
                        width: sizeBoxWidth / imageWidth,
                        height: sizeBoxWidth / aspectRatio / imageHeight
                    });
                    
                    // Update the preview image thumbnail so it will match the new crop values
                    self.sizesUpdatePreview(groupName);

                    // Trigger an event to tell others the size box has changed size
                    $sizeBox.trigger('sizeBoxResize');
                });

                return false;
            };

            return mousedownHandler;
        },

        
        //--------------------------------------------------
        // SIZES - SET FOCUS FOR CROPPING
        //--------------------------------------------------
        
        //--------------------------------------------------
        // SIZES - TEXT OVERLAYS
        //--------------------------------------------------

        /**
         * Initialize all the text overlays.
         */
        initText: function() {

            var self;

            self = this;
            
            // Create text overlays within the size box for each group
            $.each(self.sizeGroups, function(groupName, groupInfo) {

                // Get the text info from the hidden inputs
                groupInfo.textInfos = self.textGetTextInfo(groupName);

                // Create the text overlays
                $.each(groupInfo.textInfos, function(textKey) {
                    self.textOverlayAdd(groupName, textKey);
                });

                // Set up resize listeners on the sizebox
                // to resize the text overlays if the sizebox changes
                groupInfo.$sizeBox.on('sizeBoxResize', function(){
                    self.textOverlaySetFont(groupName);
                });
            });

            // Set up a form submit handler to copy all the rich text editor content back into the hidden variables
            self.dom.$form.submit(function() {
                $.each(self.sizeGroups, function(groupName, groupInfo) {
                    self.textSetTextInfo(groupName);
                });
            });

        },

        
        textSelectGroup: function(groupName) {

            var self, textInfo;

            self = this;

            // Create the "Add Text" button within the group
            self.textButtonCreate(groupName);

            // Resize the texts within the group
            self.textOverlaySetFont(groupName);
        },


        /**
         * Removes the "Add Text" link from within all groups.
         */
        textUnselect: function() {
            
            var self;

            self = this;

            // Remove the "Add Text" link
            self.textButtonRemove();
            
        },

        
        /**
         * Create the "Add Text" button and add it to the group selector.
         * Set up an event to add a new text when the button is clicked.
         *
         * @param String groupName
         */
        textButtonCreate: function(groupName) {
            
            var self;

            self = this;

            $('<a/>', {
                'class': 'imageEditor-addTextOverlay',
                'text': 'Add Text',
                'click': function() {
                    self.textAdd(groupName);
                    return false;
                }
            }).insertAfter( self.sizeGroups[groupName].$groupLabel );
        },

        
        /**
         * Remove the 'Add Text' button from all size groups.
         */
        textButtonRemove: function() {
            var self;
            self = this;
            self.$element.find('.imageEditor-addTextOverlay').remove();
        },

        
        /**
         * Get the text information for a group from the hidden inputs,
         * then transform it from multiple delimited strings into more usable javascript format.
         *
         * @param String groupName
         * Name of the sizes group that contains the text.
         * 
         * @returns Object textInfo
         * @returns Object textInfo[n]
         * @returns String textInfo[n].text
         * @returns Number textInfo[n].x
         * @returns Number textInfo[n].y
         * @returns Number textInfo[n].width
         * @returns Number textInfo[n].size
         */
        textGetTextInfo: function(groupName) {

            var groupInfo, self, sizeInfo, textInfo;

            self = this;

            groupInfo = self.sizeGroups[groupName];
            sizeInfo = self.sizesGetGroupFirstSizeInfo(groupName);
            
            // If there are no texts return an empty array
            if (!sizeInfo.inputs.texts.val()) {
                return {};
            }
            
            // Convert the delimited strings to arrays of values
            texts = sizeInfo.inputs.texts.val().split(self.textDelimiter);
            textXs = sizeInfo.inputs.textXs.val().split(self.textDelimiter);
            textYs = sizeInfo.inputs.textYs.val().split(self.textDelimiter);
            textWidths = sizeInfo.inputs.textWidths.val().split(self.textDelimiter);
            textSizes = sizeInfo.inputs.textSizes.val().split(self.textDelimiter);

            textInfo = {};

            // Use a number as a unique key in the textInfo object.
            // The items are not ordered because we need the flexibility to easily add and remove texts.
            if (!self.textInfoIndex) {
                self.textInfoIndex = 1;
            }
            
            // Now loop through the individual arrays and add them to the textInfo object.
            $.each(texts, function(index, text) {
                
                // Skip the first item because the delimited string starts with the delimeter
                // and we end up with an empty item as the first array item.
                if (index === 0) {
                    return;
                }

                textInfo[ self.textInfoIndex++ ] = {
                    text: texts[index],
                    x: textXs[index],
                    y: textYs[index],
                    width: textWidths[index],
                    size: textSizes[index],
                };
            });

            return textInfo;
        },


        /**
         * Save text information into the hidden variables for all the sizes within a group.
         *
         * @param String groupName
         * Name of the sizes group to update.
         */
        textSetTextInfo: function(groupName) {
            
            var self;

            self = this;

            // Loop through all the sizes in the group because we need to update
            // hidden inputs in each one.
            $.each(self.sizeGroups[groupName].sizeInfos, function(sizeName, sizeInfo) {

                var texts, textXs, textYs, textWidths, textSizes;

                texts = '';
                textXs = '';
                textYs = '';
                textWidths = '';
                textSizes = '';
                
                // Loop  through all the text blocks within this group
                $.each(self.sizeGroups[groupName].textInfos, function(textInfoKey, textInfo) {

                    var $rteInput;

                    // Update the text from the rich text editor
                    $rteInput = textInfo.$textOverlay.find('.imageEditor-textOverlayInput');
                    if ($rteInput.length) {
                        textInfo.text = $rteInput.val();
                    }
                    
                    texts += self.textDelimiter + textInfo.text;
                    textXs += self.textDelimiter + textInfo.x;
                    textYs += self.textDelimiter + textInfo.y;
                    textWidths += self.textDelimiter + textInfo.width;
                    textSizes += self.textDelimiter + textInfo.size;
                });

                // Save the concatenated delimted strings to the hidden variables
                sizeInfo.inputs.texts.val(texts || '');
                sizeInfo.inputs.textXs.val(textXs || '');
                sizeInfo.inputs.textYs.val(textYs || '');
                sizeInfo.inputs.textWidths.val(textWidths || '');
                sizeInfo.inputs.textSizes.val(textSizes || '');
            });
        },

        
        /**
         * Create a new text for a size group.
         *
         * @param String groupName
         */
        textAdd: function(groupName) {

            var groupInfo, self, textInfoKey;

            self = this;

            groupInfo = self.sizeGroups[groupName];
            
            // Create a new text object
            textInfoKey = self.textInfoIndex++;
            
            groupInfo.textInfos[textInfoKey] = {
                text:'',
                x:0,
                y:0,
                width:0,
                size:0
            };

            // Create a new text overlay and focus on it
            self.textOverlayAdd(groupName, textInfoKey, true);
            
            // Update the hidden variables so the new text will be saved
            self.textSetTextInfo(groupName, groupInfo.textInfos);
        },


        /**
         * Create a new text overlay on top of the image.
         *
         * @param String groupName
         * The size group to which the text belongs.
         *
         * @param String textInfoKey
         * The key that indexes into the selectedGroupTextInfo object to give the textInfo.
         * 
         * @param Boolean focus
         * Set to true if we should focus on the text input after creating the text.
         */

        textOverlayAdd: function(groupName, textInfoKey, focus) {

            var groupInfo, self, $sizeBox, $textOverlay, $textOverlayInput, $textOverlayLabel;

            self = this;

            groupInfo = self.sizeGroups[groupName];

            textInfo = groupInfo.textInfos[textInfoKey];
            if (!textInfo) {
                return;
            }

            $sizeBox = groupInfo.$sizeBox;
            
            $textOverlay = $('<div/>', {
                'class': 'imageEditor-textOverlay',
                'css': {
                    'left': ((textInfo.x || 0.25) * 100) + '%',
                    'position': 'absolute',
                    'top': ((textInfo.y || 0.25) * 100) + '%',
                    'width': ((textInfo.width || 0.50) * 100) + '%',
                    'z-index': 1
                }
            }).appendTo($sizeBox);

            // Add some data to the overlay element so it can be used later in event handlers
            // to update the text info.
            $textOverlay.data('textInfoKey', textInfoKey)
            $textOverlay.data('groupName', groupName);

            // Also save the overlay element within the data for the textInfo,
            // so we can retrieve the new value later
            textInfo.$textOverlay = $textOverlay;
            
            // Add the label to drag the text overlay
            $textOverlayLabel = $('<div/>', {
                'class': 'imageEditor-textOverlayLabel',
                'text': 'Text #' + textInfoKey
            }).on('mousedown', self.textMousedownDragHandler(function(event, original, delta) {
                return {
                    'moving': true,
                    'left': original.left + delta.x,
                    'top': original.top + delta.y
                };
            })).appendTo($textOverlay);

            // Add resize handle to change the left border
            $('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-left',
                'mousedown': self.textMousedownDragHandler(function(event, original, delta) {
                    return {
                        'left': original.left + delta.x,
                        'width': original.width - delta.x
                    };
                })
            }).appendTo($textOverlay);

            // Add resize handle to change the right border.
            $('<div/>', {
                'class': 'imageEditor-resizer imageEditor-resizer-right',
                'mousedown': self.textMousedownDragHandler(function(event, original, delta) {
                    return {
                        'left': original.left,
                        'width': original.width + delta.x
                    };
                })
            }).appendTo($textOverlay);

            // Add the remove button
            $textOverlayRemove = $('<div/>', {
                'class': 'imageEditor-textOverlayRemove',
                'text': 'Remove',
                'click': function(event) {
                    $textOverlay.remove();
                    self.textOverlayRemove(groupName, textInfoKey);
                    return false;
                }
            }).appendTo($textOverlay);
            
            // Add the text input and rich text editor
            $textOverlayInput = $('<input/>', {
                'class': 'imageEditor-textOverlayInput',
                'type': 'text',
                'value': textInfo.text || ''
            }).appendTo($textOverlay);

            // Activate rich text editor on the input
            $textOverlayInput.rte({
                'initImmediately': true,
                'useLineBreaks': true
            });

            // Move the rich text toolbacr controls to on top of the image
            // TODO: there appears to be a bug here where multiple text inputs
            // cause multiple toolbars to appear.
            // Need to find a way to show the toolbar only when the RTE has focus.
            self.$element.before($textOverlay.find('.rte-toolbar-container'));
            
            // Try to set the font size after the rich text editor loads
            // TODO: need a better way to do this, such as a ready event that the RTE fires
            var wait = 5000;
            
            var repeatResizeTextOverlayFont = function() {
                
                var $body = $( $textOverlay.find('.rte-container iframe')[0].contentDocument.body );
                if ($body.is('.rte-loaded')) {
                    self.textOverlaySetFont(groupName, textInfoKey);
                } else {
                    // The RTE isn't loaded.
                    // Try again after a delay, but give up after a certain number of tries.
                    wait -= 100;
                    if (wait > 0) {
                        setTimeout(repeatResizeTextOverlayFont, 100);
                    }
                }
            };

            repeatResizeTextOverlayFont();
            
            // Focus on the text input
            if (focus) {
                $textOverlayInput.focus();
            }

        },

        
        /**
         * Set the font on all text overlays that are visible,
         * or for a single text overlay.
         *
         * @param String [textInfoKey]
         * Optionally provide the textInfoKey for a single text info,
         * and it will be the only one that is resized.
         */
        textOverlaySetFont: function(groupName, singleTextInfoKey) {

            var groupInfo, self, $sizeBox;

            self = this;

            groupInfo = self.sizeGroups[groupName];
            $sizeBox = groupInfo.$sizeBox;
            
            // Don't do anything if the size box is not visible
            if (!$sizeBox.is(':visible')) {
                return;
            }

            var textSizes = '';

            // Loop through each text overlay within the sizebox
            $sizeBox.find('.imageEditor-textOverlay').each(function() {

                var newFontSize, originalFontSize, $rteBody, sizeHeight, $textOverlay, textInfoKey, textInfo, textSize;

                $textOverlay = $(this);

                textInfoKey = $textOverlay.data('textInfoKey');

                // If a single text was requested, only resize that one
                if (singleTextInfoKey && textInfoKey !== singleTextInfoKey) {
                    return;
                }
                
                textInfo = groupInfo.textInfos[ textInfoKey ];

                // Get the height of the selected group
                sizeHeight = self.sizesGetGroupFirstSizeInfo(groupName).height;
                
                // Check to see if we previously saved a font size
                originalFontSize = $textOverlay.data('imageEditor-originalFontSize');
                
                // Get the body of the rich text editor
                $rteBody = $( $textOverlay.find('.rte-container iframe')[0].contentDocument.body );

                if (!originalFontSize && $rteBody.is('.rte-loaded')) {
                    originalFontSize = parseFloat($rteBody.css('font-size'));
                    $.data(this, 'imageEditor-originalFontSize', originalFontSize);
                }

                if (originalFontSize > 0) {
                    textSize = 1 / sizeHeight * originalFontSize;
                    textInfo.size = textSize;
                    $rteBody.css('font-size', $sizeBox.height() * textSize);
                }
            });

            // Update the hiden variables
            self.textSetTextInfo(groupName);
        },

        
        textOverlayRemove: function(groupName, textInfoKey) {
            
            var self;
            
            self = this;

            delete self.sizeGroups[groupName].textInfos[textInfoKey];
            
            // Update the hidden variables
            self.textSetTextInfo(groupName);
        },
        
        
        textMousedownDragHandler: function(filterBoundsFunction) {
            
            var mousedownHandler, self;

            self = this;
            
            mousedownHandler = function(mousedownEvent) {

                var element, groupInfo, groupName, textOverlayPosition, original, $textOverlay, textInfoKey, $sizeBox, sizeBoxWidth, sizeBoxHeight;

                // The element that was dragged and the text overlay that contains it
                element = this;
                $textOverlay = $(element).closest('.imageEditor-textOverlay');

                // Get the data attribute for the textInfoKey so we can update the text info
                groupName = $textOverlay.data('groupName') || '';
                textInfoKey = $textOverlay.data('textInfoKey') || '';

                groupInfo = self.sizeGroups[groupName];
                $sizeBox = groupInfo.$sizeBox;
                
                textInfo = groupInfo.textInfos[textInfoKey];
                
                //resizeTextOverlayFont();

                // When user presses mouse down, get the current position of the
                // text overlay, plus the size of the sizeBox container
                // so we can constrain how much we allow it to be dragged.
                
                textOverlayPosition = $textOverlay.position();
                sizeBoxWidth = $sizeBox.width();
                sizeBoxHeight = $sizeBox.height();
                
                var original = {
                    'left': textOverlayPosition.left,
                    'top': textOverlayPosition.top,
                    'width': $textOverlay.width(),
                    'height': $textOverlay.height(),
                    'pageX': mousedownEvent.pageX,
                    'pageY': mousedownEvent.pageY
                };

                mousedownEvent.dragImmediately = true;

                $.drag(this, mousedownEvent, function(dragEvent) {

                    // This is the start callback for .drag()

                }, function(dragEvent) {

                    // This is the move callback for .drag()

                    var deltaX, deltaY, bounds;
                    
                    deltaX = dragEvent.pageX - original.pageX;
                    deltaY = dragEvent.pageY - original.pageY;

                    // Use the filterBoundsFunction to adjust the value of the bounds
                    // based on what is being dragged.
                    bounds = filterBoundsFunction(dragEvent, original, {
                        'x': deltaX,
                        'y': deltaY
                    });

                    // Fill out the missing bounds
                    bounds = $.extend({}, original, bounds);

                    // Prevent from moving outside the size box

                    if (bounds.left < 0) {
                        bounds.left = 0;
                    }

                    if (bounds.top < 0) {
                        bounds.top = 0;
                    }

                    if (bounds.top + original.height > sizeBoxHeight) {
                        bounds.top = sizeBoxHeight - original.height;
                    }
                    
                    if (bounds.moving) {
                        if (bounds.left + bounds.width > sizeBoxWidth) {
                            bounds.left = sizeBoxWidth - bounds.width;
                        }
                    } else {
                        
                        if (bounds.left + bounds.width > sizeBoxWidth) {
                            bounds.width = sizeBoxWidth - bounds.left;
                        }
                    }

                    // Save the bounds to the textInfo object
                    textInfo.x = bounds.left / sizeBoxWidth;
                    textInfo.y = bounds.top / sizeBoxHeight;
                    textInfo.width = bounds.width / sizeBoxWidth;

                    // Convert the bounds to percentage values and update the position of the text overlay
                    bounds.left = (textInfo.x * 100) + '%';
                    bounds.top = (textInfo.y * 100) + '%';
                    bounds.width = (textInfo.width * 100) + '%';
                    bounds.height = 'auto';
                    
                    $textOverlay.css(bounds);

                }, function() {
                    
                    // .drag() end callback
                    
                    // Now that we're done dragging, update the textInfo for this text,
                    // then update the hidden variables
                    self.textSetTextInfo(groupName, groupInfo.textInfos);
                    
                });

                return false;
            };

            return mousedownHandler;
        },

        
        //--------------------------------------------------
        // HOTSPOTS
        //--------------------------------------------------
        
        //--------------------------------------------------
        // MISC SUPPORT FUNCTIONS
        //--------------------------------------------------
        
        /**
         * Fix scrolling behavior of the "aside" scrollable area.
         * If user is scrolling the area and reaches the top or bottom,
         * do not let the browser start scrolling the entire page.
         */
        initScrollFix: function() {

            var self = this;

            self.dom.$aside.bind('mouswheel', function(event, delta, deltaX, deltaY) {
                if (self.elementIsScrolledToMax(this, deltaY)) {
                    event.preventDefault();
                }
            });
        },

        
        /**
         * Check if an element (with an internal scrollbar) is already scrolled to the top or the bottom.
         *
         * @param Element|jQuery element
         * The element that is being scrolled.
         *
         * @param Number deltaY
         * The number of pixels being scrolled.
         * This is typically retrieved from the mousewheel event.
         *
         * @returns Boolean
         * Return true if the element is already scrolled to the top.
         */
        elementIsScrolledToMax: function(element, deltaY) {

            var self, $el, attr, maxScrollTop, scrollTop;
            
            self = this;
            $el = $(element);
            
            // Name of the data attribute we will save on the element to remember the scroll position
            attr = 'imageEditor-maxScrollTop';

            // Get a previously saved scroll position
            maxScrollTop = $el.data(attr);

            // Get the current scroll position
            scrollTop = $el.scrollTop();
            
            // See if we previously saved the scroll position
            if (typeof maxScrollTop === 'undefined') {
                
                // We haven't saved the scroll position previously,
                // so we'll assume the current scroll position is the top
                maxScrollTop = $el.prop('scrollHeight') - $el.innerHeight();
                $el.data(attr, maxScrollTop);
            }

            // Check if we are already scrolled to the top or bottom
            if ((deltaY > 0 && scrollTop === 0) ||
                (deltaY < 0 && scrollTop >= maxScrollTop)) {
                return true;
            }

            return false;
        }

    }; // END imageEditorUtilty object

    
    // Whenever an element with class "imageEditor" is added to the page,
    // we create a new instance of the imageEditorUtility object and initialize it.
    
    bsp_utils.onDomInsert(document, '.imageEditor', {
        
        insert: function(element) {

            var imageEditor;
            
            // Create a copy of the repeatableUtility object
            // This uses prototypal inheritance so we're not actually copying the entire object
            // It allows each repeatable instance to have its own object that saves state
            imageEditor = Object.create(imageEditorUtility);
            
            // Initialize the image editor
            imageEditor.init(element);

            // Save the instance of the object on the element in case anyone wants to use it
            $(element).data('imageEditor', imageEditor);

        }
    });

    return imageEditorUtility;

}); // END define


/***======================================================================
    
    //var INPUT_NAMES = 'x y width height texts textSizes textXs textYs textWidths'.split(' ');
    //var DELIMITER = 'aaaf7c5a9e604daaa126f11e23e321d8';

    bsp_utils.onDomInsert(document, '.imageEditor', {
        'insert': function(element) {
            // var $editor = $(element);
            // var $form = $editor.closest('form');
            // var $image = $editor.find('.imageEditor-image img');
            // var imageSrc = $image.attr('src');
            // var $imageReSizeScale = $image.attr('data-scale') !== undefined && $image.attr('data-scale') !== "" ? $image.attr('data-scale') : 1.0;
            // var $originalImage = $image;
            // var $imageClone = $image.clone();
            // var imageClone = $imageClone[0];

            // $editor.find('> .imageEditor-aside').bind('mousewheel', function(event, delta, deltaX, deltaY) {
            //     var $aside = $(this),
            //             maxScrollTop = $.data(this, 'imageEditor-maxScrollTop');

            //     if (typeof maxScrollTop === 'undefined') {
            //         maxScrollTop = $aside.prop('scrollHeight') - $aside.innerHeight();
            //         $.data(this, 'imageEditor-maxScrollTop', maxScrollTop);
            //     }

            //     if ((deltaY > 0 && $aside.scrollTop() === 0) ||
            //             (deltaY < 0 && $aside.scrollTop() >= maxScrollTop)) {
            //         event.preventDefault();
            //     }
            // });

            // var $tools = $editor.find('.imageEditor-tools ul');
            // var $edit = $editor.find('.imageEditor-edit');
            // var $dataName = $editor.parents('.inputContainer').attr('data-name');
            
            // var $editButton = $('<li/>', {
            //     'html': $('<a/>', {
            //         'class': 'action-image-edit',
            //         'text': 'Edit Image',
            //         'click': function() {
            //             $edit.popup('source', $(this));
            //             $edit.popup('open');
                        
            //             // Check if there is a hidden input with ".blur", which will contain
            //             // dimensions for an overlay box ???
            //             $(".imageEditor-edit input[name=\'" + $dataName + ".blur\']").each(function(){
            //                 var attributes = $(this).val().split("x");
            //                 addSizeBox(this, attributes[0], attributes[1], attributes[2], attributes[3]);
            //             });
            //             return false;
            //         }
            //     })
            // });

            // $tools.prepend($editButton);

            // var $editInputs = $('<div/>');
            // $editButton.append($editInputs);

            // var previousOperations;
            // var processImageTimer;

            // var processImage = function() {
            //     var operations = { };

            //     var applyOperation = function($input) {
                    // var value = $input.is(':checkbox') ? $input.is(':checked') : parseFloat($input.val());

                    // if (value === false || isNaN(value)) {
                    //     return;
                    // }

                    // var name = /.([^.]+)$/.exec($input.attr('name'));
                    // name = name ? name[1] : null;

                    // if (name === 'brightness') {
                    //     operations.brightness = operations.brightness || { };
                    //     operations.brightness.brightness = value * 150;
                    //     operations.brightness.legacy = true;

                    // } else if (name === 'contrast') {
                    //     operations.brightness = operations.brightness || { };
                    //     operations.brightness.contrast = value < 0 ? value : value * 3;

                    // } else if (name === 'flipH') {
                    //     operations.fliph = operations.fliph || { };

                    // } else if (name === 'flipV') {
                    //     operations.flipv = operations.flipv || { };

                    // } else if (name === 'grayscale') {
                    //     operations.desaturate = operations.desaturate || { };

                    // } else if (name === 'invert') {
                    //     operations.invert = operations.invert || { };

                    // } else if (name === 'rotate') {
                    //     operations.rotate = operations.rotate|| { };
                    //     operations.rotate.angle = -value;

                    // } else if (name === 'sepia') {
                    //     operations.sepia = operations.sepia || { };

                    // } else if (name === 'sharpen') {
                    //     operations.sharpen = operations.sharpen || { };
                    //     operations.sharpen.amount = value;

                    // } else if (name === "blur") {
                    //     operations.blurfast = operations.blurfast || {"count" : 0, "data" : []};
                    //     var values = $input.val().split("x");
                    //     var rect = {"left" : values[0], "top" : values[1], "width" : values[2], "height" : values[3]};
                    //     operations.blurfast.data[operations.blurfast.count] = {"amount" : 1.0, "rect" :rect};
                    //     operations.blurfast.count++;
                    // }
                }

                
                // ??? Not sure why there are two separate calls here
                
                // $edit.find(":input[name$='.rotate']").each(function() {
                //     applyOperation($(this));
                // });

                // $edit.find(":input:not([name$='.rotate'])").each(function() {
                //     applyOperation($(this));
                // });

                //var serialized = JSON.stringify(operations);

                // if (serialized !== previousOperations) {
                    
                    // previousOperations = serialized;

                    // $editInputs.empty();
                    // $edit.find(':input').each(function() {
                    //     var $input = $(this);

                    //     if (!$input.is(':checkbox') || $input.is(':checked')) {
                    //         $editInputs.append($('<input/>', {
                    //             'type': 'hidden',
                    //             'name': $input.attr('name'),
                    //             'value': $input.val()
                    //         }));
                    //     }
                    // });

            //         var operationKeys = [ ];
            //         var operationKeyIndex = 0;

            //         for (var key in operations) {
            //             if (operations.hasOwnProperty(key)) {
            //                 operationKeys[operationKeys.length] = key;
            //             }
            //         }

            //         var operateItem = function(processedImage, index, key, operations, operationIndex) {
            //             if (operationIndex < operations.length) {
            //                 Pixastic.process(processedImage, key, operations[operationIndex], function(newImage) {
            //                     ++ operationIndex;
            //                     operateItem(newImage, index, key, operations, operationIndex);
            //                 });
            //             } else {
            //                 ++ index;
            //                 operate(processedImage, index);
            //             }
            //         }

            //         var operate = function(processedImage, index) {
            //             if (index < operationKeys.length) {
            //                 var key = operationKeys[index];

            //                 if (operations[key].count !== undefined) {
            //                     operateItem(processedImage, index, key, operations[key].data, 0);
            //                 } else {
            //                     Pixastic.process(processedImage, key, operations[key], function(newImage) {
            //                         ++ index;
            //                         operate(newImage, index);
            //                     });
            //                 }

            //             } else {
            //                 if ($image !== $originalImage) {
            //                     $image.remove();
            //                 }

            //                 $originalImage.before(processedImage);
            //                 $originalImage.hide();
            //                 $image = $(processedImage);

            //                 processImageTimer = setTimeout(processImage, 100);
            //             }
            //         };

            //         operate(imageClone, 0);

            //     } else {
            //         processImageTimer = setTimeout(processImage, 100);
            //     }
            // };

            // Process the image now so it will have the already applied adjustments
            // Note this seems to run repeatedly until the edit pop-up appears and is removed.
            // ??? It should probably just run the image adjustments once then quit instead of running constantly
            // processImage();

            // // Whenever edit adjustment popup is opened, process the image again
            // $edit.popup('container').bind('open', function() {
            //     processImage();
            // });

            // // Whenever edit adjustment popup is closed, cancel any processImage call that hasn't yet occurred
            // $edit.popup('container').bind('close', function() {
            //     if (processImageTimer) {
            //         clearTimeout(processImageTimer);
            //     }
            // });

            // var $resetButton = $('<button/>', {
            //     'text': 'Reset',
            //     'click': function() {
            //         $edit.find(':input').each(function() {
            //             var $input = $(this);

            //             if ($input.is(':checkbox')) {
            //                 $input.removeAttr('checked');

            //             } else {
            //                 $input.val(0);
            //             }
            //         });
            //         $editor.find(".imageEditor-blurOverlay").remove();

            //         processImage();

            //         return false;
            //     }
            // });

            var $blurOverlayIndex = 0;
            var addSizeBox = function(input, left, top, width, height) {
                //add blur box
                $blurOverlayIndex++;

                var $blurInput;

                if (input === null) {
                    $blurInput = $('<input>', {
                        'type' : 'hidden',
                        'name' : $dataName + ".blur",
                        'value' : left + "x" + top + "x" + width + "x" + height
                    });
                    $edit.append($blurInput);
                } else {
                    $blurInput = $(input);
                }

                var $blurOverlay = $('<div/>', {
                    'class': 'imageEditor-blurOverlay',
                    'css': {
                        'height': height + 'px',
                        'left': left  + 'px',
                        'position': 'absolute',
                        'top': top + 'px',
                        'width': width + 'px',
                        'z-index': 1
                    }
                });

                var $blurOverlayBox = $('<div/>', {
                    'class': 'imageEditor-blurOverlayBox',
                    'css': {
                        'height': height + 'px',
                        'position': 'absolute',
                        'width': width + 'px',
                        'z-index': 1,
                        'outline': '1px dashed #fff'
                    }
                });

                var $blurOverlayLabel = $('<div/>', {
                    'class': 'imageEditor-textOverlayLabel',
                    'text': 'Blur #' + $blurOverlayIndex
                });

                var sizeAspectRatio = width / height;
                var updateSizeBox = function(callback) {

                    // create a function that will be used as an event handler
                    // for a mousedown event
                    return function(event) {

                        // mousedown even has occurred

                        var sizeBoxPosition = $blurOverlayBox.parent().position();

                        var original = {
                            'left': sizeBoxPosition.left,
                            'top': sizeBoxPosition.top,
                            'width': $blurOverlayBox.width(),
                            'height': $blurOverlayBox.height(),
                            'pageX': event.pageX,
                            'pageY': event.pageY
                        };

                        var imageWidth = $image.width();
                        var imageHeight = $image.height();

                        //.drag( element, event, startCallback, moveCallback, endCallback )
                        $.drag(this, event, function(event) {
                            // drag start callback
                        }, function(event) {
                            // drag move callback
                            var deltaX = event.pageX - original.pageX;
                            var deltaY = event.pageY - original.pageY;
                            var bounds = callback(event, original, {
                                'x': deltaX,
                                'y': deltaY,
                                'constrainedX': Math.max(deltaX, deltaY * sizeAspectRatio),
                                'constrainedY': Math.max(deltaY, deltaX / sizeAspectRatio)
                            });

                            // Fill out the missing bounds.
                            for (key in original) {
                                bounds[key] = bounds[key] || original[key];
                            }

                            var overflow;

                            // When moving, don't let it go outside the image.
                            if (bounds.moving) {
                                if (bounds.left < 0) {
                                    bounds.left = 0;
                                }

                                if (bounds.top < 0) {
                                    bounds.top = 0;
                                }

                                overflow = bounds.left + bounds.width - imageWidth;
                                if (overflow > 0) {
                                    bounds.left -= overflow;
                                }

                                overflow = bounds.top + bounds.height - imageHeight;
                                if (overflow > 0) {
                                    bounds.top -= overflow;
                                }

                            // Resizing...
                            } else {
                                if (bounds.width < 10 || bounds.height < 10) {
                                    if (sizeAspectRatio > 1.0) {
                                        bounds.width = sizeAspectRatio * 10;
                                        bounds.height = 10;
                                    } else {
                                        bounds.width = 10;
                                        bounds.height = 10 / sizeAspectRatio;
                                    }
                                }

                                if (bounds.left < 0) {
                                    bounds.width += bounds.left;
                                    bounds.height = bounds.width / sizeAspectRatio;
                                    bounds.top -= bounds.left / sizeAspectRatio;
                                    bounds.left = 0;
                                }

                                if (bounds.top < 0) {
                                    bounds.height += bounds.top;
                                    bounds.width = bounds.height * sizeAspectRatio;
                                    bounds.left -= bounds.top * sizeAspectRatio;
                                    bounds.top = 0;
                                }

                                overflow = bounds.left + bounds.width - imageWidth;
                                if (overflow > 0) {
                                    bounds.width -= overflow;
                                    bounds.height = bounds.width / sizeAspectRatio;
                                }

                                overflow = bounds.top + bounds.height - imageHeight;
                                if (overflow > 0) {
                                    bounds.height -= overflow;
                                    bounds.width = bounds.height * sizeAspectRatio;
                                }
                            }

                            $blurOverlay.css(bounds);
                            $blurOverlayBox.css('width', bounds.width);
                            $blurOverlayBox.css('height', bounds.height);

                        // Set the hidden inputs to the current bounds.
                        }, function() {
                            // drag end callback
                            var blurOverlayBoxPosition = $blurOverlayBox.parent().position();
                            $blurInput.attr("value", blurOverlayBoxPosition.left + "x" + blurOverlayBoxPosition.top + "x" + $blurOverlayBox.width() + "x" + $blurOverlayBox.height());

                        });

                        return false;
                    };
                };

                $blurOverlayBox.append($('<div/>', {
                    'class': 'imageEditor-resizer imageEditor-resizer-left',
                    'mousedown': updateSizeBox(function(event, original, delta) {
                        return {
                            'left': original.left + delta.x,
                            'width': original.width - delta.x
                        };
                    }) 
                }));
                $blurOverlayBox.append($('<div/>', {
                    'class': 'imageEditor-resizer imageEditor-resizer-right',
                    'mousedown': updateSizeBox(function(event, original, delta) {
                        return {
                            'left': original.left,
                            'width': original.width + delta.x
                        };
                    })
                }));
                $blurOverlayBox.append($('<div/>', {
                    'class': 'imageEditor-resizer imageEditor-resizer-bottomRight',
                    'mousedown': updateSizeBox(function(event, original, delta) {
                        return {
                            'width': original.width + delta.constrainedX,
                            'height': original.height + delta.constrainedY
                        };
                    })
                }));

                $blurOverlay.append($blurOverlayBox);

                $blurOverlayLabel.mousedown(updateSizeBox(function(event, original, delta) {
                    return {
                        'moving': true,
                        'left': original.left + delta.x,
                        'top': original.top + delta.y
                    };
                }));

                $blurOverlay.append($blurOverlayLabel);

                var $blurOverlayRemove = $('<div/>', {
                    'class': 'imageEditor-textOverlayRemove',
                    'text': 'Remove',
                    'click': function() {
                        var left = $blurOverlay.css("left");
                        var top = $blurOverlay.css("top");
                        var width = $blurOverlay.css("width");
                        var height = $blurOverlay.css("height");

                        $('.imageEditor-blurOverlay').each(function() {
                            var $overlay = $(this);
                            if ($overlay.css("left") === left &&
                                    $overlay.css("top") === top &&
                                    $overlay.css("width") === width &&
                                    $overlay.css("height") === height) {
                                $overlay.remove();
                            }
                        });

                        var input = $blurInput;
                        var value = $(input).attr("value");
                        var name = $(input).attr("name");
                        $("input[name='" + name + "'][value='" + value + "']").remove();

                        $blurInput.remove();
                        $blurOverlay.remove();
                        return false;
                    }
                });

                $blurOverlay.append($blurOverlayRemove);

                $editor.append($blurOverlay);
            };

            $edit.find('.imageEditor-addBlurOverlay').bind('click', function() {
                var $imageEditorCanvas = $editor.find('.imageEditor-image canvas');
                var left = Math.floor($imageEditorCanvas.attr('width') / 2 - 50);
                var top = Math.floor($imageEditorCanvas.attr('height') / 2 - 50);
                var width = 100;
                var height = 100;
                addSizeBox(null, left, top, width, height);
            });

            var addHotSpot = function(input, left, top, width, height) {
                var $input = $(input);

                var $hotSpotOverlay = $('<div/>', {
                    'class': 'imageEditor-hotSpotOverlay',
                    'css': {
                        'height': height + 'px',
                        'left': left  + 'px',
                        'position': 'absolute',
                        'top': top + 'px',
                        'width': width + 'px',
                        'z-index': 1
                    },
                    'data-type-id' : $input.find('input[name$="file.hotspots.typeId"]').val()
                });

                var $hotSpotOverlayBox = $('<div/>', {
                    'class': 'imageEditor-hotSpotOverlayBox',
                    'css': {
                        'height': height + 'px',
                        'position': 'absolute',
                        'width': width + 'px',
                        'z-index': 1
                    }
                });

                var $hotSpotOverlayLabel = $('<div/>', {
                    'class': 'imageEditor-textOverlayLabel',
                    'text': 'HotSpot'
                });

                var sizeAspectRatio = width / height;
                var updateSizeBox = function(callback) {
                    return function(event) {
                        var sizeBoxPosition = $hotSpotOverlayBox.parent().position();
                        var original = {
                            'left': sizeBoxPosition.left,
                            'top': sizeBoxPosition.top,
                            'width': $hotSpotOverlayBox.width(),
                            'height': $hotSpotOverlayBox.height(),
                            'pageX': event.pageX,
                            'pageY': event.pageY
                        };

                        var imageWidth = $image.width();
                        var imageHeight = $image.height();

                        // .drag(element, event, startCallback, moveCallback, endCallback)
                        $.drag(this, event, function(event) {
                            // drag start callback
                        }, function(event) {
                            // drag move callback
                            var deltaX = event.pageX - original.pageX;
                            var deltaY = event.pageY - original.pageY;
                            var bounds = callback(event, original, {
                                'x': deltaX,
                                'y': deltaY,
                                'constrainedX': Math.max(deltaX, deltaY * sizeAspectRatio),
                                'constrainedY': Math.max(deltaY, deltaX / sizeAspectRatio)
                            });

                            // Fill out the missing bounds.
                            for (key in original) {
                                bounds[key] = bounds[key] || original[key];
                            }

                            var overflow;

                            // When moving, don't let it go outside the image.
                            if (bounds.moving) {
                                if (bounds.left < 0) {
                                    bounds.left = 0;
                                }

                                if (bounds.top < 0) {
                                    bounds.top = 0;
                                }

                                if (bounds.width === 0) {
                                    bounds.width = 10;
                                }

                                overflow = bounds.left + bounds.width - imageWidth;
                                if (overflow > 0) {
                                    bounds.left -= overflow;
                                }

                                overflow = bounds.top + bounds.height - imageHeight;
                                if (overflow > 0) {
                                    bounds.top -= overflow;
                                }

                            // Resizing...
                            } else {
                                if (bounds.width < 10 || bounds.height < 10) {
                                    if (sizeAspectRatio > 1.0) {
                                        bounds.width = sizeAspectRatio * 10;
                                        bounds.height = 10;
                                    } else {
                                        bounds.width = 10;
                                        bounds.height = 10 / sizeAspectRatio;
                                    }
                                }

                                if (bounds.left < 0) {
                                    bounds.width += bounds.left;
                                    bounds.height = bounds.width / sizeAspectRatio;
                                    bounds.top -= bounds.left / sizeAspectRatio;
                                    bounds.left = 0;
                                }

                                if (bounds.top < 0) {
                                    bounds.height += bounds.top;
                                    bounds.width = bounds.height * sizeAspectRatio;
                                    bounds.left -= bounds.top * sizeAspectRatio;
                                    bounds.top = 0;
                                }

                                overflow = bounds.left + bounds.width - imageWidth;
                                if (overflow > 0) {
                                    bounds.width -= overflow;
                                    bounds.height = bounds.width / sizeAspectRatio;
                                }

                                overflow = bounds.top + bounds.height - imageHeight;
                                if (overflow > 0) {
                                    bounds.height -= overflow;
                                    bounds.width = bounds.height * sizeAspectRatio;
                                }
                            }

                            $hotSpotOverlay.css(bounds);
                            $hotSpotOverlayBox.css('width', bounds.width);
                            $hotSpotOverlayBox.css('height', bounds.height);

                        // Set the hidden inputs to the current bounds.
                        }, function() {
                            // drag end callback
                            var hotSpotOverlayBoxPosition = $hotSpotOverlayBox.parent().position();
                            var scale = $imageReSizeScale;
                            var rotation = $edit.find(":input[name$='.rotate']").first().val();

                            if ($imageReSizeScale < 1) {
                                if (rotation === '0') {
                                    var cavnasWidth = $image.parent().find("canvas").width();
                                    scale = (cavnasWidth / 1000) * scale;
                                } else if (rotation === '90' || rotation === '-90') {
                                    var cavnasHeight = $image.parent().find("canvas").height();
                                    scale = (cavnasHeight / 1000) * scale;
                                }
                            }
                            scale = 1 / scale;

                            var x = parseInt(hotSpotOverlayBoxPosition.left * scale);
                            var y = parseInt(hotSpotOverlayBoxPosition.top * scale);
                            var width;
                            var height;
                            if (rotation === "0") {
                                width = parseInt($hotSpotOverlayBox.width() * scale);
                                height = parseInt($hotSpotOverlayBox.height() * scale);
                            } else if (rotation === '90' || rotation === '-90') {
                                height = parseInt($hotSpotOverlayBox.width() * scale);
                                width = parseInt($hotSpotOverlayBox.height() * scale);

                                if (rotation === '90') {
                                    var originalWidth = $image.width() / scale;
                                    y = originalWidth - x + height;
                                    x = parseInt(hotSpotOverlayBoxPosition.top * scale);
                                } else if (rotation === '-90') {
                                    var originalWidth = $image.height() * scale;
                                    x = originalWidth - parseInt(hotSpotOverlayBoxPosition.top * scale) - width;
                                    y = parseInt(hotSpotOverlayBoxPosition.left * scale);
                                }
                            }

                            if ($edit.find(":input[name$='.flipH']").first().prop('checked')) {
                                var originalWidth = $image.width() * scale;
                                x = originalWidth - x - width;
                            }

                            if ($edit.find(":input[name$='.flipV']").first().prop('checked')) {
                                var originalHeight = $image.height() * scale;
                                y = originalHeight - y - height;
                            }

                            $input.find(':input[name$="x"]').val(x);
                            $input.find(':input[name$="y"]').val(y);
                            $input.find(':input[name$="width"]').val(width);
                            $input.find(':input[name$="height"]').val(height);
                        });

                        return false;
                    };
                };

                if (isNaN(width)) {
                    $hotSpotOverlay.css("width", "10px");
                    $hotSpotOverlay.css("height", "0px");
                } else {
                    $hotSpotOverlayBox.append($('<div/>', {
                        'class': 'imageEditor-resizer imageEditor-resizer-left',
                        'mousedown': updateSizeBox(function(event, original, delta) {
                            $input.addClass("state-focus");
                            $hotSpotOverlay.addClass("selected");
                            return {
                                'left': original.left + delta.x,
                                'width': original.width - delta.x
                            };
                        })
                    }));
                    $hotSpotOverlayBox.append($('<div/>', {
                        'class': 'imageEditor-resizer imageEditor-resizer-right',
                        'mousedown': updateSizeBox(function(event, original, delta) {
                            $input.addClass("state-focus");
                            $hotSpotOverlay.addClass("selected");
                            return {
                                'left': original.left,
                                'width': original.width + delta.x
                            };
                        })
                    }));
                    $hotSpotOverlayBox.append($('<div/>', {
                        'class': 'imageEditor-resizer imageEditor-resizer-bottomRight',
                        'mousedown': updateSizeBox(function(event, original, delta) {
                            $input.addClass("state-focus");
                            $hotSpotOverlay.addClass("selected");
                            return {
                                'width': original.width + delta.constrainedX,
                                'height': original.height + delta.constrainedY
                            };
                        })
                    }));
                }

                $hotSpotOverlay.append($hotSpotOverlayBox);

                $hotSpotOverlayLabel.mousedown(updateSizeBox(function(event, original, delta) {
                    $input.addClass("state-focus");
                    $hotSpotOverlay.addClass("selected");
                    return {
                        'moving': true,
                        'left': original.left + delta.x,
                        'top': original.top + delta.y
                    };
                }));

                $hotSpotOverlay.mousedown(function() {
                    $('.imageEditor-hotSpotOverlay').removeClass("selected");
                    $('.state-focus').removeClass("state-focus");
                    $input.addClass("state-focus");
                    $hotSpotOverlay.addClass("selected");
                });
                $editor.mouseleave(function() {
                    $input.removeClass("state-focus");
                    $hotSpotOverlay.removeClass("selected");
                });

                $input.mouseover(function() {
                    $input.addClass("state-focus");
                    $hotSpotOverlay.addClass("selected");
                });

                $input.mouseleave(function() {
                    $input.removeClass("state-focus");
                    $hotSpotOverlay.removeClass("selected");
                });

                $hotSpotOverlay.append($hotSpotOverlayLabel);

                var $hotSpotOverlayRemove = $('<div/>', {
                    'class': 'imageEditor-textOverlayRemove',
                    'text': 'Remove',
                    'click': function() {
                        var left = $hotSpotOverlay.css("left");
                        var top = $hotSpotOverlay.css("top");
                        var width = $hotSpotOverlay.css("width");
                        var height = $hotSpotOverlay.css("height");

                        $('.imageEditor-hotSpotOverlay').each(function() {
                            var $overlay = $(this);
                            if ($overlay.css("left") === left &&
                                    $overlay.css("top") === top &&
                                    $overlay.css("width") === width &&
                                    $overlay.css("height") === height) {
                                $overlay.remove();
                            }
                        });

                        $input.addClass("toBeRemoved");
                        $input.find("input").attr("disabled", "disabled");
                        $hotSpotOverlay.remove();
                        return false;
                    }
                });

                $hotSpotOverlay.append($hotSpotOverlayRemove);
                $editor.append($hotSpotOverlay);
            };

            var rotateHotSpot = function (angle, x, y, width, height) {
                var $rotatedHotSpot = {};
                angle = parseInt(angle);

                var scale = $imageReSizeScale;
                if ($imageReSizeScale < 1) {
                    var cavnasHeight = $image.parent().find("canvas").height();
                    scale = (cavnasHeight / 1000) * scale;
                }
                if (angle === 90 ) {
                    var originalHeight = $image.width() / scale;

                    $rotatedHotSpot.x = originalHeight - y - width;
                    $rotatedHotSpot.y = x;
                    $rotatedHotSpot.width = height;
                    $rotatedHotSpot.height = width;
                } else if (angle === -90 ) {
                    var originalWidth = $image.height() / scale;

                    $rotatedHotSpot.x = y;
                    $rotatedHotSpot.y = originalWidth - x - height;
                    $rotatedHotSpot.width = height;
                    $rotatedHotSpot.height = width;
                }
                return $rotatedHotSpot;
            };

            var $initalizeHotSpots = function() {
                $image.parents(".inputContainer").find('.imageEditor-hotSpotOverlay').remove();
                var $hotSpots = $image.parents(".inputContainer").find('.hotSpots .objectInputs');
                if ($hotSpots !== undefined && $hotSpots.length > 0) {
                    var scale = $imageReSizeScale;
                    var rotation = $edit.find(":input[name$='.rotate']").first().val();
                    if ($imageReSizeScale < 1) {
                        if (rotation === '0') {
                            var cavnasWidth = $image.parent().find("canvas").width();
                            scale = (cavnasWidth / 1000) * scale;
                        } else if (rotation === '90' || rotation === '-90') {
                            var cavnasHeight = $image.parent().find("canvas").height();
                            scale = (cavnasHeight / 1000) * scale;
                        }
                    }

                    $hotSpots.each(function() {
                        var $this = $(this);
                        if (!$this.parents('li').first().hasClass('toBeRemoved')) {
                            var x = parseInt($this.find(':input[name$="x"]').val());
                            var y = parseInt($this.find(':input[name$="y"]').val());
                            var width = parseInt($this.find(':input[name$="width"]').val());
                            var height = parseInt($this.find(':input[name$="height"]').val());

                            if (rotation !== '0') {
                                var $rotatedHotSpot = rotateHotSpot(rotation, x, y, width, height);
                                x = $rotatedHotSpot.x;
                                y = $rotatedHotSpot.y;
                                width = $rotatedHotSpot.width;
                                height = $rotatedHotSpot.height;
                            }

                            if ($edit.find(":input[name$='.flipH']").first().prop('checked')) {
                                var originalWidth = $image.width() / scale;
                                x = originalWidth - x - width;
                            }

                            if ($edit.find(":input[name$='.flipV']").first().prop('checked')) {
                                var originalHeight = $image.height() / scale;
                                y = originalHeight - y - height;
                            }

                            x = x * scale;
                            y = y * scale;
                            width = width * scale;
                            height = height * scale;

                            addHotSpot($this.parent(), x, y, width, height);
                        }
                    });

                    $image.parents(".inputContainer").find(".hotSpots").unbind('change');
                    $image.parents(".inputContainer").find(".hotSpots").bind('change', function() {
                        $initalizeHotSpots();
                    });
                }
            };

            $initalizeHotSpots();

            $editor.parents('.inputContainer').bind('create', function() {
               var defaultX = parseInt(($image.width() / $imageReSizeScale) / 2);
               var defaultY = parseInt(($image.height() / $imageReSizeScale) / 2);

               $image.parents(".inputContainer").find('.hotSpots .objectInputs').each(function() {
                   var $this = $(this);

                   $this.find(':input[name$="x"]').val($this.find(':input[name$="x"]').val() === "" ? defaultX : $this.find(':input[name$="x"]').val());
                   $this.find(':input[name$="y"]').val($this.find(':input[name$="y"]').val() === "" ? defaultY : $this.find(':input[name$="y"]').val());
                   $this.find(':input[name$="width"]').val($this.find(':input[name$="width"]').val() === "" ? 100 : $this.find(':input[name$="width"]').val());
                   $this.find(':input[name$="height"]').val($this.find(':input[name$="height"]').val() === "" ? 100 : $this.find(':input[name$="height"]').val());
               });
               $initalizeHotSpots();
            });

            $edit.find(":input[name$='.rotate']").change(function() {
                setTimeout($initalizeHotSpots(), 250);
            });
            $edit.find(":input[name$='.flipH']").change(function() {
                setTimeout($initalizeHotSpots(), 250);
            });

            $edit.find(":input[name$='.flipV']").change(function() {
                setTimeout($initalizeHotSpots(), 250);
            });

            // END HOTSPOTS



            // $edit.append($resetButton);

            // $edit.popup();
            // $edit.popup('container').addClass('imageEditor-editPopup');
            // $edit.popup('close');

            // var $coverTop = $('<div/>', {
            //     'class': 'imageEditor-cover',
            //     'css': {
            //         'display': 'none',
            //         'left': '0',
            //         'position': 'absolute',
            //         'top': '0'
            //     }
            // });

            // var $coverLeft = $coverTop.clone(true);
            // var $coverRight = $coverTop.clone(true);
            // var $coverBottom = $coverTop.clone(true);

            // $editor.append($coverTop);
            // $editor.append($coverLeft);
            // $editor.append($coverRight);
            // $editor.append($coverBottom);

            // var updateCover = function(bounds) {
            //     var imageWidth = $image.width();
            //     var imageHeight = $image.height();
            //     var boundsRight = bounds.left + bounds.width;
            //     var boundsBottom = bounds.top + bounds.height;

            //     $coverTop.css({
            //         'height': bounds.top,
            //         'width': imageWidth
            //     });
            //     $coverLeft.css({
            //         'height': bounds.height,
            //         'top': bounds.top,
            //         'width': bounds.left
            //     });
            //     $coverRight.css({
            //         'height': bounds.height,
            //         'left': boundsRight,
            //         'top': bounds.top,
            //         'width': imageWidth - boundsRight
            //     });
            //     $coverBottom.css({
            //         'height': imageHeight - boundsBottom,
            //         'top': boundsBottom,
            //         'width': imageWidth
            //     });

            //     $coverTop.show();
            //     $coverLeft.show();
            //     $coverRight.show();
            //     $coverBottom.show();
            // };

            // var $sizes = $editor.find('.imageEditor-sizes table');

            // // Create new HTML to hold the size selectors
            // var $sizeSelectors = $('<ul/>', { 'class': 'imageEditor-sizeSelectors' });

            // // Object to hold a list of different aspect ratios.
            // // key = an aspect ratio
            // // value = the first input we found that matches that aspect ratio
            // // Note there are multiple aspect ratios set for each input so we can easily group inputs together that are close to the same aspect ratio
            // var $mainInputs = { };

            // // Loop through the size names
            // $sizes.find('th').each(function() {
            //     var $th = $(this);
            //     var $tr = $th.closest('tr');

            //     var sizeName = $tr.attr('data-size-name');


                // // Check if we are inside a popup
                // var $source = $th.popup('source');
                // if ($source) {
                //     // Yes we are in a popup
                //     // Check if this size is considered a "standard image size"
                //     // If it is a standard image size then we won't add it to the list
                //     var sizes = $source.closest('.inputContainer').attr('data-standard-image-sizes');
                //     if (sizes) {
                //         if ($.inArray(sizeName, sizes.split(' ')) < 0) {
                //             // continue with the next item in the each loop
                //             return;
                //         }
                //     }
                // }

                // var independent = $tr.attr('data-size-independent') === 'true';
                // var sizeWidth = parseFloat($tr.attr('data-size-width'));
                // var sizeHeight = parseFloat($tr.attr('data-size-height'));
                // var sizeAspectRatio = sizeWidth / sizeHeight;

                // var $input = { };

                // $.each(INPUT_NAMES, function(index, name) {
                //     $input[name] = $tr.find(':input[name$=".' + name + '"]');
                // });

                // var ratio = Math.floor(sizeAspectRatio * 100) / 100;
                // var $mainInput = $mainInputs['' + ratio];

                // if (!independent) {

                //     if ($mainInput) {

                //         // We already found an aspect ratio that exists, so just add 
                //         $mainInput['$sizeLabel'].html(
                //                 $mainInput['$sizeLabel'].html() +
                //                 '<br><span title="' + $th.text() + '">' + $th.text() + '</span>');

                //         // Also add pointers to the inputs for the current size to the first input we found,
                //         // so they call all be adjusted at the same time
                //         $.each(INPUT_NAMES, function(index, name) {
                //             $mainInput[name] = $mainInput[name].add($input[name]);
                //         });
                //         return;

                //     } else {
                //         $mainInputs['' + (ratio - 0.02)] = $input;
                //         $mainInputs['' + (ratio - 0.01)] = $input;
                //         $mainInputs['' + ratio] = $input;
                //         $mainInputs['' + (ratio + 0.01)] = $input;
                //         $mainInputs['' + (ratio + 0.02)] = $input;
                //     }
                // }


                // Box to adjust size of cropping

                // var $sizeBox = $('<div/>', {
                //     'class': 'imageEditor-sizeBox',
                //     'css': { 'position': 'absolute' }
                // });

                // $sizeBox.hide();
                // $editor.append($sizeBox);

                // var getSizeBounds = function($image) {
                //     var imageWidth = $image.width();
                //     var imageHeight = $image.height();

                //     var left = parseFloat($input.x.val()) || 0.0;
                //     var top = parseFloat($input.y.val()) || 0.0;
                //     var width = parseFloat($input.width.val()) || 0.0;
                //     var height = parseFloat($input.height.val()) || 0.0;

                //     if (width === 0.0 || height === 0.0) {
                //         width = imageHeight * sizeAspectRatio;
                //         height = imageWidth / sizeAspectRatio;

                //         if (width > imageWidth) {
                //             width = height * sizeAspectRatio;
                //         } else {
                //             height = width / sizeAspectRatio;
                //         }

                //         left = (imageWidth - width) / 2;
                //         top = 0;

                //     } else {
                //         left *= imageWidth;
                //         top *= imageHeight;
                //         width *= imageWidth;
                //         height *= imageHeight;
                //     }

                //     return {
                //         'left': left,
                //         'top': top,
                //         'width': width,
                //         'height': height
                //     };
                // };

                // var $sizeButton = $('<li/>', {
                //     'class': 'imageEditor-sizeButton',
                    // 'click': function() {
                    //     var $item = $(this).closest('li');

                    //     if ($item.is('.imageEditor-sizeSelected')) {
                    //         $item.removeClass('imageEditor-sizeSelected');
                    //         $coverTop.hide();
                    //         $coverLeft.hide();
                    //         $coverRight.hide();
                    //         $coverBottom.hide();
                    //         $sizeBox.hide();
                    //         return false;

                    //     } else {
                    //         $item.closest('ul').find('li').removeClass('imageEditor-sizeSelected');
                    //         $item.addClass('imageEditor-sizeSelected');
                    //     }

                        // $editor.find('.imageEditor-sizeBox').hide();

                        // var bounds = getSizeBounds($image);

                        // updateCover(bounds);
                        // $sizeBox.css(bounds);
                        // $sizeBox.show();

                        // $sizeBox.find('.imageEditor-textOverlay').each(function(index) {
                        //     var $textOverlay = $(this);

                            // $textOverlay.trigger('resizeTextOverlayFont');

                //             var textWidth = parseFloat($input.textWidths.val().split(DELIMITER)[index + 1]) || 0.0;

                //             if (textWidth !== 0.0) {
                //                 var textX = parseFloat($input.textXs.val().split(DELIMITER)[index + 1]) || 0.0;
                //                 var textY = parseFloat($input.textYs.val().split(DELIMITER)[index + 1]) || 0.0;

                //                 $textOverlay.css({
                //                     'left': (textX * 100) + '%',
                //                     'top': (textY * 100) + '%',
                //                     'width': (textWidth * 100) + '%'
                //                 });
                //             }
                //         });

                //         return false;
                //     }
                // });

                // $sizeSelectors.append($sizeButton);

                // var updatePreview = function() {
                //     var $body = $(document.body);
                //     $body.append($imageClone);
                //     var bounds = getSizeBounds($imageClone);
                //     $imageClone.remove();
                //     Pixastic.process(imageClone, 'crop', bounds, function(newImage) {
                //         var $preview = $sizeButton.find('.imageEditor-sizePreview');
                //         $preview.empty();
                //         $preview.append(newImage);
                //     });
                // };

                // var updateSizeBox = function(callback) {
                //     return function(event) {
                //         var sizeBoxPosition = $sizeBox.position();
                //         var original = {
                //             'left': sizeBoxPosition.left,
                //             'top': sizeBoxPosition.top,
                //             'width': $sizeBox.width(),
                //             'height': $sizeBox.height(),
                //             'pageX': event.pageX,
                //             'pageY': event.pageY
                //         };

                //         var imageWidth = $image.width();
                //         var imageHeight = $image.height();

                //         // .drag(element, event, startCallback, moveCallback, endCallback)
                //         $.drag(this, event, function(event) {
                //             // startCallback
                //         }, function(event) {
                //             // move callback
                //             var deltaX = event.pageX - original.pageX;
                //             var deltaY = event.pageY - original.pageY;
                //             var bounds = callback(event, original, {
                //                 'x': deltaX,
                //                 'y': deltaY,
                //                 'constrainedX': Math.max(deltaX, deltaY * sizeAspectRatio),
                //                 'constrainedY': Math.max(deltaY, deltaX / sizeAspectRatio)
                //             });

                //             // Fill out the missing bounds.
                //             for (key in original) {
                //                 bounds[key] = bounds[key] || original[key];
                //             }

                //             var overflow;

                //             // When moving, don't let it go outside the image.
                //             if (bounds.moving) {
                //                 if (bounds.left < 0) {
                //                     bounds.left = 0;
                //                 }

                //                 if (bounds.top < 0) {
                //                     bounds.top = 0;
                //                 }

                //                 overflow = bounds.left + bounds.width - imageWidth;
                //                 if (overflow > 0) {
                //                     bounds.left -= overflow;
                //                 }

                //                 overflow = bounds.top + bounds.height - imageHeight;
                //                 if (overflow > 0) {
                //                     bounds.top -= overflow;
                //                 }

                //             // Resizing...
                //             } else {
                //                 if (bounds.width < 10 || bounds.height < 10) {
                //                     if (sizeAspectRatio > 1.0) {
                //                         bounds.width = sizeAspectRatio * 10;
                //                         bounds.height = 10;
                //                     } else {
                //                         bounds.width = 10;
                //                         bounds.height = 10 / sizeAspectRatio;
                //                     }
                //                 }

                //                 if (bounds.left < 0) {
                //                     bounds.width += bounds.left;
                //                     bounds.height = bounds.width / sizeAspectRatio;
                //                     bounds.top -= bounds.left / sizeAspectRatio;
                //                     bounds.left = 0;
                //                 }

                //                 if (bounds.top < 0) {
                //                     bounds.height += bounds.top;
                //                     bounds.width = bounds.height * sizeAspectRatio;
                //                     bounds.left -= bounds.top * sizeAspectRatio;
                //                     bounds.top = 0;
                //                 }

                //                 overflow = bounds.left + bounds.width - imageWidth;
                //                 if (overflow > 0) {
                //                     bounds.width -= overflow;
                //                     bounds.height = bounds.width / sizeAspectRatio;
                //                 }

                //                 overflow = bounds.top + bounds.height - imageHeight;
                //                 if (overflow > 0) {
                //                     bounds.height -= overflow;
                //                     bounds.width = bounds.height * sizeAspectRatio;
                //                 }
                //             }

                //             updateCover(bounds);
                //             $sizeBox.css(bounds);
                //             $sizeBox.find('.imageEditor-textOverlay').trigger('resizeTextOverlayFont');

                //         // Set the hidden inputs to the current bounds.
                //         }, function() {
                //             // end callback
                //             var sizeBoxPosition = $sizeBox.position();
                //             var sizeBoxWidth = $sizeBox.width();
                //             var sizeBoxHeight = $sizeBox.height();

                //             $input.x.val(sizeBoxPosition.left / imageWidth);
                //             $input.y.val(sizeBoxPosition.top / imageHeight);
                //             $input.width.val(sizeBoxWidth / imageWidth);
                //             $input.height.val(sizeBoxWidth / sizeAspectRatio / imageHeight);

                //             updatePreview();
                //             $sizeBox.find('.imageEditor-textOverlay').trigger('resizeTextOverlayFont');
                //         });

                //         return false;
                //     };
                // };

                // $sizeBox.mousedown(updateSizeBox(function(event, original, delta) {
                //     return {
                //         'moving': true,
                //         'left': original.left + delta.x,
                //         'top': original.top + delta.y
                //     };
                // }));

                // $sizeBox.append($('<div/>', {
                //     'class': 'imageEditor-resizer imageEditor-resizer-topLeft',
                //     'mousedown': updateSizeBox(function(event, original, delta) {
                //         return {
                //             'left': original.left + delta.constrainedX,
                //             'top': original.top + delta.constrainedY,
                //             'width': original.width - delta.constrainedX,
                //             'height': original.height - delta.constrainedY
                //         };
                //     })
                // }));
                // $sizeBox.append($('<div/>', {
                //     'class': 'imageEditor-resizer imageEditor-resizer-bottomRight',
                //     'mousedown': updateSizeBox(function(event, original, delta) {
                //         return {
                //             'width': original.width + delta.constrainedX,
                //             'height': original.height + delta.constrainedY
                //         };
                //     })
                // }));

                // var $sizeLabel = $('<span/>', {
                //     'class': 'imageEditor-sizeLabel',
                //     'html': '<span title="' + $th.text() + '">' + $th.text() + '</span>'
                // });

                // $input['$sizeLabel'] = $sizeLabel;
                // $sizeButton.append($sizeLabel);

                // var $sizePreview = $('<div/>', {
                //     'class': 'imageEditor-sizePreview',
                //     'html': $('<img/>', {
                //         'alt': $th.text(),
                //         'src': imageSrc,
                //         'load': function() {
                //             updatePreview();
                //         }
                //     })
                // });

                // $sizeButton.append($sizePreview);


                //  TEXT OVERLAYS

                // var textOverlayIndex = 0;

                // $sizeLabel.after($('<a/>', {
                //     'class': 'imageEditor-addTextOverlay',
                //     'text': 'Add Text',
                //     'click': function(event, init) {
                //         ++ textOverlayIndex;

                //         var $textOverlay = $('<div/>', {
                //             'class': 'imageEditor-textOverlay',
                //             'css': {
                //                 'left': (($input.textXs.val().split(DELIMITER)[textOverlayIndex] || 0.25) * 100) + '%',
                //                 'position': 'absolute',
                //                 'top': (($input.textYs.val().split(DELIMITER)[textOverlayIndex] || 0.25) * 100) + '%',
                //                 'width': (($input.textWidths.val().split(DELIMITER)[textOverlayIndex] || 0.50) * 100) + '%',
                //                 'z-index': 1
                //             }
                //         });

                //         $sizeBox.append($textOverlay);

                //         var $textOverlayLabel = $('<div/>', {
                //             'class': 'imageEditor-textOverlayLabel',
                //             'text': 'Text #' + textOverlayIndex
                //         });

                //         var resizeTextOverlayFont = function() {
                //             if (!$sizeBox.is(':visible')) {
                //                 return;
                //             }

                //             var textSizes = '';

                //             $sizeBox.find('.imageEditor-textOverlay').each(function() {
                //                 var $to = $(this),
                //                         $body = $($to.find('.rte-container iframe')[0].contentDocument.body),
                //                         originalFontSize = $.data(this, 'imageEditor-originalFontSize'),
                //                         textSize,
                //                         newFontSize;

                //                 if (!originalFontSize && $body.is('.rte-loaded')) {
                //                     originalFontSize = parseFloat($body.css('font-size'));
                //                     $.data(this, 'imageEditor-originalFontSize', originalFontSize);
                //                 }

                //                 textSizes += DELIMITER;

                //                 if (originalFontSize > 0) {
                //                     textSize = 1 / sizeHeight * originalFontSize;
                //                     textSizes += textSize;
                //                     $body.css('font-size', $sizeBox.height() * textSize);
                //                 }
                //             });

                //             $input.textSizes.val(textSizes);
                //         };


                        // $textOverlay.bind('resizeTextOverlayFont', resizeTextOverlayFont);



                        // var updateTextOverlay = function(callback) {
                        //     return function(event) {
                        //         resizeTextOverlayFont();

                        //         var textOverlayPosition = $textOverlay.position();
                        //         var original = {
                        //             'left': textOverlayPosition.left,
                        //             'top': textOverlayPosition.top,
                        //             'width': $textOverlay.width(),
                        //             'height': $textOverlay.height(),
                        //             'pageX': event.pageX,
                        //             'pageY': event.pageY
                        //         };

                        //         var sizeBoxWidth = $sizeBox.width();
                        //         var sizeBoxHeight = $sizeBox.height();

                        //         event.dragImmediately = true;

                        //         $.drag(this, event, function(event) {

                        //         }, function(event) {
                        //             var deltaX = event.pageX - original.pageX;
                        //             var deltaY = event.pageY - original.pageY;
                        //             var bounds = callback(event, original, {
                        //                 'x': deltaX,
                        //                 'y': deltaY
                        //             });

                        //             // Fill out the missing bounds.
                        //             for (key in original) {
                        //                 bounds[key] = bounds[key] || original[key];
                        //             }

                        //             bounds.left = ((bounds.left / sizeBoxWidth) * 100) + '%';
                        //             bounds.top = ((bounds.top / sizeBoxHeight) * 100) + '%';
                        //             bounds.width = ((bounds.width / sizeBoxWidth) * 100) + '%';
                        //             bounds.height = 'auto';

                        //             $textOverlay.css(bounds);

                        //         // Set the hidden inputs to the current bounds.
                        //         }, function() {
                        //             var texts = '',
                        //                     xs = '',
                        //                     ys = '',
                        //                     widths = '';

                        //             $sizeBox.find('.imageEditor-textOverlay').each(function() {
                        //                 var $to = $(this),
                        //                         textOverlayPosition = $to.position(),
                        //                         textOverlayWidth = $to.width(),
                        //                         textOverlayHeight = $to.height();

                        //                 texts += DELIMITER + ($to.find('.imageEditor-textOverlayInput').val());
                        //                 xs += DELIMITER + (textOverlayPosition.left / sizeBoxWidth);
                        //                 ys += DELIMITER + (textOverlayPosition.top / sizeBoxHeight);
                        //                 widths += DELIMITER + (textOverlayWidth / sizeBoxWidth);
                        //             });

                        //             $input.texts.val(texts);
                        //             $input.textXs.val(xs);
                        //             $input.textYs.val(ys);
                        //             $input.textWidths.val(widths);
                        //         });

                        //         return false;
                        //     };
                        // };

                        // $textOverlayLabel.mousedown(updateTextOverlay(function(event, original, delta) {
                        //     return {
                        //         'moving': true,
                        //         'left': original.left + delta.x,
                        //         'top': original.top + delta.y
                        //     };
                        // }));

                        // $textOverlay.append($textOverlayLabel);

                        // var $textOverlayRemove = $('<div/>', {
                        //     'class': 'imageEditor-textOverlayRemove',
                        //     'text': 'Remove',
                        //     'click': function() {
                        //         $textOverlay.remove();
                        //         return false;
                        //     }
                        // });

                        // $textOverlay.append($textOverlayRemove);

                        // $textOverlay.append($('<div/>', {
                        //     'class': 'imageEditor-resizer imageEditor-resizer-left',
                        //     'mousedown': updateTextOverlay(function(event, original, delta) {
                        //         return {
                        //             'left': original.left + delta.x,
                        //             'width': original.width - delta.x
                        //         };
                        //     })
                        // }));
                        // $textOverlay.append($('<div/>', {
                        //     'class': 'imageEditor-resizer imageEditor-resizer-right',
                        //     'mousedown': updateTextOverlay(function(event, original, delta) {
                        //         return {
                        //             'left': original.left,
                        //             'width': original.width + delta.x
                        //         };
                        //     })
                        // }));

                        // var $textOverlayInput = $('<input/>', {
                        //     'class': 'imageEditor-textOverlayInput',
                        //     'type': 'text',
                        //     'value': $input.texts.val().split(DELIMITER)[textOverlayIndex] || ''
                        // });

                        // $textOverlay.append($textOverlayInput);
                        // $textOverlayInput.rte({
                        //     'initImmediately': true,
                        //     'useLineBreaks': true
                        // });

                        // $editor.before($textOverlay.find('.rte-toolbar-container'));

                        // if (!init) {
                        //     $textOverlayInput.focus();
                        // }

                        // var wait = 5000;
                        // var repeatResizeTextOverlayFont = function() {
                        //     var $body = $($textOverlay.find('.rte-container iframe')[0].contentDocument.body);
                        //     if ($body.is('.rte-loaded')) {
                        //         resizeTextOverlayFont();
                        //     } else {
                        //         wait -= 100;
                        //         if (wait > 0) {
                        //             setTimeout(repeatResizeTextOverlayFont, 100);
                        //         }
                        //     }
                        // };

                        // repeatResizeTextOverlayFont();

                        // On submit of the form, update the values for the text overlays that are showing
                        // ??? What if you update a text overlay, then move to a different group then submit the form?
                        // Need to save the current text values before removing the text overlays
                        // $form.submit(function() {
                        //     var texts = '';

                        //     $sizeBox.find('.imageEditor-textOverlay').each(function() {
                        //         texts += DELIMITER + $(this).find('.imageEditor-textOverlayInput').val();
                        //     });

                        //     $input.texts.val(texts);
                        // });

                        // if (!init) {
                        //     var sizeBoxWidth = $sizeBox.width();
                        //     var sizeBoxHeight = $sizeBox.height();
                        //     var texts = '',
                        //             xs = '',
                        //             ys = '',
                        //             widths = '';

                        //     $sizeBox.find('.imageEditor-textOverlay').each(function() {
                        //         var $to = $(this),
                        //                 textOverlayPosition = $to.position(),
                        //                 textOverlayWidth = $to.width(),
                        //                 textOverlayHeight = $to.height();

                        //         texts += DELIMITER + ($to.find('.imageEditor-textOverlayInput').val());
                        //         xs += DELIMITER + (textOverlayPosition.left / sizeBoxWidth);
                        //         ys += DELIMITER + (textOverlayPosition.top / sizeBoxHeight);
                        //         widths += DELIMITER + (textOverlayWidth / sizeBoxWidth);
                        //     });

                        //     $input.texts.val(texts);
                        //     $input.textXs.val(xs);
                        //     $input.textYs.val(ys);
                        //     $input.textWidths.val(widths);
                        // }

                        // return false;
                    }
                }));

                // For each text in the group, simulate a click on the add text overlay button to create the overlay
                // $.each($input.texts.val().split(DELIMITER), function(index, text) {
                //     if (index > 0) {
                //         $sizeButton.find('.imageEditor-addTextOverlay').trigger('click', true);
                //     }
                // });
            });

            // $sizes.before($sizeSelectors);
            // $sizes.hide();

            // Automatically select the first group if there is only one group
            var $buttons = $sizeSelectors.find('.imageEditor-sizeButton');
            if ($buttons.length == 1) {
                $buttons.click();
            }
        }

    });

});

======================================================================***/
