#! /bin/sh

DIR=$(cd "$(dirname "$0")"; pwd)
LIBS="$DIR/app/libs/linux/"

cd $DIR
./app/jre/bin/java -Djava.library.path="$LIBS" -Xmx4096m -jar app/AiGIS.jar

# アイコンパスを更新
sed -i -e "s,#*Icon=.*,Icon=$DIR/app/icon/icon.png,g" AiGIS.desktop
