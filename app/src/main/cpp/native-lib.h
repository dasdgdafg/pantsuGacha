#ifndef MYAPPLICATION_NATIVE_LIB_H
#define MYAPPLICATION_NATIVE_LIB_H

#include <tuple>

enum Rolls {
    FREE = 0,
    LOW = 1, // also used for farming
    MED = 2,
    HIGH = 3,
};

std::tuple<int, int> randPantsu(Rolls rollType);

#endif //MYAPPLICATION_NATIVE_LIB_H
