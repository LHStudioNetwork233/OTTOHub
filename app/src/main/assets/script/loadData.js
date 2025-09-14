window.onload = function() {
    loadData();
};

function loadData() {
    console.log("start loading");
    loadStyle();
    loadScripts(); //加载自定义脚本和样式

    var content = document.getElementById("content");

    document.bgColor = window.dataBridge.getColor(0);
    content.style.color = window.dataBridge.getColor(1); //设置适配颜色

    var intervalId = setInterval(
        function() {
            var data = window.dataBridge.getData();
            if (data !== "loading") {
                console.log("onload data " + data);
                clearInterval(intervalId);
                content.innerHTML = data; //设置内部HTML
                onImageClick();
            }
        },
        500);
}

function loadStyle() {
    var uri = window.dataBridge.getStyleFile();
    if (uri !== null) {
        document.getElementById('dynamic-css').href = uri;
    }
}

function loadScripts() {
    var scriptUris = JSON.parse(window.dataBridge.getScriptFiles());
    scriptUris.forEach(
        function(uri) {
            var script = document.createElement('script');
            script.src = uri;
            document.body.appendChild(script);
        }
    );
}

function onImageClick() {
    if (window.ImageBridge.isHandle()) {
        var content = document.getElementById("content");
        var images = content.getElementsByTagName("img");
        for (var i = 0; i < images.length; i++) {
            images[i].addEventListener(
                'click',
                function(event) {
                    event.preventDefault();
                    var imageUrl = this.src;
                    window.ImageBridge.callImageViewer(imageUrl);
                },
                true);
        }
    }
}
