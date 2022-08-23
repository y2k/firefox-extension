build:
	rm -rf resources/public/js && lein cljsbuild once

run_extension:
	rm -rf resources/public/js && lein cljsbuild once options && lein figwheel extension

run_options:
	rm -rf resources/public/js && lein figwheel options

test:
	lein test

.PHONY: build run_extension run_options test
