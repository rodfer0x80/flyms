.PHONY: run
run: # start all the good stuff
	scripts/run.py

.PHONY: build
build: # detect OS, install deps
	scripts/build.py
