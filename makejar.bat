if NOT exist "jar-output" md jar-output
cd jar-output
echo i|del *.*

copy ..\src\V.java .

javac -g:none V.java
jar cvfM V4k-raw.jar V.class

del V.class

pack200 --repack V4k.jar V4k-raw.jar
pack200 -E9 V4k.jar.pack.gz V4k.jar
del V.*
cd ..
