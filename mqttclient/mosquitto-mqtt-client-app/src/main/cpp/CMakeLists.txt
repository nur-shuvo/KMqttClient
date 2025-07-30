cmake_minimum_required(VERSION 3.10...3.31)
project(mosquitto_client_android)

add_definitions(-DINTERNAL_CLIENT_PRIVATE_KEY_PASSWORD="${INTERNAL_CLIENT_PRIVATE_KEY_PASSWORD}")

# General Configuration
set(LIBRARY_EXTENSION a)
set(LIBRARY_FORMAT STATIC)

set(DISTRIBUTION_POSTFIX "${CMAKE_BUILD_TYPE}/${ANDROID_ABI}")
set(OPENSSL_DISTRIBUTION_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/openssl/${DISTRIBUTION_POSTFIX}")
set(MOSQUITTO_DISTRIBUTION_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution/mosquitto/${DISTRIBUTION_POSTFIX}")

add_library(ssl ${LIBRARY_FORMAT} IMPORTED)
set_target_properties(ssl PROPERTIES IMPORTED_LOCATION
        ${OPENSSL_DISTRIBUTION_DIR}/libssl.${LIBRARY_EXTENSION})

add_library(crypto ${LIBRARY_FORMAT} IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION
        ${OPENSSL_DISTRIBUTION_DIR}/libcrypto.${LIBRARY_EXTENSION})

add_library(mosquitto ${LIBRARY_FORMAT} IMPORTED)
set_target_properties(mosquitto PROPERTIES IMPORTED_LOCATION
        ${MOSQUITTO_DISTRIBUTION_DIR}/libmosquitto_static.${LIBRARY_EXTENSION})

add_library(mosquitto_client_android SHARED
        mosquitto-client-jni.cpp
        mosquitto-client-jni.h)

target_include_directories(mosquitto_client_android PRIVATE
        ${OPENSSL_DISTRIBUTION_DIR}/include
        ${MOSQUITTO_DISTRIBUTION_DIR}/include)

target_link_libraries(mosquitto_client_android
        android
        log
        ssl
        crypto
        mosquitto)
