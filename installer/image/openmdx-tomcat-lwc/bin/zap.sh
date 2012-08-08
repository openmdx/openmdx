#! /bin/sh
# zap pattern:  kill all processes matching pattern

PATH=/bin:/usr/bin
IFS='
'                   # just a newline
case $1 in
"")   echo 'Usage: zap [-2] pattern' 1>&2; exit 1 ;;
-*)   SIG=$1; shift
esac

echo '   PID TTY TIME CMD'
kill $SIG `ps -ag | egrep "$*" | awk '{print $1}'`
