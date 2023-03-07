#!/usr/bin/env python3


import sys
import os
import time


import cv2
import mediapipe as mp
# import numpy as np

from lib.logger import Logger


VISION_LOG = "vision_debug.log"

def motionTracker(DEBUG=False):
    logger = Logger(VISION_LOG)
    
    cap = cv2.VideoCapture(0)
    mpHands = mp.solutions.hands
    hands = mpHands.Hands()

    #mpDraw = mp.solutions.drawing_utils
    #cTime = 0
    #pTime = 0

    while True:
        success, img = cap.read()
        imgRGB = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = hands.process(imgRGB)
        if results.multi_hand_landmarks:
            for handlms in results.multi_hand_landmarks:
                for id, lm in enumerate(handlms.landmark):
                    h, w, c = img.shape
                    cx, cy = int(lm.x*w), int(lm.y*h)
                    logger.debug(f"{id}, {cx}, {cy}")
                    #print(id, cx, cy)
                    # if id == 5:
                    # cv2.circle(img, (cx, cy), 15, (139, 0, 0), cv2.FILLED)

                # Time and FPS Calculation
                #mpDraw.draw_landmarks(
                #    img, handlms, mpHands.HAND_CONNECTIONS)

        #cTime = time.time()
        #fps = 1/(cTime-pTime)
        #pTime = cTime
        #cv2.putText(img, str(int(fps)), (10, 70),
        #            cv2.FONT_HERSHEY_SIMPLEX, 3, (139, 0, 0), 3)
        #            #cv2.imshow("Image", img)
        cv2.waitKey(1)


# vision opencv module rpc connected to interface
def main():
    motionTracker()
    return 0


if __name__ == '__main__':
    sys.exit(main())
