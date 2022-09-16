compile:
	lein figwheel :once

clean:
	rm -rf resources/js

build:
	rm -rf resources/js && lein cljsbuild once

run_extension:
	rm -rf resources/js && lein cljsbuild once options && lein figwheel extension

options:
	rm -rf resources/js && lein figwheel options

figwheel:
	rm -rf resources/js && lein figwheel extension

test:
	lein test

publish:
	lein cljsbuild once min min-options && cd resources && zip my-extension.zip manifest.json options.html js/options.js js/extension.js

.PHONY: figwheel build run_extension run_options test compile clean publish
