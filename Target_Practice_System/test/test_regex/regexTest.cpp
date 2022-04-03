#include <iostream>
#include <string>
#include <regex>
#include <vector>
#include <time.h>
#include <stdlib.h>

// std::string T0("OR;0;-1");                             // Target 0:   next: -1   up: .     down: .
// std::string T1("TI;1;1000;2000;0;0");                  // Target 1:   next: .   up: 1s     down: 2s
// std::string T2("OR;2;3;TI;2;3000;5000;2000;2000");     // Target 2:   next: 3   up: 3-5s   down: 5-7s
// std::string T3("OR;3;0;TI;3;3000;0;2000;0");           // Target 3:   next: 0   up: 3-5s   down: .
//
// std::string buff = T0 + T1 + T2 + T3;
std::string buff("start;0;;OR;0;1;TI;0;1000;0;0;0;;OR;1;0;TI;1;1000;0;0;0");

class SERIAL {
public:
	void print(std::string str) {
		std::cout << str;
	}
	void print(int i) {
		std::cout << i;
	}
	void println(std::string str) {
		std::cout << str << std::endl;
	}
	void println(int i) {
		std::cout << i << std::endl;;
	}
};

class Target {
public:
	bool activeFlag = false;

	int mode = -1;
    int nextTarget = -3;
    int upInterval = 0;
    int downInterval = 0;
    int upIntervalRange = 1;
    int downIntervalRange = 1;

	int setSwitchTime() {
		int timer = 24387885;

		if (upInterval > 0 && activeFlag) {
			return timer + (rand() % upIntervalRange) + upInterval;
		} else if (downInterval > 0 && !activeFlag) {
			return timer + (rand() % downIntervalRange) + downInterval;
		}

		return 0;
	}
};

int main() {
	SERIAL Serial;
	std::vector<Target> targets = {Target(), Target(), Target(), Target()};
	int offset = 0;
	srand(time(NULL));



    std::smatch matches;
    int index = 0;
    bool ready = false;

    while (std::regex_search (buff, matches, std::regex("OR;([0-9]+);(-*[0-9]+)"))) {
        int index = atoi(matches.str(1).c_str()) - offset; // Target
        int i = atoi(matches.str(2).c_str()); // Next target
        targets[index].nextTarget = i;
        Serial.print("target: ");
        Serial.print(index + offset);
        Serial.print(" (");
        Serial.print(index);
        Serial.print(")   nextTarget: ");
        Serial.println(targets[index].nextTarget);
        ready = true;
        buff = buff.erase(matches.position(0), matches.length(0)); // Iterates to next match.
    }

    while (std::regex_search (buff, matches, std::regex("TI;([0-9]+);([0-9]+);([0-9]+);*([0-9]+);*([0-9]+)"))) {
        int index = atoi(matches.str(1).c_str()) - offset; // Target
        int i = atoi(matches.str(2).c_str()); // Up interval
        int j = atoi(matches.str(3).c_str()); // Down interval
		int k = atoi(matches.str(4).c_str()) + 1; // Up interval range
		int l = atoi(matches.str(5).c_str()) + 1; // Down interval range
        targets[index].upInterval = i;
        targets[index].downInterval = j;
		targets[index].upIntervalRange = k;
        targets[index].downIntervalRange = l;
        Serial.print("target: ");
        Serial.print(index + offset);
        Serial.print(" (");
        Serial.print(index);
        Serial.print(")   upInterval: ");
        Serial.print(targets[index].upInterval);
        Serial.print("   downInterval: ");
        Serial.print(targets[index].downInterval);
        Serial.print("   upIntervalRange: ");
        Serial.print(targets[index].upIntervalRange);
        Serial.print("   downIntervalRange: ");
        Serial.println(targets[index].downIntervalRange);
        ready = true;
        buff = buff.erase(matches.position(0), matches.length(0)); // Iterates to next match.
    }

	for (int index = 0; index < targets.size(); index++) {
		Serial.println(index);
		for (int i = 0; i < 10; i++) {
			int switchTime = targets[index].setSwitchTime();

			Serial.print("switchInterval: ");
			Serial.println(switchTime);
		}
	}

    return 0;
}
