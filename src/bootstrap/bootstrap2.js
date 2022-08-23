(async function () {
    async function add_script_url(url) {
        let script = document.createElement('script');
        script.src = "http://localhost:3449/js/compiled/out_ext/" + url
        document.body.appendChild(script);
        await new Promise(r => script.onload = _ => { script.onload = null; r() })
    }

    try {
        window.CLOSURE_UNCOMPILED_DEFINES = {}
        window.CLOSURE_NO_DEPS = true
        window.CLOSURE_DEFINES = { 'goog.ENABLE_CHROME_APP_SAFE_SCRIPT_LOADING': true }

        await add_script_url("goog/base.js");
        await add_script_url("goog/deps.js");
        await add_script_url("cljs_deps.js");

        goog.require("figwheel.connect");
        goog.require("process.env");
        goog.require("extension.extension");
        await new Promise(r => setTimeout(r, 1000))
        figwheel.connect.start();
    } catch (e) {
        console.error(e)
    }
})()