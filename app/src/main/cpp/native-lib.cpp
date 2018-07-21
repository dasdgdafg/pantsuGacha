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
static int farmerCost = 50;

static int pantyPoints = 200;

static const double chances[] = {0.95, 0.85, 0.65, 0.40};
static const double farmChances[] = {1.00, 0.95, 0.85, 0.60};

extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_fetchPantsu(JNIEnv *env, jobject /* this */) {
    int stars, type;
    std::tie(stars, type) = randPantsu(false);
    pantsu[stars][type]++;
}

std::tuple<int, int> randPantsu(bool farming) {
    double rare = (double) rand() / RAND_MAX;
    int type = rand() % 4;
    int stars;
    const double *chance = farming ? farmChances : chances;
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

extern "C" JNIEXPORT jstring JNICALL Java_com_example_bar_foo_myapplication_MainActivity_farmStatus(JNIEnv *env, jobject /* this */) {
    std::ostringstream stringStream;

    stringStream << "pantsu farmers: ";
    stringStream << farmers;
    stringStream << "\n";
    stringStream << "cost to buy a new one: "; // Probably move this near the button at some point
    stringStream << farmerCost;

    return env->NewStringUTF(stringStream.str().c_str());
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
    if(pantyPoints < farmerCost)
        return 0;
    else {
        pantyPoints -= farmerCost;
        farmers++;
        farmerCost =  (int)pow((double)farmerCost,1.2); // Maybe another formula?
        return 1;
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_getFarmers(JNIEnv *env, jobject instance) {
    return farmers;
}

extern "C" JNIEXPORT jint JNICALL Java_com_example_bar_foo_myapplication_MainActivity_getPoints(JNIEnv *env, jobject instance) {
    return pantyPoints;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_farmPantsu(JNIEnv *env, jobject instance) {
    int stars, type;
    std::tie(stars, type) = randPantsu(true);
    pantsu[stars][type] += farmers;
}