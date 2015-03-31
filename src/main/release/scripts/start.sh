###
# **************************************************-
# ingrid-upgrader
# ==================================================
# Copyright (C) 2014 - 2015 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.1 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# http://ec.europa.eu/idabc/eupl5
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and
# limitations under the Licence.
# **************************************************#
###
#!/bin/sh
#
##
# Environment Variables
#
#   INGRID_JAVA_HOME Overrides JAVA_HOME.
#
#   INGRID_OPTS      addtional java runtime options
#

THIS="$0"

# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`
PID=$INGRID_HOME/ingrid.pid

# -Durl describes the URL that can be accessed from extern
INGRID_OPTS="-Durl=@EXTERNAL_URL@ -Djetty.port=@SERVER_PORT@ -Dsource=@SCAN_DIR@ -Dindex=./index -Djetty.reload=manual -Djetty.home=./jetty"

# functions
stopIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  if [ -f $PID ]; then
		procid=`cat $PID`
		idcount=`ps -p $procid | wc -l`
		if [ $idcount -eq 2 ]; then
			echo stopping $command
			kill `cat $PID`
			sleep 3
			idcount=`ps -p $procid | wc -l`
			if [ $idcount -eq 1 ]; then
				echo "process ($procid) has been terminated."
				unlink $PID
				exit 0
			else
				echo "process is still running. Exit."
				exit 1
			fi 
		else
			echo "process is not running. Exit."
			unlink $PID 
		fi
	else
		echo "process is not running. Exit."
	fi
}

stopNoExitIplug()
{
  echo "Try stopping jetty ($INGRID_HOME)..."
  if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo stopping $command
        kill -9 `cat $PID`
        echo "process ($procid) has been terminated."
      else
        echo "process is not running. Exit."
      fi
    else
      echo "process is not running. Exit."
    fi
}


startIplug()
{
  echo "Try starting jetty ($INGRID_HOME)..."
  if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo plug running as process `cat $PID`.  Stop it first.
        exit 1
      fi
  fi
  
  # some Java parameters
  if [ "$INGRID_JAVA_HOME" != "" ]; then
    #echo "run java in $INGRID_JAVA_HOME"
    JAVA_HOME=$INGRID_JAVA_HOME
  fi
  
  if [ "$JAVA_HOME" = "" ]; then
    echo "Error: JAVA_HOME is not set."
    exit 1
  fi
  
  JAVA=$JAVA_HOME/bin/java
	
  INGRID_OPTS="$INGRID_OPTS -Dingrid_home=$INGRID_HOME -XX:+UseG1GC -XX:NewRatio=1"
  
  # check java version
  JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
  JAVA_VERSION_PART_0=`echo $JAVA_VERSION | awk '{split($0, array, "-")} END{print array[1]}'`
  JAVA_VERSION_PART_1=`echo $JAVA_VERSION_PART_0 | awk '{split($0, array, "_")} END{print array[1]}'`
  JAVA_VERSION_PART_2=`echo $JAVA_VERSION_PART_0 | awk '{split($0, array, "_")} END{print array[2]}'`
  if [[ "$JAVA_VERSION_PART_1" > "1.7.0" ]]; then
    LENGTH="${#JAVA_VERSION_PART_2}" 
    if [[ "$LENGTH" < "2" ]]; then
        JAVA_VERSION_PART_2="0"$JAVA_VERSION_PART_2
    fi
    if [[ "$JAVA_VERSION_PART_2" > "19" ]]; then
        INGRID_OPTS="$INGRID_OPTS -XX:+UseStringDeduplication" 
    elif [[ "$JAVA_VERSION_PART_1" > "1.8.0" ]]; then
        INGRID_OPTS="$INGRID_OPTS -XX:+UseStringDeduplication" 
    fi
  fi
  
  # run it
  exec nohup "$JAVA" $INGRID_OPTS -jar jetty/start.jar > console.log &
  
  echo "jetty ($INGRID_HOME) started."
  echo $! > $PID
}

# make sure the current user has the privilege to execute that script
if [ "$INGRID_USER" = "" ]; then
  INGRID_USER="ingrid"
fi

STARTING_USER=`whoami`
if [ "$STARTING_USER" != "$INGRID_USER" ]; then
  echo "You must be user '$INGRID_USER' to start that script! Set INGRID_USER in environment to overwrite this."
  exit 1
fi 

case "$1" in
  start)
    startIplug
    ;;
  stop)
    stopIplug
    ;;
  restart)
    stopNoExitIplug
    echo "sleep 3 sec ..."
    sleep 3
    startIplug
    ;;
  status)
    if [ -f $PID ]; then
      procid=`cat $PID`
      idcount=`ps -p $procid | wc -l`
      if [ $idcount -eq 2 ]; then
        echo "process ($procid) is running."
      else
        echo "process is not running. Exit."
      fi
    else
      echo "process is not running. Exit."
    fi
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac
