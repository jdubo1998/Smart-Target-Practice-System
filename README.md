# Directories
### Target_System_Master_Contoller
Master controller, central hub which sends the signal to slave controller to raise their targets and communicated with the Android application.
- Device: Raspberry Pi
- Language: Python
- Technologies:
  - BLE: Communication with the Slave controllers.
  - Sockets: Communication with the Android applicaiton.

### Target_Practice_System
Slave controllers, used to controll the target motors, which pop up and down and send information about the if target was hit and at what time.
- Device: ESP32
- Language: C++
- Technologies:
  - BLE: Communication with the Master controller.
  - Servos: Used to controll the rotation of the target arm.
  - Piezo Disks: Pressure disks which generate a current when compressed, used to detect when the target was hit.

### TargetPracticeSystemController
Phone application, used to create and start a practice session.
- Device: Android Phone
- Language: Java
