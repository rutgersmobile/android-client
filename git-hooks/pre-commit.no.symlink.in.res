#!/bin/sh
#
# This hook verifies that no links have been placed in the resources directory.
# Renderizer copies links to its output directories instead of the actual files,
# so if you forget to use the `--dereference` option with `cp` then you might end
# up trying to commit symlinks instead of actual images!
#
# To use, link this as .git/hooks/pre-commit (or include it in your custom hook)

allowsymlinks=$(git config --bool hooks.allowsymlinks)
linked=$(find app/src/main/res -type l)

if [ "$allowsymlinks" != "true" ] && [ "$linked" != "" ]
then
	cat <<\EOF
Error: Attempt to commit symlinked resources.

Other people will not have resources necessary for building the project.

If you want to allow symlinked resources for some reason, you can disable this check with:

 git config hooks.allowsymlinks true
EOF

	# Display symlinked resources
	echo -e "\nSymlinked resources: "
	while read -r line; do
	    echo -e "\t$line"
	done <<< "$linked"

	exit 1
fi
