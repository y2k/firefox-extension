build:
	lein cljsbuild once

test:
	lein test

.PHONY: build test
