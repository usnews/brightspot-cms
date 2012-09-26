CKEDITOR.skins.add('cms', {
    'editor': { 'css': [ 'editor.css' ] },
    'dialog': { 'css': [ 'dialog.css' ] },
    'init': function(editor) {
        editor.on('dialogShow', function(e) {
            // console.log(e.editor);
            var co = $('#cke_contents_' + e.editor.name).offset();
            var so = $(e.editor.getSelection().getStartElement().$).offset();
            // console.log(o);
            e.data.parts.dialog.setStyles({
                'left': co.left + so.left + 'px',
                'position': 'absolute',
                'top': co.top + so.top + 'px',
                'z-index': 75
            });
        });
    }
});

(function() {
    var init = function() {
        CKEDITOR.dialog.on('resize', function(e) {
            var dialog = e.data.dialog;
            dialog.on('show', function() {
            });
        });
    };
    CKEDITOR.dialog ? init() : CKEDITOR.on('dialogPluginReady', init);
})();
/*
(function()
{
	CKEDITOR.dialog ? dialogSetup() : CKEDITOR.on( 'dialogPluginReady', dialogSetup );

	function dialogSetup()
	{
		CKEDITOR.dialog.on( 'resize', function( evt )
			{
				var data = evt.data,
					width = data.width,
					height = data.height,
					dialog = data.dialog,
					contents = dialog.parts.contents;

				if ( data.skin != 'kama' )
					return;

				contents.setStyles(
					{
						width : width + 'px',
						height : height + 'px'
					});

				// Fix the size of the elements which have flexible lengths.
				setTimeout( function()
					{
						var innerDialog = dialog.parts.dialog.getChild( [ 0, 0, 0 ] ),
							body = innerDialog.getChild( 0 );

						// tc
						var el = innerDialog.getChild( 2 );
						el.setStyle( 'width', ( body.$.offsetWidth ) + 'px' );

						// bc
						el = innerDialog.getChild( 7 );
						el.setStyle( 'width', ( body.$.offsetWidth - 28 ) + 'px' );

						// ml
						el = innerDialog.getChild( 4 );
						el.setStyle( 'height', ( body.$.offsetHeight - 31 - 14 ) + 'px' );

						// mr
						el = innerDialog.getChild( 5 );
						el.setStyle( 'height', ( body.$.offsetHeight - 31 - 14 ) + 'px' );
					},
					100 );
			});
	}
})();
*/
