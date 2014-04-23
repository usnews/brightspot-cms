(function($, win, undef) {

$.plugin2('taxonomy', {
    '_create': function(root) {

        $('body').on('click', '.taxonomyExpand', function(){
            var $this = $(this);
            var selectedClass = 'state-selected';
            $this.closest('ul').find('.'+selectedClass).removeClass(selectedClass);
            $this.closest('li').addClass(selectedClass);
        });
    }
});

})(jQuery, window);
