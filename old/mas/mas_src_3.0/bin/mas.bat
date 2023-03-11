cd ..

START /realtime "" "C:/Program Files/Java/jdk1.7.0/bin/java.exe" -server -Xms200m -Xmx200m -XX:+AggressiveOpts -cp lib/mas.jar;extlib/org.springframework.asm-3.0.0.RC2.jar;extlib/org.springframework.beans-3.0.0.RC2.jar;extlib/org.springframework.context-3.0.0.RC2.jar;extlib/org.springframework.core-3.0.0.RC2.jar;extlib/org.springframework.expression-3.0.0.RC2.jar;extlib/xercesImpl.jar;extlib/commons-logging-1.1.1.jar;extlib/javaosc.jar eu.davidgamez.mas.Main
exit
pause

 -Xprof
-verbose:gc 

:\Users\taropeg>java -X
   -Xmixed           mixed mode execution (default)
   -Xint             interpreted mode execution only
   -Xbootclasspath:<directories and zip/jar files separated by ;>
                     set search path for bootstrap classes and resources
   -Xbootclasspath/a:<directories and zip/jar files separated by ;>
                     append to end of bootstrap class path
   -Xbootclasspath/p:<directories and zip/jar files separated by ;>
                     prepend in front of bootstrap class path
   -Xnoclassgc       disable class garbage collection
   -Xincgc           enable incremental garbage collection
   -Xloggc:<file>    log GC status to a file with time stamps
   -Xbatch           disable background compilation
   -Xms<size>        set initial Java heap size
   -Xmx<size>        set maximum Java heap size
   -Xss<size>        set java thread stack size
   -Xprof            output cpu profiling data
   -Xfuture          enable strictest checks, anticipating future default
   -Xrs              reduce use of OS signals by Java/VM (see documentation)
   -Xcheck:jni       perform additional checks for JNI functions
   -Xshare:off       do not attempt to use shared class data
   -Xshare:auto      use shared class data if possible (default)
   -Xshare:on        require using shared class data, otherwise fail.
