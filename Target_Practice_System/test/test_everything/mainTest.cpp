// #include <Arduino.h>
// #include <BLEDevice.h>
// #include <BLEUtils.h>
// #include <BLEServer.h>
// #include <vector>
// #include <ESP32Servo.h>
//
// #include "Target.h"
// #include "SlaveController.h"
//
// #define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
// #define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
//
// #define TARGET_COUNT 2
// const int PINS[] = {22, 23, 19, 21}; // Recommended PWM GPIO pins on the ESP32 include 2,4,12-19,21-23,25-27,32-33
//
// // std::vector<Target> targets;
//
// // class MyCallbacks : public BLECharacteristicCallbacks {
// //     void onWrite(BLECharacteristic *pCharacteristic) {
// //         std::string value = pCharacteristic->getValue();
// //     }
// //
// //     void onRead() {
// //         Serial.print("onRead");
// //     }
// // };
//
// portMUX_TYPE mux = portMUX_INITIALIZER_UNLOCKED;
//
// void IRAM_ATTR button1Click() {
//     portENTER_CRITICAL_ISR(&mux);
//     // targets[0].onHit();
//     portEXIT_CRITICAL_ISR(&mux);
// }
//
// void IRAM_ATTR button2Click() {
//     portENTER_CRITICAL_ISR(&mux);
//     // targets[1].onHit();
//     portEXIT_CRITICAL_ISR(&mux);
// }
//
// void setup() {
//     // ESP32PWM::allocateTimer(0);
// 	// ESP32PWM::allocateTimer(1);
// 	// ESP32PWM::allocateTimer(2);
// 	// ESP32PWM::allocateTimer(3);
//     Serial.begin(9600);
//
//     /***   Target Initialization   ***/
//     // for (int i = 0; i < TARGET_COUNT; i++) {
//     //     targets.push_back(Target());
//     // }
//     //
//     // for (int i = 0; i < TARGET_COUNT; i ++) {
//     //     targets[i].setPeriodHertz(50);
//     //     targets[i].attach(PINS[i]);
//     // }
//
//     /***   TEMP: Button Initialization   ***/
//     // const byte button1 = 5;
//     // const byte button2 = 18;
//     //
//     // pinMode(button1, INPUT_PULLUP);
//     // pinMode(button2, INPUT_PULLUP);
//     // attachInterrupt(digitalPinToInterrupt(button1), button1Click, FALLING);
//     // attachInterrupt(digitalPinToInterrupt(button2), button2Click, FALLING);
//     // attachInterrupt(button1, button1Click, FALLING);
//     // attachInterrupt(button2, button2Click, FALLING);
//
//     // BLEDevice::init("ESP32_Controller");
//     // BLEServer *pServer = BLEDevice::createServer();
//     // BLEService *pService = pServer->createService(SERVICE_UUID);
//     //
//     // BLECharacteristic *pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE);
//     //
//     // pCharacteristic->setCallbacks(new MyCallbacks());
//     // pCharacteristic->setValue(String(TARGET_COUNT).c_str());
//     // pService->start();
//     //
//     // BLEAdvertising *pAdvertising = pServer->getAdvertising();
//     // pAdvertising->start();
// }
//
// void loop() {
//     /***   TEMP: Piezo Disks   ***/
//     // int value = analogRead(32);
//     //
//     // if (value > 50) {
//     //     Serial.println(value);
//     // }
//
//     /***   TEMP: Target Activation   ***/
//     // for (int i = 0; i < TARGET_COUNT; i++) {
//     //     if (!targets[i].hitFlag) {
//     //         targets[i].deactivate();
//     //     }
//     // }
//
//     // targets[0].activate();
//     // delay(1000);
//     // targets[0].deactivate();
//     // delay(1000);
//     // targets[1].activate();
//     // delay(1000);
//     // targets[1].deactivate();
//     // delay(1000);
// }
