
class Motion:
    def __init__(self, configs=""):
        self.configs = configs

    def parseGesture(self, tip, cx, cy):
        print(tip, cx, cy)
