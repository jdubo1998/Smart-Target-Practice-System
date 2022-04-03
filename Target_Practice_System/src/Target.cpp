#include "Target.h"

Target::Target() {

}

void Target::resetTarget() {
    deactivate();
    hitFlag = false;
    nextTargetFlag = false;
    activeFlag = false;

    reactionTime = 0;
    nextTarget = -3;
    upInterval = 0;
    downInterval = 0;
    upIntervalRange = 1;
    downIntervalRange = 1;
    switchTime = 0;
}

void Target::activate() {
    write(0);

    if (!activeFlag) {
        reactionTime = 0;
        activeFlag = true;
        setSwitchTime();
        hitFlag = false;
        nextTargetFlag = false;
        actCount++;
    }
}

void Target::deactivate() {
    write(90);
    if (activeFlag) {
        activeFlag = false;
        setSwitchTime();
        nextTargetFlag = true;
    }
}

void Target::onHit(int64_t endTime) {
    if (activeFlag) {
        reactionTime = (uint) (endTime - timer);
        deltaTimes.push_back(reactionTime);
        hitCount++;
        hitFlag = true;
        deactivate();
    }
}

void Target::setSwitchTime() {
    gettimeofday(&tv_now, NULL);
    timer = (uint64_t)tv_now.tv_sec * 1000000 + (uint64_t)tv_now.tv_usec;

    if (upInterval > 0 && activeFlag) {
        switchTime = timer + (rand() % upIntervalRange) + upInterval;
    } else if (downInterval > 0 && !activeFlag) {
        switchTime = timer + (rand() % downIntervalRange) + downInterval;
    } else {
        switchTime = 0;
    }

    Serial.print("switchTime: ");
    Serial.println(switchTime);
}

std::string Target::getData(int i) {
    std::string data(";");

    data.append(String("next;" + String() + ";" + String() + ";" + String()).c_str());
    // data.append(String(i).c_str()).append(";").append(String(hitCount).c_str()).append(";").append(String(actCount).c_str());

    for (int v = 0; v < deltaTimes.size(); v++) {
        data.append(";").append(String(deltaTimes[v]).c_str());
    }

    return data;
}
