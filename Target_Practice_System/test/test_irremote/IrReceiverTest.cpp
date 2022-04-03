// #include <Arduino.h>
// #include <IRremote.h>
//
// const int RECV_PIN = 17;
//
// void setup() {
//     Serial.begin(9600);
//     IrReceiver.begin(RECV_PIN, ENABLE_LED_FEEDBACK);
// }
//
// void loop() {
//     if (IrReceiver.decode()) {
//         Serial.print("Command received: ");
//         Serial.println(IrReceiver.decodedIRData.command);
//         IrReceiver.resume();
//     }
// }
