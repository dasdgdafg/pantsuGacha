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


extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_fetchPantsu(JNIEnv *env, jobject /* this */) {
    double rare = (double) rand() / RAND_MAX;
    int type = rand() % 4;
    int stars;
    if (rare > 0.95) {
        stars = 5;
    } else  if (rare > 0.85) {
        stars = 4;
    } else  if (rare > 0.65) {
        stars = 3;
    } else  if (rare > 0.40) {
        stars = 2;
    } else {
        stars = 1;
    }
    pantsu[stars][type]++;
}

extern "C" JNIEXPORT jstring JNICALL Java_com_example_bar_foo_myapplication_MainActivity_status(JNIEnv *env, jobject /* this */) {
    std::ostringstream stringStream;
    for (unsigned int stars = 1; stars <= 5; stars++) {
        stringStream << std::string(stars, '*') << " pantsu: ";
        for (int type = 0; type < 4; type++) {
            stringStream << pantsu[stars][type] << ",";
        }
        stringStream << "\n";
    }

    stringStream << "\n";
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
    int type = rand() % 4;
    pantsu[1][type] += farmers;
}