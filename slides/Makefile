NODE = node_modules
BOWER = bower_components

all: $(BOWER)
	./node_modules/grunt-cli/bin/grunt

$(NODE):
	npm install

$(BOWER): $(NODE)
	./node_modules/bower/bin/bower install

clean: $(BOWER)
	./node_modules/grunt-cli/bin/grunt clean

.PHONY: all clean
