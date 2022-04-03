#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <vector>
#include <ESP32Servo.h>

// #include "Target.h"
#include "SlaveController.h"

#define SERVICE_UUID        "0ced1a90-a3de-11eb-bcbc-0242ac130002"
#define CHARACTERISTIC_UUID "10b5fdc2-a3de-11eb-bcbc-0242ac130002"

// #define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
// #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

#define TARGET_COUNT 1

SlaveController SC = SlaveController(TARGET_COUNT, BLEUUID(SERVICE_UUID), BLEUUID(CHARACTERISTIC_UUID));

void setup() {
    ESP32PWM::allocateTimer(0);
	ESP32PWM::allocateTimer(1);
	ESP32PWM::allocateTimer(2);
	ESP32PWM::allocateTimer(3);
    Serial.begin(9600);Serial.println("\n\n");
    SC.startBLE();
    SC.startPiezoDetection();
}

void loop() {
    if (SC.inSessionFlag) {
        SC.loop();
    } else {
        delay(3000);
    }
}
