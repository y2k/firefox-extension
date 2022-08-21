build:
	rm -rf resources/public/js && lein cljsbuild once options

run:
	rm -rf resources/public/js && lein figwheel options

test:
	lein test

.PHONY: build test
