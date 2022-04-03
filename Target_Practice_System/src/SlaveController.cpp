#include "SlaveController.h"

QueueHandle_t SlaveController::queueTimes;
QueueHandle_t SlaveController::queueTargets;
int SlaveController::queueItems = 0;

SlaveController::SlaveController (int targetCount, BLEUUID serviceUUID, BLEUUID characteristicUUID)
                : TARGET_COUNT(targetCount), SERVICE_UUID(serviceUUID), CHARACTERISTIC_UUID(characteristicUUID) {
    /***  Target Intialization  ***/
    for (int i = 0; i < TARGET_COUNT; i++) {
        targets.push_back(Target());
    }

    for (int i = 0; i < TARGET_COUNT; i ++) {
        targets[i].setPeriodHertz(50);
        targets[i].attach(PWM_PINS[i]);
    }

    struct timeval tv_now;
    gettimeofday(&tv_now, NULL);
    srand ((uint) tv_now.tv_usec); // Initialize random seed using current system time.

    queueTimes = xQueueCreate(10, sizeof(uint64_t)); // Creates a queue of size 10 each holding 4 bytes of information.
    queueTargets = xQueueCreate(10, sizeof(int)); // Creates a queue of size 10 each holding 4 bytes of information.
    if (xQueueSend(queueTargets, (int*) &TARGET_COUNT, (TickType_t) 20) != pdPASS ) {
        Serial.println("Failed to send TARGET_COUNT to queueTargets.");
    }
}

void SlaveController::resetController() {
    pCharacteristic->setValue(String("tc;"+String(TARGET_COUNT)).c_str());

    for (int i = 0; i < TARGET_COUNT; i ++) {
        targets[i].resetTarget();
    }
}

void SlaveController::startBLE() {
    /***  Bluetooth Intialization  ***/
    BLEDevice::init("STPS_SC_XXX"); // TODO: Change this to an actual numbering system.
    BLEDevice::setMTU(517);

    BLEServer *pServer = BLEDevice::createServer();
    BLEService *pService = pServer->createService(SERVICE_UUID);

    pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID,
                    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);

    pCharacteristic->setCallbacks(this);
    pService->start();

    BLEAdvertising *pAdvertising = pServer->getAdvertising();
    pAdvertising->start();

    resetController();
}

void SlaveController::onWrite(BLECharacteristic *pCharacteristic) {
    buff = pCharacteristic->getValue();

    // if (buff[0] != '\0') {
    //     std::string str = buff;
    //     str.resize(10, ' ');
    //
    //     Serial.print("| ");
    //     Serial.print(str.c_str());
    //     Serial.println("|");
    // }

    sendResponse();
}

void SlaveController::onRead(BLECharacteristic *pCharacteristic) {
    // Code
}

void SlaveController::sendResponse() {
    std::smatch matches;
    bool ready = false;

    /* Set the offset for the slave controller. */
    while (std::regex_search (buff, matches, std::regex("off;([0-9]+)"))) {
        offset = atoi(matches.str(1).c_str()); // Target
        Serial.print("offset: " + String(offset));
        buff = buff.erase(matches.position(0), matches.length(0)); // Iterates to next match.
    }

    /* Set the next target parameter. */
    while (std::regex_search (buff, matches, std::regex("nt;([0-9]+);(-*[0-9]+)"))) {
        int t = atoi(matches.str(1).c_str()) - offset; // Target
        int i = atoi(matches.str(2).c_str()); // Next target
        targets[t].nextTarget = i;

        Serial.println(String("target: " + String(t+offset) + " (" + String(t) + ")   nextTarget: " + String(targets[t].nextTarget)));

        ready = true;
        buff = buff.erase(matches.position(0), matches.length(0)); // Iterates to next match.
    }

    /* Set the timer interval paramaters. */
    while (std::regex_search (buff, matches, std::regex("ti;([0-9]+);([0-9]+);([0-9]+);([0-9]+);([0-9]+)"))) {
        int t = atoi(matches.str(1).c_str()) - offset; // Target
        int i = atoi(matches.str(2).c_str()) * 1000; // Up interval
        int j = atoi(matches.str(3).c_str()) * 1000; // Down interval
		int k = atoi(matches.str(4).c_str()) * 1000 + 1; // Up interval range
		int l = atoi(matches.str(5).c_str()) * 1000 + 1; // Down interval range
        targets[t].upInterval = i;
        targets[t].downInterval = j;
		targets[t].upIntervalRange = k;
        targets[t].downIntervalRange = l;

        Serial.print(String("target: " + String(t+offset) + " (" + String(t) + ")"));
        Serial.print(String("   upInterval: " + String(targets[t].upInterval) + "   downInterval: " + String(targets[t].downInterval)));
        Serial.println(String("   upIntervalRange: " + String(targets[t].upIntervalRange) + "   downIntervalRange: " + String(targets[t].downIntervalRange)));

        ready = true;
        buff = buff.erase(matches.position(0), matches.length(0)); // Iterates to next match.
    }

    /* Sends a ready singal only when the sequence parameters are updated. */
    if (ready) {
        pCharacteristic->setValue("ready");
    }

    /* When the begin signal is received send the signal to activate the first target. */
    while (std::regex_search (buff, matches, std::regex("begin"))) {
        Serial.println("begin");
        inSessionFlag = true;

        /* Only the first slave controller sends the signal to get the first target in the sequence. The rest clear the BLE characteristic value. */
        if (offset == 0) {
            pCharacteristic->setValue("next;-2;-2;-2");
        } else {
            pCharacteristic->setValue("");
        }

        for (int i = 0; i < TARGET_COUNT; i++) {
            targets[i].setSwitchTime();
        }

        buff = matches.suffix().str(); // Iterates to next match.
    }

    /* When running the session, if a target belongs to this slave controller, the master controller with send this activate signal. */
    while (std::regex_search (buff, matches, std::regex("act;([0-9]+)"))) {
        int t = atoi(matches.str(1).c_str()); // Target to activate.

        Serial.println(String("activate target: " + String(t+offset) + " (" + String(t) + ")"));

        targets[t].activate();
        pCharacteristic->setValue(""); // Clears the characteristic value.
        buff = matches.suffix().str(); // Iterates to next match.
    }

    /* When the slave controller receives the end signal, it resets itself. */
    while (std::regex_search (buff, matches, std::regex("end"))) {
        // std::string data; // Hit Count
        //
        // for (int i = 0; i < TARGET_COUNT; i++) {
        //     data.append("data;").append(targets[i].getData(i + offset));
        //
        //     if (i < TARGET_COUNT - 1) {
        //         data.append(";");
        //     }
        // }

        resetController();

        // Serial.print("Data Sent: ");
        // Serial.println(data.c_str());

        // pCharacteristic->setValue(data);
        buff = matches.suffix().str(); // Iterates to next match.
        inSessionFlag = false;
    }
}

void SlaveController::loop() {
    for (int i = 0; i < uxQueueMessagesWaiting(queueTimes); i++) {
        int target = 0;
        uint64_t endtime = 0;

        xQueueReceive(queueTargets, &target, (TickType_t) 10);
        xQueueReceive(queueTimes, &endtime, (TickType_t) 10);
        // if (xQueueReceive(queueTargets, &target, (TickType_t) 10) != pdPASS) {
        //     Serial.println("Failed to receive target from queueTargets.");
        // }
        // if (xQueueReceive(queueTimes, &endtime, (TickType_t) 10) != pdPASS) {
        //     Serial.println("Failed to receive endTime from queueTimes.");
        // }

        targets[target].onHit(endtime);

        Serial.println(String("Target " + String(target+offset) + " (" + String(target) + ") hit receieved."));
    }

    gettimeofday(&tv_now, NULL);
    curTime = (uint64_t)tv_now.tv_sec * 1000000 + (uint64_t)tv_now.tv_usec;

    for (int i = 0; i < TARGET_COUNT; i++) {
        if (targets[i].switchTime > 0 && curTime > targets[i].switchTime) {
            if (targets[i].activeFlag) {
                targets[i].deactivate();
            } else {
                targets[i].activate();
            }
        }

        if (targets[i].nextTargetFlag) {
            pCharacteristic->setValue(String("next;" + String(i + offset) + ";" + String(targets[i].reactionTime) + ";" + String(targets[i].nextTarget)).c_str());
            targets[i].hitFlag = false;
            targets[i].nextTargetFlag = false;
        }
    }
}

void SlaveController::detectPiezoDisks(void * paramater) {
    std::vector<int> PIEZO_PINS = {32, 33, 25, 26};
    int targetCount = 0;


    xQueueReceive(queueTargets, &targetCount, (TickType_t) 10);
    // if (xQueueReceive(queueTargets, &targetCount, (TickType_t) 10) != pdPASS) {
    //     Serial.println("Failed to receive TARGET_COUNT from queueTargets.");
    // }

    bool pauseHit[targetCount] = {false};

    int debugPin = 5;
    pinMode(debugPin, INPUT);
    for (;;) {
        for (int i = 0; i < targetCount; i++) {
            int value = analogRead(PIEZO_PINS[i]);

            Serial.print("Value: ");
            Serial.print(value);
            Serial.print("   pauseHit: ");
            Serial.print(pauseHit[i]);
            if (value < 50 || pauseHit[i]) {
                Serial.println("");
            }

            if (value > 50 && !pauseHit[i]) {
                struct timeval tv_now;
                gettimeofday(&tv_now, NULL);
                uint64_t endtime = (uint64_t)tv_now.tv_sec * 1000000 + (uint64_t)tv_now.tv_usec;

                Serial.print(String("Hit Detected on target " + String(i)));
                pauseHit[i] = true;

                xQueueSend(queueTargets, (int*) &i, (TickType_t) 20);
                xQueueSend(queueTimes, (uint64_t*) &endtime, (TickType_t) 20);
                // if (xQueueSend(queueTargets, (int*) &i, (TickType_t) 20) != pdPASS ) {
                //     Serial.print("Failed to send ");
                //     Serial.print(i);
                //     Serial.println(" to queueTargets.");
                // }
                // if (xQueueSend(queueTimes, (uint64_t*) &endtime, (TickType_t) 20) != pdPASS ) {
                //     Serial.print("Failed to send ");
                //     Serial.print(endtime);
                //     Serial.println(" to queueTimes.");
                // }

                queueItems++;
            } else if(value < 50 && pauseHit[i]) {
                pauseHit[i] = false;
            }
        }
        delay(30);
    }
}

void SlaveController::startPiezoDetection() {
    xTaskCreatePinnedToCore(this->detectPiezoDisks, // Function
                            "detectPiezoDisksTask", // Name
                            2500, // Stack size
                            this, // Parameters
                            0, // Priority
                            &detectPiezoDisksTask, // Task handle
                            0); // Core to run on
}
