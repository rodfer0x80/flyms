import cv2
import mediapipe as mp

mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands

# Initialize hand tracking
hands = mp_hands.Hands(static_image_mode=False,
                       max_num_hands=1, min_detection_confidence=0.7)

# Initialize OpenCV camera
cap = cv2.VideoCapture(0)

<<<<<<< HEAD
# Define variables to track hand position
prev_x = 0
prev_y = 0

while True:
    ret, frame = cap.read()

    # Convert image to RGB for Mediapipe
    image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

    # Detect hand landmarks
    results = hands.process(image)

    # Check if hand is detected
    if results.multi_hand_landmarks:

        # Get hand landmarks
        hand_landmarks = results.multi_hand_landmarks[0]

        # Get hand position
        curr_x = hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].x
        curr_y = hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].y

        # Determine hand gesture
        if curr_x+1 > prev_x:
            gesture = 'Slide right'
        elif curr_x-1 < prev_x:
            gesture = 'Slide left'
        elif curr_y-1 < prev_y:
            gesture = 'Slide up'
        elif curr_y+1 > prev_y:
            gesture = 'Slide down'
        elif hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP].x > 1.1*hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].x:
            gesture = 'Open hand'
        else:
            gesture = 'Closed hand'

        # Print hand gesture to screen
        cv2.putText(frame, gesture, (50, 50),
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        # Update previous hand position
        prev_x = curr_x
        prev_y = curr_y

    # Show video stream
    cv2.imshow('Hand gesture detection', frame)

    # Exit program when 'q' is pressed
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release OpenCV camera and Mediapipe hands
cap.release()
hands.close()
=======
if __name__ == '__main__':
    # TODO compile from source and run it on gpu
    logger = Logger(filename="debug.log")
    motion = Motion()
    mp_drawing = mp.solutions.drawing_utils
    mp_drawing_styles = mp.solutions.drawing_styles
    mp_hands = mp.solutions.hands
    # For webcam input:
    cap = cv2.VideoCapture(0)
    with mp_hands.Hands(
            model_complexity=0,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5) as hands:
        while cap.isOpened():
            success, image = cap.read()
            if not success:
                # Ignoring empty camera frame
                # If loading a video, use 'break' instead of 'continue'.
                continue
            # To improve performance, optionally mark the image as not writeable to
            # pass by reference.
            image.flags.writeable = False
            image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            results = hands.process(image)
            # Draw the hand annotations on the image.
            image.flags.writeable = True
            image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR)
            if results.multi_hand_landmarks:
                for hand_landmarks in results.multi_hand_landmarks:
                    for tip, lm in enumerate(hand_landmarks.landmark):
                        # keep track of all fingers for every fram
                        # if for 30 frames it moves 30% of screen size
                        # do slide and ignore next 30 frames
                        # repeat
                        # cmds: 1 hand left,right,up,down slides
                        # close hand to select
                        # 2 hand slide bar
                        h, w, c = image.shape
                        cx, cy = int(lm.x*w), int(lm.y*h)
                        logger.debug(f"{tip}, {cx}, {cy}")
                        gesture = motion.parseGesture(tip, cx, cy)
                        logger.debug(gesture)
                    mp_drawing.draw_landmarks(
                        image,
                        hand_landmarks,
                        mp_hands.HAND_CONNECTIONS,
                        mp_drawing_styles.get_default_hand_landmarks_style(),
                        mp_drawing_styles.get_default_hand_connections_style())
            # Flip the image horizontally for a selfie-view display.
            cv2.imshow('MediaPipe Hands', cv2.flip(image, 1))
            if cv2.waitKey(5) & 0xFF == 27:
                break
    cap.release()
    sys.exit(0)
>>>>>>> b3068525c258051020e36e03516387d059bc7cda
