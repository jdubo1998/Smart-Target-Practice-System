#ifndef SLAVECONTROLLER_H
#define SLAVECONTROLLER_H

#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <ESP32Servo.h>
#include <sys/time.h>
#include <regex>
#include <vector>

#include "Target.h"

// class SlaveController {
class SlaveController : public BLECharacteristicCallbacks {
public:
    bool inSessionFlag = false;

    SlaveController(int targetCount, BLEUUID serviceUUID, BLEUUID characteristicUUID);
    void startPiezoDetection();
    void startBLE();
    void loop();

private:
    TaskHandle_t detectPiezoDisksTask;
    static QueueHandle_t queueTimes;
    static QueueHandle_t queueTargets;
    static void detectPiezoDisks(void * paramater);
    static int queueItems;

    const int PWM_PINS[4] = {22, 23, 19, 21}; // Recommended PWM GPIO pins on the ESP32 include 2,4,12-19,21-23,25-27,32-33
    const int TARGET_COUNT;
    const BLEUUID SERVICE_UUID;
    const BLEUUID CHARACTERISTIC_UUID;
    BLECharacteristic *pCharacteristic;
    int offset = 0;
    std::string buff;
    std::vector<Servo> servos;
    std::vector<Target> targets;
    struct timeval tv_now;
    uint64_t curTime;

    void sendResponse();
    void onWrite(BLECharacteristic *pCharacteristic);
    void onRead(BLECharacteristic *pCharacteristic);

    void resetController();

    void OrderLoop(int index);
    void IntervalLoop(int index);
};

#endif
