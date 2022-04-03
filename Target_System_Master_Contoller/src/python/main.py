from bluetooth.ble import DiscoveryService
from bluetooth.ble import GATTRequester
from MasterController import MasterController
import time
import re

MC = MasterController()

def main():
    while (True):
        MC.loop()
        time.sleep(0.01)

if __name__ == "__main__":
    main()
