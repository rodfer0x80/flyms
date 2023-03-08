#!/usr/bin/env python3

import os
import sys

from lib.UHR import *

class MASVision:
    def __init__(self):
        config = {
            "name": "MASVision",
            "con": {"host": "127.0.0.1", "port": 5555,},
            "data": [
                {"name": "GESTURE", "gesture": "ALL", "trigger": "self.toggle_test"},
            ],
        }

        self.uhr = UniversalHandRecognition(config)

    def toggle_test(self):
        print("test passed")
        return 0
    
    def bootstrap(self):
        try:
            self.uhr.loopForever()
        except KeyboardInterrupt:
            return 0
        return 1

def main():
    mas_vision = MASVision()
    return mas_vision.bootstrap()


if __name__ == '__main__':
    sys.exit(main())
