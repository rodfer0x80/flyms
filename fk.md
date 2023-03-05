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

this will all unfortunetly be done in python but if we have time we will start converting stuff to C or golang
literally the main goal is an opensource and free app u can generate music with (this part can be useless as ai progress goes on)
we just rly want to be able to visualise and edit ai generated music for free, it doesnt matter if we generate it with a third party
ml algorithm
we just need to ve a free not too bad generator and make the rest good

get the mas core working with the interface and the splitter, after this its 90% done
then get the music gen ai to work with text prompt and load into new project save reload into another project and make edits

new name is flawml
it sucks that u cant make ai generated music worse and more human, but we want to do so

would be good to have a llm chatbot for controllling the daw but we ve no time for tha

the main purpose is to have music files, load them and loop them live for playing, so no vocals and no need for ai generation or editing
the main points are mas the live music player and editor and the splitter which as to be top notch optimisedt 

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
