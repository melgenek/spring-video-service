<form onsubmit="onSubmit()">
    <input type="file" id="fileInput"/>
    <input type="submit"/>
</form>

<script>
    function onSubmit() {
        event.preventDefault();
        var fileInput = document.getElementById("fileInput");
        var fileList = fileInput.files;
        if (fileList.length > 0) { // do upload of the first file if available
            var fileToUpload = fileList[0];
            uploadFile(fileToUpload);
        }
    }

    function uploadFile(file) {
        var xhr = new XMLHttpRequest();
        xhr.open(
                "POST", "/upload/" + file.name, // request path
                true // asyncronous
        );
        xhr.send(file); // start upload

        // Redirect to the video list after upload finishes
        xhr.onreadystatechange = function () {
            if (xhr.readyState === 4) {
                window.location.href = "/"
            }
        }
    }
</script>
