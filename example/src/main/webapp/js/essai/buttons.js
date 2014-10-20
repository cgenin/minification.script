/**
 * Created by skarb on 19/10/2014.
 */
$(document).ready(function () {
    $('#resetButt').click(function () {
        console.log('click on button !');
        var value = $('#value').val();
        $('#content').html('<h2>' + value + '</h2><em>by toolbar</em>');
    });
});
