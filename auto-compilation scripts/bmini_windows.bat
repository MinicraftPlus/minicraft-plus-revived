REM I don't actually know if this works...

cd src

dir /s *.java > filenames.txt

javac -d ../build/ @filenames.txt

del filenames.txt

cd ..

jar cfve minicraft_plus_test.jar minicraft.Game -C build/ .

java -jar minicraft_plus_test.jar
