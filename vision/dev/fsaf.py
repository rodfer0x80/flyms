# import cv2
# import mediapipe as mp

# # Initialize MediaPipe Hand Tracking solution
# mp_hands = mp.solutions.hands

# # Initialize video capture
# cap = cv2.VideoCapture(0)

# # Initialize MediaPipe drawing utilities
# mp_drawing = mp.solutions.drawing_utils

# # Initialize gesture recognizer
# gesture_recognizer = mp_hands.HandLandmarkRecognizer(
#     static_image_mode=False,
#     max_num_hands=1,
#     min_detection_confidence=0.7,
#     min_tracking_confidence=0.7)

# # Define a function to recognize hand motion


# def recognize_hand_motion(hand_landmarks):
#     # Get coordinates of wrist and thumb tip
#     wrist_x = hand_landmarks.landmark[mp_hands.HandLandmark.WRIST].x
#     wrist_y = hand_landmarks.landmark[mp_hands.HandLandmark.WRIST].y
#     thumb_x = hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP].x
#     thumb_y = hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP].y

#     # Calculate distance between thumb and wrist
#     distance = ((thumb_x - wrist_x) ** 2 + (thumb_y - wrist_y) ** 2) ** 0.5

#     # Determine hand motion based on thumb and wrist coordinates and distance
#     if thumb_y < wrist_y and distance > 0.1:
#         return "Up"
#     elif thumb_y > wrist_y and distance > 0.1:
#         return "Down"
#     elif thumb_x < wrist_x and distance > 0.1:
#         return "Left"
#     elif thumb_x > wrist_x and distance > 0.1:
#         return "Right"
#     elif distance <= 0.1 and hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP].y < hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].y:
#         return "Thumbs down"
#     elif distance <= 0.1 and hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP].y > hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].y:
#         return "Thumbs up"
#     else:
#         return ""


# # Run hand motion recognition on live camera feed
# while True:
#     # Read frame from camera
#     ret, frame = cap.read()

#     # Convert image to RGB format
#     image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

#     # Process image with MediaPipe Hand Tracking solution
#     results = gesture_recognizer.process(image)

#     # Check if hand is detected
#     if results.multi_hand_landmarks:
#         # Draw hand landmarks on image
#         for hand_landmarks in results.multi_hand_landmarks:
#             mp_drawing.draw_landmarks(
#                 image, hand_landmarks, mp_hands.HAND_CONNECTIONS)

#         # Recognize hand motion from hand landmarks
#         hand_motion = recognize_hand_motion(hand_landmarks)

#         # Print recognized hand motion to screen
#         if hand_motion:
#             print(hand_motion)

#     # Display image on screen
#     cv2.imshow('MediaPipe Hand Tracking',
#                cv2.cvtColor(image, cv2.COLOR_RGB2BGR))

#     # Wait for user to press 'q' to exit
#     if cv2.waitKey(1) & 0xFF == ord('q'):
#         break

# # Release resources
# cap.release()
# cv2.destroyAllWindows()
import cv2
import mediapipe as mp
from mediapipe.python.solutions import drawing_utils as mp_drawing_utils

# Initialize MediaPipe Hand Tracking solution
mp_hands = mp.solutions.hands

# Initialize video capture
cap = cv2.VideoCapture(0)

# Initialize variables for gesture recognition
min_distance = 30
prev_x, prev_y = None, None

# Define function to recognize gesture from hand motion


def recognize_gesture(x, y):
    global prev_x, prev_y
    if prev_x is None or prev_y is None:
        prev_x, prev_y = x, y
        return None

    dx = x - prev_x
    dy = y - prev_y

    if dx > min_distance and abs(dy) < min_distance:
        prev_x, prev_y = x, y
        return "Right"
    elif dx < -min_distance and abs(dy) < min_distance:
        prev_x, prev_y = x, y
        return "Left"
    elif dy > min_distance and abs(dx) < min_distance:
        prev_x, prev_y = x, y
        return "Down"
    elif dy < -min_distance and abs(dx) < min_distance:
        prev_x, prev_y = x, y
        return "Up"
    else:
        return None

# Define function to recognize gesture from hand landmarks


def recognize_gesture_from_landmarks(hand_landmarks):
    thumb_tip = hand_landmarks.landmark[mp_hands.HandLandmark.THUMB_TIP]
    index_tip = hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP]
    middle_tip = hand_landmarks.landmark[mp_hands.HandLandmark.MIDDLE_FINGER_TIP]
    pinky_tip = hand_landmarks.landmark[mp_hands.HandLandmark.PINKY_TIP]

    # Check if thumb is up or down
    if thumb_tip.y < index_tip.y and thumb_tip.y < middle_tip.y and thumb_tip.y < pinky_tip.y:
        return "Thumbs up"
    elif thumb_tip.y > index_tip.y and thumb_tip.y > middle_tip.y and thumb_tip.y > pinky_tip.y:
        return "Thumbs down"
    else:
        return None


# Run gesture recognition on live camera feed
with mp_hands.Hands(
        min_detection_confidence=0.5,
        min_tracking_confidence=0.5) as gesture_recognizer:
    while True:
        # Read frame from camera
        ret, frame = cap.read()

        # Convert image to RGB format
        image = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Process image with MediaPipe Hand Tracking solution
        results = gesture_recognizer.process(image)

        # Check if hand is detected
        if results.multi_hand_landmarks:
            # Get first detected hand
            hand_landmarks = results.multi_hand_landmarks[0]

            # Draw hand landmarks on image
            mp_drawing_utils.draw_landmarks(
                image, hand_landmarks, mp_hands.HAND_CONNECTIONS)

            # Recognize gesture from hand motion
            # x, y = hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].x, hand_landmarks.landmark[mp_hands.HandLandmark.INDEX_FINGER_TIP].y
            # gesture = recognize_gesture(x * image.shape[1], y * image.shape[0])

            # Recognize gesture from hand landmarks
            gesture = recognize_gesture_from_landmarks(hand_landmarks)

            # Print recognized gesture to screen
            if gesture is not None:
                print(gesture)
        else:
            # Reset variables for gesture recognition
            prev_x, prev_y = None, None

        # Display image on screen
        cv2.imshow('MediaPipe Hand Tracking',
                   cv2.cvtColor(image, cv2.COLOR_RGB2BGR))

        # Wait for user to press 'q'
        if cv2.waitKey(10) & 0xFF == ord('q'):
            break
# Release video capture and destroy windows
cap.release()
cv2.destroyAllWindows()
