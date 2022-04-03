from bluepy.btle import Scanner, DefaultDelegate, Peripheral, BTLEException, ADDR_TYPE_RANDOM, ADDR_TYPE_PUBLIC
from SlaveControllerMirror import SlaveControllerMirror
from SocketServerThread import SocketServerThread
import random
import re
import time

class MasterController:
    socketServerThread = None

    inSession = False
    phoneConnected = False
    waitingForReady = False # Waiting for all slave controllers flag.
    begin = False # Begin flag

    endCondition = 0
    timeLimit = 0
    targetLimit = 0

    shotsFired = 0
    actCount = 0
    targetHit = 0
    hitCount = 0
    accuracy = 0.0
    reactionTime = 0
    reactionTimeCount = 0
    reactionTimeAvg = 0.0

    SCMirrors = []
    targetMap = {}
    startingTarget = 0
    targetCount = 0

    def __init__(self):
        self.socketServerThread = SocketServerThread("", 50420)

        self.BLEscan()
        self.readValues()
        self.mapTargets()
        # self.detectExternal()

    def disconnect(self):
        self.SCMirrors.clear()

    def BLEscan(self,scans=5):
        self.disconnect()
        scanner = Scanner(0)

        # Scans 5 time or until a Slave Controller connection is made.
        while len(self.SCMirrors) == 0 and scans > 0:
            scanEntries = scanner.scan(5)
            for scanEntry in scanEntries:
                for (adtype, description, value) in scanEntry.getScanData():
                    if description == 'Complete Local Name' and re.search('STPS_SC_', value):
                        print("Found Slave Controller @ " + scanEntry.addr)
                        self.SCMirrors.append(SlaveControllerMirror(scanEntry)) # Add Slave Controller Mirror to the Slave Controller Mirror array.
            scans -= 1
            time.sleep(3) # Pauses for 3 seconds between each try.
        self.SCCount = len(self.SCMirrors)
        self.SCMirrors[self.SCCount - 1].lastSCM = True

        if self.SCCount == 0:
            print("Failed to connect to any Slave Controllers.") # TODO: Change debugging output system.
            exit()

    def readValues(self):
        prn = "| "
        for SCM in self.SCMirrors:
            SCM.readValue()
            prn += "{:10}".format(SCM.buff) + "| "
            self.sendResponses(SCM)
        if not prn == "|           |           | ":
            print(prn)

    def mapTargets(self):
        offset = 0
        target = 0
        for SCM in self.SCMirrors:
            self.targetCount += SCM.targetCount
            # Maps out each target to their slave controller number.
            for t in range(SCM.targetCount):
                # Offset maps targets to slave controlelr number: [0 1 | 2 3 | 4 5 6 7] : [0 1 | 0 1 | 0 1 2 3] : [0 0 | 1 1 | 2 2 2 2]
                self.targetMap[t+offset] = target
            SCM.offset = offset
            offset += SCM.targetCount # Increases offset for the next slave controller.
            target+=1

    # def detectExternal(self):
    #     while()
    #         socketServerThread = SocketServerThread("", 50420)
    #         socketServerThread.listen()

        # self.parseCode("start;1;;OR;0;1;;OR;1;0")
        # self.parseCode("start;0;;ec;0;;nt;0;1;ti;0;1000;0;0;0;;nt;1;0;ti;1;1000;0;0;0")

    def parseCode(self, code):
        TargetCodes = re.split(";;", code)
        self.startingTarget = int(re.sub("start;", "", TargetCodes[0]))

        match = re.search("ec;([0-9]+);([0-9]+)", code)
        self.socketServerThread.sendMessage(match.group(0))
        self.endCondition = int(match.group(1))

        if self.endCondition == 0:
            self.timeLimit = int(match.group(2))
        elif self.endCondition == 1:
            self.targetLimit = int(match.group(2))

        index = 2
        for SCM in self.SCMirrors:
            SCcode = "off;" + str(SCM.offset)
            for i in range(SCM.targetCount):
                SCcode += ";" + TargetCodes[index]
                index += 1
            SCM.sendValue(SCcode)
        self.waitingForReady = True
        self.inSession = True

    def sendResponses(self, SCM):
        begin = False
        allReady = False
        match = None

        # Response for when the slave controller sends its targetCount.
        if re.search("tc;", SCM.buff):
            SCM.targetCount = int(re.sub("tc;", "", SCM.buff))

        # Send begin singal once all ready signals have been received.
        if self.waitingForReady:
            if SCM.buff == "ready":
                SCM.ready = True

            if SCM.lastSCM:
                allReady = True
                for scm in self.SCMirrors:
                    if scm.ready == False:
                        allReady = False
                        break

            begin = allReady # If all slaves are ready, turn on begin flag.
            self.waitingForReady = not allReady # If all slaves are ready, turn off waitingForReady flag.

        if begin and SCM.lastSCM:
            for scm in self.SCMirrors:
                scm.sendValue("begin");

        # Received next target signal.
        # match = re.search("next;", SCM.buff)
        if re.search("next;", SCM.buff):
            params = re.split(";", SCM.buff)
            curTarget = int(params[1])

            # Checks if the curTarget was hit.
            if int(params[2]) > 0:
                reactionTime = int(int(params[2]) / 1000)
                hitMessage = "hit;" + params[1] + ";" + str(reactionTime)
                self.targetHit+=1
                print(str(self.targetHit) + "   " + str(self.targetLimit))
                if self.targetHit == self.targetLimit:
                    hitMessage += ";exit"
                self.socketServerThread.sendMessage(hitMessage)

            nextTarget = int(params[3])

            if nextTarget == -1:
                 # Makes sure the same target is not randomly selected.
                while nextTarget == -1 and nextTarget == curTarget:
                    nextTarget = random.randint(0, self.targetCount)

            # Sends the activate target signal to the first target in sequence.
            elif nextTarget == -2:
                nextTarget = self.startingTarget
            #elif nextTarget == 3:
            #    nextTarget = 
#HERE


            if nextTarget >= 0:
                # Sends the activate target signal to the next target in sequence.
                self.SCMirrors[self.targetMap[nextTarget]].sendValue("act;{}".format(nextTarget-self.SCMirrors[self.targetMap[nextTarget]].offset))
                self.socketServerThread.sendMessage("act;0")

        if re.search("data;", SCM.buff):
            params = re.split(";", SCM.buff)

            index = 0 # parameter index
            target = 0
            hitCount = 0
            actCount = 0

            for param in params:
                if param == "data":
                    self.hitCount += hitCount
                    self.actCount += actCount

                    index = 1
                    hitCount = 0
                    actCount = 0

                # Target number.
                elif index == 1:
                    target = int(param)
                    index += 1

                # Hit count
                elif index == 2:
                    hitCount = int(param)
                    index += 1

                # Activation count
                elif index == 3:
                    actCount = int(param)
                    index += 1

                # Activation count
                elif index > 3:
                    self.reactionTime += int(param)
                    self.reactionTimeCount += 1
                    index += 1


            if SCM.lastSCM:
                if self.hitCount != self.targetHit:
                    print("Not equal: hitCount: " + str(self.hitCount) + "   targetHit: " + str(self.targetHit))

                self.reactionTimeAvg = self.reactionTime / self.reactionTimeCount
                self.accuracy = self.hitCount / self.shotsFired * 100

    def endSession(self):
        self.inSession = False
        for SCM in self.SCMirrors:
            SCM.sendValue("end")

    def loop(self):
        if self.socketServerThread.isConnected:
            if self.socketServerThread.startSession:
                self.parseCode(self.socketServerThread.code)
                self.socketServerThread.startSession = False

            if self.inSession: # TODO: Rename boolean
                self.readValues()

        elif self.socketServerThread.endSession:
            self.endSession()
            self.socketServerThread.endSession = False

        else:
            self.socketServerThread.listen()
