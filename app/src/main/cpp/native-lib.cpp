#include <jni.h>
#include <string>
#include <sstream>
#include <tgmath.h>

static int f = 0;
static int ssr = 0;
static int farmers = 0;

static int farmerCost = 10;
static bool notEnough = false;


extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_fetchPantsu(JNIEnv *env, jobject /* this */) {
    double num = (double) rand() / RAND_MAX;
    if (num > 0.9) {
        ssr++;
    } else {
        f++;
    }
}

extern "C" JNIEXPORT jstring JNICALL Java_com_example_bar_foo_myapplication_MainActivity_status(JNIEnv *env, jobject /* this */) {
    std::ostringstream stringStream;
    stringStream << "shitty pantsu: ";
    stringStream << f;
    stringStream << "\n";
    stringStream << "good pantsu: ";
    stringStream << ssr;
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

    if(f<farmerCost)
        notEnough = true;
    else {
        notEnough = false;
        f-=farmerCost;
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
    f+= farmers;
}