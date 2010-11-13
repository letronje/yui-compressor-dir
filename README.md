Modified version of the YUI js/css compressor that walks through a directory to compress js/css files in it.
Creates a backup of every compressable file found.(backup file name = <orig_file_name>_orig.<orig_extension>)

Usage:

java -jar <path/to/yui/jar> <path/to/dir/containing/js/and/css/files>

TODO:

- Fix hard coding for jsp files and generalize detection of js/css files based on file name regexes
- Use a command line option parsing lib(like the one that comes with the original YUI compressor)
- Add a command line switch to tell the compressor to delete the backup files from previous run
