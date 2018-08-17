#!/bin/csh -f

# kills Afecs C clients
# author: vhg, 29-aug-2013

set a=`ps -ef | grep -v grep | grep ACodaCC | awk '{print $2}'`
foreach i($a)
kill -9 $i
end

exit
