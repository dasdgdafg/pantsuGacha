#include "native-lib.h"

#include <jni.h>
#include <string>
#include <sstream>
#include <tgmath.h>

static int pantsu[6][4] = {{0,0,0,0}, // don't use this row, it's just to do pantsu[stars] instead of pantsu[stars-1] everywhere
                           {0,0,0,0}, // *
                           {0,0,0,0}, // **
                           {0,0,0,0}, // ***
                           {0,0,0,0}, // ****
                           {0,0,0,0}};// *****
static int levels[6][4] = {{0,0,0,0},
                           {0,0,0,0},
                           {0,0,0,0},
                           {0,0,0,0},
                           {0,0,0,0},
                           {0,0,0,0}};

static int farmers = 0;
static const int BASE_FARMER_COST = 50;
static const double FARMER_COST_INCREASE = 1.15;

static int pantyPoints = 200;

static const double ROLL_ODDS[4][4] = {{1.00, 0.99, 0.89, 0.65}, // free * - ****   65% 24% 10% 01% 00%
                                       {1.00, 0.95, 0.85, 0.60}, // low * - ****    60% 25% 10% 05% 00%
                                       {0.95, 0.75, 0.55, 0.30}, // med * - *****   30% 25% 20% 20% 05%
                                       {0.80, 0.55, 0.30, 0.00}};// high ** - ***** 00% 30% 25% 25% 20%
static const int ROLL_PRICES[] = {0,    // free - pantsu sells for  7.35 average
                                  25,   // low  - pantsu sells for  8.00 average
                                  1000, // med  - pantsu sells for 12.25 average - 5% chance of *****
                                  4000};// high - pantsu sells for 16.75 average - 20% chance of *****
static const double farmChances[] = {1.00, 0.95, 0.85, 0.60};

extern "C" JNIEXPORT jintArray JNICALL Java_com_example_bar_foo_myapplication_MainActivity_fetchPantsu(JNIEnv *env, jobject /* this */, Rolls rollType) {
    jintArray result;
    result = env->NewIntArray(3);
    if (pantyPoints < ROLL_PRICES[rollType]) {
        env->SetIntArrayRegion(result, 0, 3, (jint[]){0, 0, 0});
    } else {
        pantyPoints -= ROLL_PRICES[rollType];
        int stars, type;
        std::tie(stars, type) = randPantsu(rollType);
        pantsu[stars][type]++;
        env->SetIntArrayRegion(result, 0, 3, (jint[]){1, stars, type});
    }
    return result;
}

std::tuple<int, int> randPantsu(Rolls rollType) {
    double rare = (double) rand() / RAND_MAX;
    int type = rand() % 4;
    int stars;
    const double *chance = ROLL_ODDS[rollType];
    if (rare > chance[0]) {
        stars = 5;
    } else  if (rare > chance[1]) {
        stars = 4;
    } else  if (rare > chance[2]) {
        stars = 3;
    } else  if (rare > chance[3]) {
        stars = 2;
    } else {
        stars = 1;
    }
    return {stars, type};
}

extern "C" JNIEXPORT jintArray JNICALL Java_com_example_bar_foo_myapplication_MainActivity_pantsuStatus(JNIEnv *env, jobject /* this */)
{
    jintArray result;
    result = env->NewIntArray(20);
    if (result == NULL) {
        return NULL;
    }

    // fill a temp structure to use to populate the java int array
    jint fill[20];
    for (int i = 0; i < 20; i++) {
        fill[i] = pantsu[i/4+1][i%4];
    }
    // move from the temp structure to the java structure
    env->SetIntArrayRegion(result, 0, 20, fill);
    return result;
}

extern "C" JNIEXPORT jintArray JNICALL Java_com_example_bar_foo_myapplication_MainActivity_getLevels(JNIEnv *env, jobject /* this */)
{
    jintArray result;
    result = env->NewIntArray(20);
    if (result == NULL) {
        return NULL;
    }

    // fill a temp structure to use to populate the java int array
    jint fill[20];
    for (int i = 0; i < 20; i++) {
        fill[i] = levels[i/4+1][i%4];
    }
    // move from the temp structure to the java structure
    env->SetIntArrayRegion(result, 0, 20, fill);
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_levelAll(JNIEnv *env, jobject /* this */) {
    for (int stars = 1; stars <= 5; stars++) {
        for (int type = 0; type < 4; type++) {
            int available = std::min(std::max(pantsu[stars][type] - 1, 0), 9 - levels[stars][type]);
            pantsu[stars][type] -= available;
            levels[stars][type] += available;
        }
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_sellExtras(JNIEnv *env, jobject /* this */) {
    for (int stars = 1; stars <= 5; stars++) {
        for (int type = 0; type < 4; type++) {
            int useful = std::min(std::max(pantsu[stars][type] - 1, 0), 9 - levels[stars][type]);
            useful++; // keep one of each
            if (pantsu[stars][type] > useful) {
                int toSell = pantsu[stars][type] - useful;
                pantsu[stars][type] -= toSell;
                pantyPoints += 5 * stars * toSell;
            }
        }
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_buyFarmer(JNIEnv *env, jobject instance) {
    if(pantyPoints < farmerCost())
        return 0;
    else {
        pantyPoints -= farmerCost();
        farmers++;
        return 1;
    }
}

int farmerCost() {
    // cost = 50^(1.15^farmers)
    return (int)pow(50, pow(1.15, farmers));
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_getFarmers(JNIEnv *env, jobject instance) {
    return farmers;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_bar_foo_myapplication_MainActivity_getFarmerCost(JNIEnv *env, jobject instance) {
    return farmerCost();
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_bar_foo_myapplication_MainActivity_getPoints(JNIEnv *env, jobject instance) {
    return pantyPoints;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_bar_foo_myapplication_MainActivity_getRollPrice(JNIEnv *env, jobject instance, Rolls rollType) {
    return ROLL_PRICES[rollType];
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_farmPantsu(JNIEnv *env, jobject instance) {
    int stars, type;
    std::tie(stars, type) = randPantsu(LOW);
    pantsu[stars][type] += farmers;
}

extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_setLoadedData(JNIEnv *env, jobject instance,
                                                                                                    jintArray newPantsu_,
                                                                                                    jintArray newLevels_,
                                                                                                    jint newPoints,
                                                                                                    jint newFarmers) {
    jint *newPantsu = env->GetIntArrayElements(newPantsu_, NULL);
    jint *newLevels = env->GetIntArrayElements(newLevels_, NULL);

    for (int i = 0; i < 10; i++) {
        pantsu[i/4+1][i%4] = newPantsu[i];
        levels[i/4+1][i%4] = newLevels[i];
    }
    pantyPoints = newPoints;
    farmers = newFarmers;

    env->ReleaseIntArrayElements(newPantsu_, newPantsu, 0);
    env->ReleaseIntArrayElements(newLevels_, newLevels, 0);
}