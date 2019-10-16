# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/libs/${ANDROID_ABI})
#important 需要注意目录
#设置头文件目录
include_directories(src/main/cpp/include)

# Creates and names a library, sets it as either STATICUserArguments
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.cpp

        src/main/cpp/PushH264Stream.cpp
        src/main/cpp/RtpSender.cpp
        src/main/cpp/misc.c
        src/main/cpp/pes.c
        src/main/cpp/ps_demux.c
        src/main/cpp/ps_stream.c
        src/main/cpp/ts_demux.c
        src/main/cpp/ts_stream.c
        src/main/cpp/g711.c

        src/main/cpp/MediaDecoder.cpp
        src/main/cpp/PullMediaStream.cpp
        src/main/cpp/RtpRecvData.cpp
        src/main/cpp/RtpRecvDataBase.cpp
        src/main/cpp/RtpRecvPSData.cpp
        src/main/cpp/RtpRecvDataFactory.cpp
        src/main/cpp/ring_buffer_s.cpp
        src/main/cpp/spin_mutex.cpp
        src/main/cpp/PSDemux.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
add_library(
        jrtp
        SHARED
        IMPORTED
)


set_target_properties(
        jrtp
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libjrtp.so
)

add_library(
        jthread
        SHARED
        IMPORTED
)


set_target_properties(
        jthread
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libjthread.so
)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        jrtp
        jthread
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})