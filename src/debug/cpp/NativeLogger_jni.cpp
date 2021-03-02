#include <string_view>

#include <jni.h>

#include "NativeLogger.hpp"
#include "utils/jni.hpp"


using utf16_view = std::u16string_view;


extern "C" JNIEXPORT void JNICALL
Java_util_log_NativeLogger_nativeInit(
    JNIEnv* j_env,
    jclass,
    jstring j_process_tag,
    jint j_log_file_descriptor
) {
    NativeLogger::init(
        JniStringGuard(j_env, j_process_tag).view(),
        j_log_file_descriptor);
}

extern "C" JNIEXPORT jlong JNICALL
Java_util_log_NativeLogger_nativeCtor(
    JNIEnv* j_env,
    jobject,
    jstring j_braced_class_tag
) {
    return reinterpret_cast<jlong>(new NativeLogger(
            JniStringGuard(j_env, j_braced_class_tag).view()));
}

extern "C" JNIEXPORT void JNICALL
Java_util_log_NativeLogger_nativeD(
    JNIEnv* j_env,
    jobject,
    jlong j_native_logger_ptr,
    jstring j_msg
) {
    reinterpret_cast<const NativeLogger*>(j_native_logger_ptr)->d(
        JniStringGuard(j_env, j_msg).view());
}
