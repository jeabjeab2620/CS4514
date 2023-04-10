@echo off
"C:\\Users\\Hyeon Kim\\AppData\\Local\\Android\\Sdk\\cmake\\3.18.1\\bin\\cmake.exe" ^
  "-HC:\\Users\\Hyeon Kim\\Desktop\\Study\\FYP\\project_code\\VLC P2P Payment\\MyApplication\\OpenCV\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=21" ^
  "-DANDROID_PLATFORM=android-21" ^
  "-DANDROID_ABI=x86" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86" ^
  "-DANDROID_NDK=C:\\Users\\Hyeon Kim\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\Hyeon Kim\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\Hyeon Kim\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\Hyeon Kim\\AppData\\Local\\Android\\Sdk\\cmake\\3.18.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=C:\\Users\\Hyeon Kim\\Desktop\\Study\\FYP\\project_code\\VLC P2P Payment\\MyApplication\\OpenCV\\build\\intermediates\\cxx\\Debug\\6f37xp1l\\obj\\x86" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=C:\\Users\\Hyeon Kim\\Desktop\\Study\\FYP\\project_code\\VLC P2P Payment\\MyApplication\\OpenCV\\build\\intermediates\\cxx\\Debug\\6f37xp1l\\obj\\x86" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BC:\\Users\\Hyeon Kim\\Desktop\\Study\\FYP\\project_code\\VLC P2P Payment\\MyApplication\\OpenCV\\.cxx\\Debug\\6f37xp1l\\x86" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
