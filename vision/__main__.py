import cv2
import mediapipe as mp

mp_drawing = mp.solutions.drawing_utils
mp_hands = mp.solutions.hands

# Initialize hand tracking
hands = mp_hands.Hands(static_image_mode=False,
                       max_num_hands=1, min_detection_confidence=0.7)

# Initialize OpenCV camera
cap = cv2.VideoCapture(0)

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
