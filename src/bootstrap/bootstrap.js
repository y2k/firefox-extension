async function document_write(x) {
    let script = document.createElement('script');

    let url = /src="(.+?)"/.exec(x)?.[1]
    if (url) {
        // script.src = "moz-extension://6a654681-bf43-47cf-a041-4f5ee8dec02f/" + url
        script.src = "http://localhost:3449/" + url

        document.body.appendChild(script);

        await new Promise(r =>
            script.onload = _ => {
                script.onload = null;
                r()
            }
        )
    } else {
        script.innerHTML = x.replace("<script>", "").replace("</script>", "")
        document.body.appendChild(script);
    }
}

(async function () {
    try {
        window.CLOSURE_UNCOMPILED_DEFINES = {}
        window.CLOSURE_NO_DEPS = true
        window.CLOSURE_DEFINES = { 'goog.ENABLE_CHROME_APP_SAFE_SCRIPT_LOADING': true }

        if (typeof goog == "undefined")
            await document_write('<script src="js/compiled/out_ext/goog/base.js"></script>');
        await document_write('<script src="js/compiled/out_ext/goog/deps.js"></script>');
        await document_write('<script src="js/compiled/out_ext/cljs_deps.js"></script>');

        goog.require("figwheel.connect");
        goog.require("process.env");
        goog.require("extension.extension");
        await new Promise(r => setTimeout(r, 1000))
        figwheel.connect.start();
    } catch (e) {
        console.error(e)
    }
})()
