#!/bin/bash
hostname=$(hostname)
datetime=$(date +"%Y-%m-%d-%H-%M-%S")
#
# This part starts / stops the master daemon
#
# Example:
# ./dewe.sh master start
# ./dewe.sh master stop
#
if [ $1 == "master" ]; then
    if [ $2 == "start" ]; then
        echo "start master"
        nohup java -jar ../target/DEWE.v2.one-jar.jar master < /dev/null > master-$hostname-$datetime.log 2>&1 &
        echo $! > master.pid
    fi
    if [ $2 == "stop" ]; then
        echo "stop master"
        if [ -f 'master.pid' ]; then
          kill -TERM $(cat master.pid)
          rm master.pid
        fi
    fi
fi

#
# This part starts /stops the worker daemon
#
# Example:
# ./dewe.sh worker start
# ./dewe.sh worker stop
#
if [ $1 == "worker" ]; then
    if [ $2 == "start" ]; then
        echo "start worker"
        nohup java -jar ../target/DEWE.v2.one-jar.jar worker < /dev/null > worker-$hostname-$datetime.log 2>&1 &
        echo $! > worker.pid
    fi
    if [ $2 == "stop" ]; then
        echo "stop worker"
        if [ -f 'worker.pid' ]; then
          kill -TERM $(cat worker.pid)
          rm worker.pid
        fi
    fi
fi

#
# This part starts /stops the load monitoring daemon
#
# Example:
# ./dewe.sh monitor xvdb start
# ./dewe.sh monitor stop
#
if [ $1 == "monitor" ]; then
    if [ -z "$3" ]; then
      if [ $2 == "stop" ]; then
          echo "stop load monitor"
          if [ -f 'monitor.pid' ]; then
            kill -TERM $(cat monitor.pid)
            rm monitor.pid
          fi
      fi
    else
      if [ $3 == "start" ]; then
          echo "start load monitor"
          nohup java -jar ../target/DEWE.v2.one-jar.jar monitor $2 < /dev/null > monitor-$hostname-$datetime.log 2>&1 &
          echo $! > monitor.pid
      fi
    fi
fi

#
# This part submits a workflow to the master
#
# Example:
# ./dewe.sh submit Test-Name /data/Project-Folder
#
if [ $1 == "submit" ]; then
	java -jar ../target/DEWE.v2.one-jar.jar submit $2 $3
fi

#
# This part checks the status of submitted workflows
#
# Example:
# ./dewe.sh status
#

if [ $1 == "status" ]; then
	dbhost=`grep master= config.properties | awk -F'=' '{print $2}'`
	mysql -h $dbhost -u root -proot -e "SELECT * FROM workflow" workflow
fi
