# load pyqt widgets
from PyQt5.QtWidgets import * 
import sys


class Window(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("hello dev!")
        self.setGeometry(0, 0, 200, 200)
        self.label = QLabel("Hello World", self)
        self.show()


class Interface():
    def __init__(self):
        self.app = QApplication(sys.argv)

    def bootstrap(self):
        window = Window() 
        self.app.exec()
        return 0

def main():


if __name__ == '__main__':
    iface = Interface()
    iface.bootstrap()
    sys.exit(0)
