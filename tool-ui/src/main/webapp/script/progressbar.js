function startProgress()
{
              document.getElementById('uploadStatus').style.display = 'inline';
              document.getElementById('uploadProgressMessage').innerHTML = 'Upload Progress:';
              document.getElementById('uploadProgressMessage').style.display = 'inline';
              //document.getElementById('progressBarText').innerHTML = "0" + '%';
              window.setTimeout("refreshProgress()", 1000);
              return true;
}
function refreshProgress() {
              $.get('/cms/content/progress.jsp?' + new Date().getTime(),function(data,status){
              var uploadInfo = $.parseJSON( data );
              updateProgress(uploadInfo);
              })
}
function updateProgress(uploadInfo) {
              // make sure you set the width style property for uploadProgressBar, otherwise progress.style.width won't work
              var percentage=uploadInfo.percentDone;
              var progress = document.getElementById("uploadProgressBar");
              var indicator = document.getElementById("uploadIndicator");
              var maxWidth = parseInt(progress.style.width) - 4;
              var width = percentage * maxWidth / 100;
              indicator.style.width = width + "px";
              var perc = document.getElementById("uploadPercentage");
              perc.innerHTML = percentage + "%";
              if (percentage != 100)
              window.setTimeout("refreshProgress()", 1000);
              else 
              document.getElementById('uploadStatus').style.display = "none";
}
