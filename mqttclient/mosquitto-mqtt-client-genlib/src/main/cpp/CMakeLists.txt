cmake_minimum_required(VERSION 3.10...3.31)
project(mosquittoAndroid)
include(ExternalProject)

find_program(GIT_EXECUTABLE git)
if (NOT GIT_EXECUTABLE)
    message(FATAL_ERROR "Git not found. Please ensure it is installed and available in the PATH.")
endif ()

find_program(PERL_EXECUTABLE perl)
if (NOT PERL_EXECUTABLE)
    message(FATAL_ERROR "Perl not found. Please ensure it is installed and available in the PATH.")
endif ()

# Check if ANDROID_NDK is set
if (NOT DEFINED ANDROID_NDK OR ANDROID_NDK STREQUAL "")
    message(FATAL_ERROR "ANDROID_NDK is not set. Ensure your Android NDK is properly configured.")
else ()
    message(STATUS "ANDROID_NDK is set to ${ANDROID_NDK}")
endif ()

message(STATUS "CMake version: ${CMAKE_VERSION}")

# General Configuration
set(ENABLE_STATIC ON)
set(ENABLE_TLS ON)
set(ENABLE_SHARED OFF)
set(LIBRARY_FORMAT STATIC)
set(LIBRARY_EXTENSION a)

# OpenSSL Configuration
set(OPENSSL_VERSION "OpenSSL_1_1_1w")
set(OPENSSL_REPOSITORY "https://github.com/openssl/openssl.git")
set(OPENSSL_FEATURES no-shared enable-ecdsa enable-ecdh no-weak-ssl-ciphers no-ssl2 no-ssl3 no-comp no-hw no-engine no-psk no-srp no-zlib no-deprecated no-tests)


# Mosquitto Configuration
set(MOSQUITTO_VERSION "2.0.22")
set(MOSQUITTO_REPOSITORY "https://github.com/eclipse-mosquitto/mosquitto.git")

ExternalProject_Add(openssl_project
        GIT_REPOSITORY ${OPENSSL_REPOSITORY}
        GIT_TAG ${OPENSSL_VERSION}
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CONFIGURE_COMMAND ${CMAKE_COMMAND} -E env PATH=${ANDROID_TOOLCHAIN_ROOT}/bin:$ENV{PATH} CC=${CMAKE_C_COMPILER} ANDROID_NDK_HOME=${ANDROID_NDK} perl <SOURCE_DIR>/Configure android-${ANDROID_ARCH_NAME} --openssldir=${CMAKE_LIBRARY_OUTPUT_DIRECTORY} --libdir=${CMAKE_LIBRARY_OUTPUT_DIRECTORY} --prefix=${CMAKE_LIBRARY_OUTPUT_DIRECTORY} ${OPENSSL_FEATURES} -D__ANDROID_API__=${ANDROID_PLATFORM_LEVEL}
        BUILD_COMMAND ${CMAKE_COMMAND} -E env PATH=${ANDROID_TOOLCHAIN_ROOT}/bin:$ENV{PATH} ANDROID_NDK_HOME=${ANDROID_NDK} make
        BUILD_BYPRODUCTS ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.${LIBRARY_EXTENSION} ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.${LIBRARY_EXTENSION}
        BUILD_IN_SOURCE 1
        BUILD_ALWAYS 1
        LOG_DOWNLOAD 1
        LOG_CONFIGURE 1
        LOG_BUILD 1)

add_library(ssl ${LIBRARY_FORMAT} IMPORTED)
add_dependencies(ssl openssl_project)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.${LIBRARY_EXTENSION})

add_library(crypto ${LIBRARY_FORMAT} IMPORTED)
add_dependencies(crypto openssl_project)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION
        ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.${LIBRARY_EXTENSION})

ExternalProject_Add(mosquitto_project
        GIT_REPOSITORY ${MOSQUITTO_REPOSITORY}
        GIT_TAG ${MOSQUITTO_VERSION}
        GIT_SHALLOW 1
        GIT_PROGRESS 1
        CMAKE_ARGS
        -DOPENSSL_INCLUDE_DIR=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include
        -DOPENSSL_CRYPTO_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.${LIBRARY_EXTENSION}
        -DOPENSSL_SSL_LIBRARY=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.${LIBRARY_EXTENSION}
        -DWITH_BUNDLED_DEPS=ON
        -DWITH_TLS=${ENABLE_TLS}
        -DWITH_TLS_PSK=OFF
        -DWITH_EC=${ENABLE_TLS}
        -DWITH_UNIX_SOCKETS=ON
        -DWITH_SOCKS=OFF
        -DWITH_SRV=OFF
        -DWITH_STATIC_LIBRARIES=${ENABLE_STATIC}
        -DWITH_SHARED_LIBRARIES=${ENABLE_SHARED}
        -DWITH_PIC=ON
        -DWITH_THREADING=ON
        -DWITH_DLT=OFF
        -DWITH_CJSON=OFF
        -DWITH_CLIENTS=OFF
        -DWITH_BROKER=OFF
        -DINC_BRIDGE_SUPPORT=OFF
        -DWITH_APPS=OFF
        -DWITH_LIB_CPP=OFF
        -DWITH_PLUGINS=OFF
        -DDOCUMENTATION=OFF
        -DWITH_DOCS=OFF
        -DUSE_LIBWRAP=OFF
        -DINC_DB_UPGRADE=OFF
        -DINC_MEMTRACK=OFF
        -DWITH_PERSISTENCE=OFF
        -DWITH_SYS_TREE=OFF
        -DWITH_ADNS=OFF
        -DWITH_SYSTEMD=OFF
        -DWITH_WEBSOCKETS=OFF
        -DWITH_CONTROL=OFF
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DCMAKE_PREFIX_PATH=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_PREFIX=${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
        -DCMAKE_INSTALL_LIBDIR=.
        -DCMAKE_INSTALL_BINDIR=bin
        -DCMAKE_INSTALL_INCLUDEDIR=include/mosquitto
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DANDROID_TOOLCHAIN=${ANDROID_TOOLCHAIN}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DANDROID_STL=${ANDROID_STL}
        -DANDROID_PIE=${ANDROID_PIE}
        -DANDROID_CPP_FEATURES=${ANDROID_CPP_FEATURES}
        -DANDROID_ALLOW_UNDEFINED_SYMBOLS=${ANDROID_ALLOW_UNDEFINED_SYMBOLS}
        -DANDROID_ARM_MODE=${ANDROID_ARM_MODE}
        -DANDROID_DISABLE_FORMAT_STRING_CHECKS=${ANDROID_DISABLE_FORMAT_STRING_CHECKS}
        -DANDROID_CCACHE=${ANDROID_CCACHE}
        -DANDROID_SANITIZE=${ANDROID_SANITIZE}
        DEPENDS crypto ssl
        BUILD_IN_SOURCE 1
        BUILD_ALWAYS 1
        LOG_DOWNLOAD 1
        LOG_CONFIGURE 1
        LOG_BUILD 1)

set(DISTRIBUTION_POSTFIX "${CMAKE_BUILD_TYPE}/${ANDROID_ABI}")
set(OPENSSL_DISTRIBUTION_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/openssl/${DISTRIBUTION_POSTFIX}")
set(MOSQUITTO_DISTRIBUTION_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/mosquitto/${DISTRIBUTION_POSTFIX}")

add_custom_command(
        TARGET mosquitto_project POST_BUILD

        COMMAND "${CMAKE_COMMAND}" -E
        copy_directory "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include/openssl"
        ${OPENSSL_DISTRIBUTION_DIR}/include

        COMMAND "${CMAKE_COMMAND}" -E
        copy "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libssl.${LIBRARY_EXTENSION}"
        "${OPENSSL_DISTRIBUTION_DIR}/libssl.${LIBRARY_EXTENSION}"

        COMMAND "${CMAKE_COMMAND}" -E
        copy "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcrypto.${LIBRARY_EXTENSION}"
        "${OPENSSL_DISTRIBUTION_DIR}/libcrypto.${LIBRARY_EXTENSION}"

        COMMAND "${CMAKE_COMMAND}" -E
        copy_directory "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/include/mosquitto"
        ${MOSQUITTO_DISTRIBUTION_DIR}/include

        COMMAND "${CMAKE_COMMAND}" -E
        copy "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libmosquitto_static.${LIBRARY_EXTENSION}"
        "${MOSQUITTO_DISTRIBUTION_DIR}/libmosquitto_static.${LIBRARY_EXTENSION}"

        COMMENT "Copying OpenSSL and Mosquitto files to the distribution directory")
