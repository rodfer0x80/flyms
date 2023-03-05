# flyms
> generate AI music, split it and quickly edit on the fly

## Purpose
I see a lot of music splitters but they are all paid
AI music is theoretically cool progress but it technically sucks
We can make it suckless by splitting the sound and mixing different sounds together
If you have a paid license for a fancy DAW and are used to it this is probably a bit useless
But if you just want to make some cool sounds on the fly it's a fun tool anyway

## Architecture
> make things simple 
MLGen -> generate music using googles AI system
Splitter -> split sounds intro different tracks
MAS -> loop play sounds and make quick edits, control timestamps, adjust tempo and pitch, mute or play specific tracks,
    save and load files containing only a track or an entire sound  into wav, mp3, mp4, webm
Interface -> xplatform frontend
