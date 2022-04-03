from bluepy.btle import Scanner, DefaultDelegate, Peripheral, BTLEException
import re
import time

class SlaveControllerMirror:
    ready = False # Ready flag
    lastSCM = False

    peripheral = None
    service = None
    characteristic = None
    handle = None

    buff = None
    targetCount = 0

    def __init__(self, scanEntry):
        self.peripheral = Peripheral(scanEntry)
        self.peripheral.setMTU(517)

        for service in self.peripheral.getServices():
            if not re.search('^.*-0000-1000-8000-00805f9b34fb', str(service.uuid)):
                self.service = service
                for characteristic in self.service.getCharacteristics():
                    self.characteristic = characteristic
                    self.handle = characteristic.getHandle()

    def activate(self, i):
        print('Under Construction')

    def readValue(self):
        # self.buff = self.requester.read_by_handle(self.handle)[0].decode('utf-8')
        self.buff = self.characteristic.read().decode('utf-8')
        if not self.buff == '':
            self.sendValue('')

    def sendValue(self, value):
        self.characteristic.write(str.encode(value))
        # self.requester.write_by_handle(self.handle, str.encode(value))
