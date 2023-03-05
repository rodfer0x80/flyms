# flyd

## vision
decent looking daw, load files and split them for editing
chatbot for text to control editing eg u load a music file and tell it to extend the beat or change tempo and bass notes
for this we probly need to train the chatbot speciffically and give it control cmds accordingly
u can also edit manually
then u shud be able to play music in loop, chatbot shud understand and get cmds with timestaps as well so loop 0:43 to end thus skip intro for eg

interface shud run in electron so its clean and xplatform
finally all the backend cmds are from the mas module which is inspired by mas from david gamez

this generates music for new projects u can then save it to a file and load it into another project after making ur edits or before, shud take data from text
https://github.com/lucidrains/musiclm-pytorch

then when we load it we use this thingy
https://github.com/deezer/spleeter
it will split the music and load it into mas

after that we can actually control the daw with some commands using the chatbot or manually
u can also use timestamps and ask it to loop or manually
u can also save the file now and u ve made a track

this will all unfortunetly be done in python but if we have time we will start converting stuff to C or golang

also
https://www.kaggle.com/datasets/googleai/musiccaps
https://arxiv.org/pdf/2301.11325.pdf
https://github.com/lucidrains/musiclm-pytorch
## Architecture
````
Control:
this is same as mas
https://pkg.go.dev/gitlab.com/gomidi/midi/player
https://github.com/gomidi/midi

Interface:
start - starts app
quit - quits app
repair - reinstalls midi sounds and dependencies
install - installs midi sounds and dependencies
qt gui control with arrows keys and enter
on top u ve menu to create new agent and stuff
bottom u ve agents
similar to mas but cleaner
call controller mas functions
https://therecipe.github.io/qt/
https://github.com/therecipe/qt

Vision:
detect horizontal and vertical movements and click tap
pass this to interface
https://gocv.io/

````
