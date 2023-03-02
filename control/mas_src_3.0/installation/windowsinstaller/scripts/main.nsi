# Script for installing MIDI Agent System on Windows

!include 'LogicLib.nsh'

#------------------------------------------------#
#-----             Basic Information        -----#
#------------------------------------------------#

# Root directory of MIDI Agent System
!define MAS_ROOT ..\..\..

# The name of the installer
Name "MIDI Agent System 3.0"

# The file to write
outFile "../build/mas_setup-3.0.exe"

# The default installation directory 
InstallDir $PROGRAMFILES\mas-3.0


#------------------------------------------------#
#-----              Pages                   -----#
#------------------------------------------------#
Page license
Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles


#------------------------------------------------#
#-----              License                 -----#
#------------------------------------------------#
LicenseData "gpl-2.0.txt"


#------------------------------------------------#
#-----              Shortcuts               -----#
#------------------------------------------------#
# Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"
  SetOutPath $INSTDIR
  CreateDirectory "$SMPROGRAMS\MIDI Agent System"
  CreateShortCut "$SMPROGRAMS\MIDI Agent System\Uninstall.lnk" "$INSTDIR\uninstall.exe" 
  CreateShortCut "$SMPROGRAMS\MIDI Agent System\MAS.lnk" "$INSTDIR\mas.bat" "" "$INSTDIR\images\mas_64.ico" 
SectionEnd

Section "Desktop Shortcut"
  SetOutPath $INSTDIR
  CreateShortCut "$DESKTOP\MAS.lnk" "$INSTDIR\mas.bat" "" "$INSTDIR\images\mas_64.ico" 
SectionEnd

Section "Quick Launch Shortcut"
  SetOutPath $INSTDIR
  CreateShortCut "$QUICKLAUNCH\MAS.lnk" "$INSTDIR\mas.bat" "" "$INSTDIR\images\mas_64.ico" 
SectionEnd

#----------------------------------------------------#
#-----     Include Java Runtime Environment     -----#
#----------------------------------------------------#
Var INCLUDE_JRE
Section "Java Runtime Environment"
	StrCpy $INCLUDE_JRE "true"
SectionEnd


#------------------------------------------------#
#-----              Files                   -----#
#------------------------------------------------#
Section ""

  # Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  # List of files to install
  !include files.nsh
  
  # Output the uninstaller
  WriteUninstaller "uninstall.exe"
  
SectionEnd # end the section


#------------------------------------------------#
#-----              Uninstall               -----#
#------------------------------------------------#
Section "Uninstall"

#  MessageBox MB_OK "$SMPROGRAMS"
	
  # Remove files at the root of the installation directory
  Delete $INSTDIR\mas.bat
  Delete $INSTDIR\mas.conf
  Delete $INSTDIR\uninstall.exe
  
  # Remove shortcuts
  Delete "$DESKTOP\MAS.lnk"
  Delete "$QUICKLAUNCH\MAS.lnk"
  Delete "$SMPROGRAMS\MIDI Agent System\MAS.lnk"
  Delete "$SMPROGRAMS\MIDI Agent System\Uninstall.lnk"
  RMDir "$SMPROGRAMS\MIDI Agent System"

  # Remove directories 
  RMDir /r $INSTDIR\spring
  RMDir /r $INSTDIR\plugins
  RMDir /r $INSTDIR\lib
  RMDir /r $INSTDIR\jre
  RMDir /r $INSTDIR\images
  RMDir /r $INSTDIR\extlib
  RMDir $INSTDIR

SectionEnd


