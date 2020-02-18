#!/bin/csh -f
# author gurjyan
# Aug 2013
#
alias MATH 'set \!:1 = `echo "\!:3-$" | bc -l`'

set i = 1
set loop = $1

cd ../ACodaCC/Debug/

while ( $loop > 0)
MATH loop = $loop - $i
ACodaCC -n U$loop -m libCClient.so &
echo $loop
end
