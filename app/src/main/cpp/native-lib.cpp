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
static int farmers = 0;

static int farmerCost = 10;
static bool notEnough = false;

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
    if(notEnough)
        stringStream << "\nYou don't have enough pantsu for that!";

    notEnough= false;
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

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_buyFarmer(JNIEnv *env, jobject instance) {
    if(pantsu[1][0] + pantsu[1][1] + pantsu[1][2] + pantsu[1][3] < farmerCost)
        notEnough = true;
    else {
        notEnough = false;
        int type = rand() % 4;
        int remainingCost = farmerCost;
        while (remainingCost > 0) {
            if (pantsu[1][type] >= remainingCost) {
                pantsu[1][type] -= remainingCost;
                remainingCost = 0;
            } else {
                remainingCost -= pantsu[1][type];
                pantsu[1][type] = 0;
                type = (type + 1) % 4;
            }
        }
        farmers++;
        farmerCost =  (int)pow((double)farmerCost,1.2); // Maybe another formula?
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_getFarmers(JNIEnv *env, jobject instance) {
    return farmers;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_bar_foo_myapplication_MainActivity_farmPantsu(JNIEnv *env, jobject instance) {
    int stars, type;
    std::tie(stars, type) = randPantsu(true);
    pantsu[stars][type] += farmers;
}