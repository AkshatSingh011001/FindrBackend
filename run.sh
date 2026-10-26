#!/bin/sh
# usage: ./run.sh console | server | tests
mkdir -p bin
javac -encoding UTF-8 -d bin -sourcepath src src/com/findr/Main.java src/com/findr/ServerMain.java src/com/findr/test/TestRunner.java || exit 1
case "$1" in
  server)  java -cp bin com.findr.ServerMain ;;
  tests)   java -cp bin com.findr.test.TestRunner ;;
  *)       java -cp bin com.findr.Main ;;
esac
