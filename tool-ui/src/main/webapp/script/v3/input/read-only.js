define([ 'bsp-utils' ], function (bsp_utils) {
  bsp_utils.onDomInsert(document, '.inputContainer-readOnly :text, .inputContainer-readOnly textarea', {
    'insert': function (input) {
      input.readOnly = true;
    }
  });
});