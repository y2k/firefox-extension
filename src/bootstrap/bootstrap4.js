
window.CLOSURE_DEFINES = { 'goog.ENABLE_CHROME_APP_SAFE_SCRIPT_LOADING': true }
document.write('<script src="http://localhost:3449/js/compiled/extension.js"></script>');

document.head.innerHTML += '<script async="" src="http://localhost:3449/js/compiled/extension.js"></script>'

window.CLOSURE_DEFINES = { 'goog.ENABLE_CHROME_APP_SAFE_SCRIPT_LOADING': true }
let script = document.createElement('script');
script.src = 'http://localhost:3449/js/compiled/extension.js'
document.body.appendChild(script)
