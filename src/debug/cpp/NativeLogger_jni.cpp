#include <string_view>

#include <jni.h>

#include "utils/jni.hpp"
#include "utils/logging.hpp"


using utf16_view = std::u16string_view;


extern "C" JNIEXPORT void JNICALL
Java_util_log_NativeLogger_nativeInit(
    JNIEnv* j_env,
    jclass,
    jstring j_process_tag,
    jint j_log_file_descriptor
) {
    AndroidWriter::init(
        JniStringGuard(j_env, j_process_tag).u8str(),
        j_log_file_descriptor
    );
}

extern "C" JNIEXPORT jlong JNICALL
Java_util_log_NativeLogger_nativeCtor(
    JNIEnv* j_env,
    jobject,
    jstring j_braced_class_tag
) {
    return reinterpret_cast<jlong>(new AndroidWriter(
            JniStringGuard(j_env, j_braced_class_tag).u8str()));
}

extern "C" JNIEXPORT void JNICALL
Java_util_log_NativeLogger_nativeD(
    JNIEnv* j_env,
    jobject,
    jlong j_native_logger_ptr,
    jstring j_msg,
    jint j_msg_length
) {
    my::log::Logger<my::log::Level::DEBUG, AndroidWriter>(
        *reinterpret_cast<const AndroidWriter*>(j_native_logger_ptr)
    ) << JniStringGuard(j_env, j_msg, j_msg_length).u8str();
}
