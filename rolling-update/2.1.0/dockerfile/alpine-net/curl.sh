#!/bin/bash
function httpRequest()
{
    succ=0
    fail=0
    while true
    do
      #curl 请求
      info=`curl -s -m 10 -I $1`
      #获取返回码
      code=`echo $info|grep "HTTP"|awk '{print $2}'`
      #对响应码进行判断
      if [[ $code == 2* ]]
      then
	  succ=$[ $succ+1 ]
          if [ $succ == $3 ]
          then
	    break
          fi
      else
         fail=$[ $fail+1 ]
         if [ $fail == $4 ]
         then
           break
         fi
      fi
      sleep $2
    done


    if [ $succ == $3 ]
    then
    	exit 0
    elif [ $fail == $4 ]
    then
        exit 1
    else
	exit 1
    fi

}

httpRequest $1 $2 $3 $4
