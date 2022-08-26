build:
	rm -rf resources/public/js && lein cljsbuild once

run_extension:
	rm -rf resources/public/js && lein cljsbuild once options && lein figwheel extension

run_options:
	rm -rf resources/public/js && lein figwheel options

figwheel:
	rm -rf resources/public/js && lein figwheel extension

test:
	lein test

.PHONY: figwheel build run_extension run_options test
