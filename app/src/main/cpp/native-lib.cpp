#include <jni.h>
#include <string>
#include <sstream>

static int f = 0;
static int ssr = 0;

extern "C" JNIEXPORT void JNICALL Java_com_example_bar_foo_myapplication_MainActivity_fetchPantsu( JNIEnv *env, jobject /* this */) {
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
    return env->NewStringUTF(stringStream.str().c_str());
}

