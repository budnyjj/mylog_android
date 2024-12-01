cmake_minimum_required(VERSION 3.4.1)

add_library(
    mylog_android
    SHARED
    NativeLogger_jni.cpp
)
target_include_directories(
    mylog_android
    PRIVATE
    utils/mylog/include
)
find_library(LOG log)
target_link_libraries(mylog_android ${LOG})
