#ifndef TARGET_H
#define TARGET_H

#include <Arduino.h>
#include <ESP32Servo.h>
#include <sys/time.h>
#include <stdlib.h>
#include <vector>

class Target : public Servo {
public:
    bool hitFlag = false;
    bool nextTargetFlag = false;
    bool activeFlag = false;

    uint reactionTime = 0;
    int nextTarget = -3;
    int upInterval = 0;
    int downInterval = 0;
    int upIntervalRange = 1;
    int downIntervalRange = 1;
    uint64_t switchTime = 0;
    std::vector<uint> deltaTimes;
    int hitCount = 0;

    Target();
    void activate();
    void deactivate();
    void resetTarget();
    void startInterval();
    void onHit(int64_t endtime);
    void loop();
    void setSwitchTime();

    std::string getData(int i);

private:
    static void hitInterrupt();
    struct timeval tv_now;

    int pin;
    uint64_t timer = 0;

    int actCount = 0;
};

#endif
