#! /bin/sh

OUTDIR=out

while echo $1 | grep -q ^-; do
    eval $( echo $1 | sed 's/^-//' )=$2
    shift
    shift
done

if [ -d $OUTDIR ]; then
  rm -rf $OUTDIR
fi
mkdir -p $OUTDIR/jar
JARDIR=$OUTDIR/jar

JDKVER=$(javac -version 2>&1)
echo $JDKVER
if [[ $JDKVER != "javac 1."* ]] ;
then
  JAVACOPT="--release 8"
fi

# OSごとにクラスパス区切りを変える
case "$(uname)" in
  *"MINGW"*|*"MSYS"*|*"CYGWIN"*)
    CPSEP=";"
    ;;
  *)
    CPSEP=":"
    ;;
esac

# Jar
func_jar()
{
  echo "Create JAR"
  TMPNAME=tmp
  TMPDIR=$JARDIR/$TMPNAME
  mkdir $TMPDIR

  CLASSPATH="./libs/jogl-all.jar${CPSEP}./libs/gluegen-rt.jar${CPSEP}./libs/vecmath.jar${CPSEP}./libs/jfreechart-1.0.19.jar${CPSEP}./libs/jcommon-1.0.23.jar${CPSEP}./libs/AppleJavaExtensions-1.6.jar${CPSEP}./libs/fits.jar"

  echo "Compiling Java sources..."
  find ./src -name "*.java" -print | xargs javac -XDignore.symbol.file -classpath "$CLASSPATH" -d $TMPDIR $JAVACOPT

  cp -R src/aigis/res $TMPDIR/aigis/res
  cp -R template/meta/* $TMPDIR
  cp -R libs $JARDIR/
  cd $JARDIR
  jar cvmf ./$TMPNAME/META-INF/MANIFEST.MF AiGIS.jar -C $TMPNAME .
  rm -rf $TMPNAME
  cd - >/dev/null
  cp -R template/Jar/* $JARDIR
  echo "Done!"
}

func_jar

# Macアプリ
func_mac()
{
  echo "Create MacApp"
  MACDIR=$OUTDIR/mac
  APPDIR=$MACDIR/AiGIS.app/Contents/MacOS
  TEMPDIR=template/Mac
  mkdir $MACDIR
  cp -R $TEMPDIR/AiGIS.app $MACDIR/
  cp $JARDIR//AiGIS.jar $APPDIR/
  cp -R $TEMPDIR/jre $APPDIR/
  cp -R libs $APPDIR/
  rm -R $APPDIR/libs/linux
  rm -R $APPDIR/libs/win
  echo "Done!"
}

# Linuxアプリ
func_linux()
{
  echo "Create LinuxApp"
  LINUXDIR=$OUTDIR/linux
  APPDIR=$LINUXDIR/app
  TEMPDIR=template/Linux
  mkdir $LINUXDIR
  mkdir $APPDIR
  cp $JARDIR/AiGIS.jar $APPDIR
  cp -R $TEMPDIR/jre $APPDIR/
  cp -R $TEMPDIR/icon $APPDIR/
  cp -R libs $APPDIR/
  rm -R $APPDIR/libs/mac
  rm -R $APPDIR/libs/win
  cp -R $TEMPDIR/AiGIS.sh $LINUXDIR/
  cp -R $TEMPDIR/AiGIS.desktop $LINUXDIR/
  echo "Done!"
}

# Winアプリ
func_win()
{
  echo "Create WindowsApp"
  WINDIR=$OUTDIR/win
  APPDIR=$WINDIR/app
  TEMPDIR=template/Win
  mkdir $WINDIR
  mkdir $APPDIR
  cp $JARDIR/AiGIS.jar $APPDIR
  cp -R $TEMPDIR/jre $APPDIR/
  cp -R libs $APPDIR/
  rm -R $APPDIR/libs/mac
  rm -R $APPDIR/libs/linux
  cp -R $TEMPDIR/AiGIS/Release/AiGIS.exe $WINDIR/
  echo "Done!"
}

case "$target" in
  "mac" ) func_mac;;
  "linux" ) func_linux;;
  "win" ) func_win;;
  "all" ) func_mac;func_linux;func_win;;
esac
