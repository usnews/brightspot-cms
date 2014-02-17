function startProgress() {
    document.getElementById('cms-upload-progress').style.display = 'inline';
    window.setTimeout("refreshProgress()", 50);
    return true;
}
function refreshProgress() {
    $.get('/cms/content/progress.jsp?t=' + new Date().getTime(), function (data, status) {
        var uploadInfo = $.parseJSON(data);
        updateProgress(uploadInfo);
    });
}

function updateProgress(uploadInfo) {
    var percentage = uploadInfo.percentDone;
    var progress = document.getElementById("uploadprogress-bar");
    progress.style.width = percentage + "%";
    var percentProgress = document.getElementById("upload-percentage-status");
    percentProgress.innerHTML=progress.style.width +" Complete";
    if (percentage != 100) {
        window.setTimeout("refreshProgress()", 500);
    } else { //Delete progress data once it's finished
       $.get('/cms/content/progress.jsp?action=delete&t=' + new Date().getTime(), function (data, status) {
       });
    }
}
