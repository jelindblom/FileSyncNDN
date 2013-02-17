#!/bin/sh

usage() {
	echo "usage: $0 [ -h ] [ -g ] [ -d ] [ -x ] [ -r <repository path> ]"
	echo "-h : help"
	echo "-g : gui"
	echo "-d : ccnd"
	echo "-x : 64-bit Architecture"
	echo "-r : ccnr"
	exit 1
}

MAINCLASS="FileSyncNDN"
LIBRARY="./FileSyncNDN/libs/"

while getopts hgdr:x OPT; do
	case "$OPT" in
		h)
			usage
			;;
		g)
			MAINCLASS="FileSyncNDNGui"
			;;
		d)
			ccndstop
			ccndstart
			;;
		r)
			( mkdir -p $OPTARG; cd $OPTARG; ccnr & ) 
			;;
		x)
			LIBRARY="./FileSyncNDN/libs/64bit/"
			;;
		\?) 
			echo "Invalid Option..."
			usage
			;;
		*)
			echo "Invalid Argument..."
			usage
			;;
	esac
done

# Execute Application
java -Djava.library.path=$LIBRARY -classpath ./FileSyncNDN/libs/ccn.jar:./FileSyncNDN/libs/bcprov-jdk16-143.jar:./FileSyncNDN/libs/commons-codec-1.7.jar:./FileSyncNDN/libs/commons-io-2.4.jar:./FileSyncNDN/libs/jnotify-0.94.jar:./FileSyncNDN/build/classes $MAINCLASS
