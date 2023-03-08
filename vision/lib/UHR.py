from .logger import *

class UniversalHandRecognition:
    def __init__(self, config):
        self.config = config
        self.logger = Logger()

    def loopForever(self):
        while True:
            # get gestures from camera
            gesture = self.readGesture()
            self.send(gesture)
        return 0

    def send(self, data):
        logger.debug(f"{self.config['con']}, {data})
