import os
import sys
from random import SystemRandom
from tkinter import *
from tkinter import filedialog
import math
import PySimpleGUI as sg    
import os.path


    print ("Select an image to input")
    CrypImage = image.open()
    #resize the image
    CrypImage = image.resize((400, 400))
    #display the image
    CrypImage.show

