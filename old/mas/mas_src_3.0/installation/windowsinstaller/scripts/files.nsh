# Configuration file
File /oname=mas.conf ${MAS_ROOT}\mas.conf
AccessControl::GrantOnFile "$INSTDIR\mas.conf" "(BU)" "FullAccess"

# Executable file
${If} $INCLUDE_JRE == 'true'
	File /oname=mas.bat ${MAS_ROOT}\bin\mas_jre.bat
${Else}
	File /oname=mas.bat ${MAS_ROOT}\bin\mas_system_jre.bat
${EndIf}

# Java runtime environment
${If} $INCLUDE_JRE == 'true'
	File /r ${MAS_ROOT}\..\jre
${EndIf}

# Spring files
File /r /x .svn ${MAS_ROOT}\spring

# Plugins
File /r /x .svn ${MAS_ROOT}\plugins

# Library files
CreateDirectory $INSTDIR\lib
File /oname=lib\mas.jar ${MAS_ROOT}\lib\mas.jar

# Image files
File /r /x .svn /x *.svg ${MAS_ROOT}\images

# Third party library files
CreateDirectory $INSTDIR\extlib
File /oname=extlib\commons-logging-1.1.1.jar ${MAS_ROOT}\extlib\commons-logging-1.1.1.jar
File /oname=extlib\javaosc.jar ${MAS_ROOT}\extlib\javaosc.jar
File /oname=extlib\org.springframework.asm-3.0.0.RC2.jar ${MAS_ROOT}\extlib\org.springframework.asm-3.0.0.RC2.jar
File /oname=extlib\org.springframework.beans-3.0.0.RC2.jar ${MAS_ROOT}\extlib\org.springframework.beans-3.0.0.RC2.jar
File /oname=extlib\org.springframework.context-3.0.0.RC2.jar ${MAS_ROOT}\extlib\org.springframework.context-3.0.0.RC2.jar
File /oname=extlib\org.springframework.core-3.0.0.RC2.jar ${MAS_ROOT}\extlib\org.springframework.core-3.0.0.RC2.jar
File /oname=extlib\org.springframework.expression-3.0.0.RC2.jar ${MAS_ROOT}\extlib\org.springframework.expression-3.0.0.RC2.jar
File /oname=extlib\xercesImpl.jar ${MAS_ROOT}\extlib\xercesImpl.jar

