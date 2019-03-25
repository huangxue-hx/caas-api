#!/bin/bash
function httpRequest()
{
      succ=0
      fail=0
      while true
      do
        info=`nc -zv -w10 $1 $2 2>& 1 | grep 'open'`
        if [ "$info" ]
        then
           succ=$[ $succ+1 ]
           if [ $succ == $4 ]
           then
             break
           fi
        else
          fail=$[ $fail+1 ]
          if [ $fail == $5 ]
          then
            break
          fi
        fi
        sleep $3
     done

     if [ $succ == $4 ]
     then
	exit 0
     elif [ $fail == $5 ]
     then
 	exit 1
     else
	exit 1
     fi

}

httpRequest $1 $2 $3 $4 $5
