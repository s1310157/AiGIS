#! /bin/bash

DIR=$(cd "$(dirname "$0")"; pwd)

NAME="$(uname)"
echo $NAME
case "${NAME:0:4}" in
  "Darw" ) OS='mac';;
  "Linu" ) OS='linux';;
  "MSYS" ) OS='win';;
  * ) echo "Your platform ($(uname)) is not supported.";exit 1;;
esac

java -Djava.library.path="$DIR/libs/$OS/" -Xmx4096m -jar AiGIS.jar
