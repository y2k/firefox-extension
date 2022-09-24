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

set_manifest_version:
	cd resources && cat manifest.json | sed "s/\"version\": \"1.0.0\"/\"version\": \"1.0.$$EXT_VERSION\"/" > manifest.temp && mv manifest.temp manifest.json

publish: test set_manifest_version
	lein cljsbuild once min min-options && cd resources && zip my-extension.zip manifest.json options.html js/options.js js/extension.js && cat updates.template.json | sed "s/__CODE_VERSION__/$$EXT_VERSION/" > updates.json

.PHONY: figwheel build run_extension run_options test compile clean publish set_manifest_version
