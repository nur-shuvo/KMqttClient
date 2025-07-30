#include "mosquitto-client-jni.h"

static int password_callback(char *buf, int size, int rwflag, void *userdata) {
    char *password = decode(INTERNAL_CLIENT_PRIVATE_KEY_PASSWORD);
    strncpy(buf, password, size);
    buf[size - 1] = '\0';
    free(password);
    return (int) strlen(buf);
}

void on_log_callback(struct mosquitto *mosq, void *userdata, int level, const char *message) {
    if (level == MOSQ_LOG_ERR) {
        LOGE("level: [Error]: %s\n", message);
    }
}

void onConnectCallback(struct mosquitto *mosq, void *context, int reasonCode) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }
    const char *reasonDescriptor = mosquitto_reason_string(reasonCode);
    jstring jReasonDescriptor = env->NewStringUTF(reasonDescriptor);

    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onConnectCallback, reasonCode, jReasonDescriptor);

    env->DeleteLocalRef(jReasonDescriptor);
    checkAndClearException(env, "onConnectCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

void onDisconnectCallback(struct mosquitto *mosq, void *context, int reasonCode) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }
    const char *reasonDescriptor = mosquitto_reason_string(reasonCode);
    jstring jReasonDescriptor = env->NewStringUTF(reasonDescriptor);

    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onDisconnectCallback, reasonCode,
                        jReasonDescriptor);
    env->DeleteLocalRef(jReasonDescriptor);
    checkAndClearException(env, "onDisconnectCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

void onSubscribeCallback(struct mosquitto *mosq, void *context, int mid, int qosCount,
                         const int *grantedQos) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }

    bool isSuccess = false;
    for (int i = 0; i < qosCount; i++) {
        if (grantedQos[i] <= 2) {
            isSuccess = true;
            break;
        }
    }
    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onSubscribeCallback, (jint) mid,
                        (jboolean) isSuccess);
    checkAndClearException(env, "onSubscribeCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

void onMessageCallback(struct mosquitto *mosq, void *context,
                       const struct mosquitto_message *message) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }
    jstring topic = env->NewStringUTF(message->topic);
    jstring payload = env->NewStringUTF((char *) message->payload);

    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onMessageCallback, (jint) message->mid,
                        topic, (jint) message->payloadlen, payload,
                        (jint) message->qos, (jboolean) message->retain);

    env->DeleteLocalRef(topic);
    env->DeleteLocalRef(payload);

    checkAndClearException(env, "onMessageCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

void onUnsubscribeCallback(struct mosquitto *mosq, void *context, int mid) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }
    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onUnsubscribeCallback, mid);

    checkAndClearException(env, "onUnsubscribeCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

void onPublishCallback(struct mosquitto *mosq, void *context, int mid) {
    JNIEnv *env;
    if (!attachThreadToJvm(globalMosquittoContext->javaVM, &env)) {
        return;
    }
    env->CallVoidMethod(globalMosquittoContext->clientInstanceMap[(char *) context],
                        globalMosquittoContext->onPublishCallback, mid);

    checkAndClearException(env, "onPublishCallback");
    globalMosquittoContext->javaVM->DetachCurrentThread();
}

extern "C" {

JNIEXPORT jobject JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_connect(
        JNIEnv *env, jobject thiz, jobject clientConfig) {
    jclass configClass = (*env).GetObjectClass(clientConfig);

    jfieldID idField = (*env).GetFieldID(configClass, "identifier", "Ljava/lang/String;");
    jfieldID hostField = (*env).GetFieldID(configClass, "serverHost", "Ljava/lang/String;");
    jfieldID portField = (*env).GetFieldID(configClass, "serverPort", "I");
    jfieldID sessionField = (*env).GetFieldID(configClass, "cleanSession", "Z");
    jfieldID keepAliveField = (*env).GetFieldID(configClass, "keepAlive", "I");
    jfieldID reconnectField = (*env).GetFieldID(configClass, "reconnectDelay", "I");
    jfieldID sendMaxField = (*env).GetFieldID(configClass, "sendMaximum", "I");
    jfieldID receiveMaxField = (*env).GetFieldID(configClass, "receiveMaximum", "I");
    jfieldID authField = (*env).GetFieldID(configClass, "authentication", "Lcom/nurshuvo/kmqtt/internal/message/connect/Authentication;");

    jstring identifier = (jstring) (*env).GetObjectField(clientConfig, idField);
    jstring serverHost = (jstring) (*env).GetObjectField(clientConfig, hostField);
    jint serverPort = (*env).GetIntField(clientConfig, portField);
    jboolean cleanSession = (*env).GetBooleanField(clientConfig, sessionField);
    jint keepAlive = (*env).GetIntField(clientConfig, keepAliveField);
    jint reconnectDelay = (*env).GetIntField(clientConfig, reconnectField);
    jint sendMaximum = (*env).GetIntField(clientConfig, sendMaxField);
    jint receiveMaximum = (*env).GetIntField(clientConfig, receiveMaxField);
    jobject auth = env->GetObjectField(clientConfig, authField);

    const char *clientIDStr = env->GetStringUTFChars(identifier, 0);
    const char *hostStr = env->GetStringUTFChars(serverHost, 0);

    if (globalMosquittoContext->clientInstanceMap[clientIDStr] == nullptr) {
        createClient(env, thiz, identifier, cleanSession);
    }

    mosquitto_int_option(
            globalMosquittoContext->mosquittoClientsMap[clientIDStr],
            MOSQ_OPT_PROTOCOL_VERSION,
            MQTT_PROTOCOL_V5
    );
    mosquitto_reconnect_delay_set(
            globalMosquittoContext->mosquittoClientsMap[clientIDStr],
            reconnectDelay,
            reconnectDelay,
            false
    );
    mosquitto_int_option(globalMosquittoContext->mosquittoClientsMap[clientIDStr],
                         MOSQ_OPT_RECEIVE_MAXIMUM,
                         receiveMaximum
    );
    mosquitto_int_option(globalMosquittoContext->mosquittoClientsMap[clientIDStr],
                         MOSQ_OPT_SEND_MAXIMUM,
                         sendMaximum
    );

    jclass tlsAuthClass = env->FindClass("com/nurshuvo/kmqtt/internal/message/connect/Authentication$TlsAuthentication");
    jclass noAuthClass = env->FindClass("com/nurshuvo/kmqtt/internal/message/connect/Authentication$NoAuthentication");

    if (env->IsInstanceOf(auth, tlsAuthClass)) {
        jfieldID caPathField = env->GetFieldID(tlsAuthClass, "certificateAuthorityPath", "Ljava/lang/String;");
        jfieldID clientCertificatePathField = env->GetFieldID(tlsAuthClass, "clientCertificatePath", "Ljava/lang/String;");
        jfieldID privateKeyField = env->GetFieldID(tlsAuthClass, "privateKeyPath", "Ljava/lang/String;");

        jstring caPath = (jstring) env->GetObjectField(auth, caPathField);
        jstring serverCert = (jstring) env->GetObjectField(auth, clientCertificatePathField);
        jstring privateKey = (jstring) env->GetObjectField(auth, privateKeyField);

        const char *caPathStr = env->GetStringUTFChars(caPath, 0);
        const char *clientCertStr = env->GetStringUTFChars(serverCert, 0);
        const char *privateKeyStr = env->GetStringUTFChars(privateKey, 0);

        mosquitto_tls_set(
                globalMosquittoContext->mosquittoClientsMap[clientIDStr],
                caPathStr,
                nullptr,
                clientCertStr,
                privateKeyStr,
                password_callback
        );

        env->ReleaseStringUTFChars(caPath, caPathStr);
        env->ReleaseStringUTFChars(serverCert, clientCertStr);
        env->ReleaseStringUTFChars(privateKey, privateKeyStr);
    }
    else if (env->IsInstanceOf(auth, noAuthClass)) {}
    else {
        LOGE("Unknown authentication");
    }

    int resultCode = mosquitto_connect_async(
            globalMosquittoContext->mosquittoClientsMap[clientIDStr],
            hostStr,
            serverPort,
            keepAlive
    );
    const char *resultDescriptor = "Success";
    if (resultCode != MOSQ_ERR_SUCCESS) {
        resultDescriptor = mosquitto_strerror(resultCode);
    }

    jclass resultPairClass = env->FindClass(
            "com/nurshuvo/kmqtt/internal/message/connack/MqttConnAck");
    jmethodID constructor = env->GetMethodID(resultPairClass, "<init>", "(ILjava/lang/String;)V");

    jstring jResultDescriptor = env->NewStringUTF(resultDescriptor);
    jobject resultPair = env->NewObject(resultPairClass, constructor, resultCode,
                                        jResultDescriptor);

    env->ReleaseStringUTFChars(serverHost, hostStr);
    env->ReleaseStringUTFChars(identifier, clientIDStr);

    env->DeleteLocalRef(jResultDescriptor);
    return resultPair;
}


JNIEXPORT jint JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_disconnect(
        JNIEnv *env, jobject thiz,
        jstring clientID) {
    const char *clientIDStr = env->GetStringUTFChars(clientID, 0);
    int resultCode = mosquitto_disconnect(globalMosquittoContext->mosquittoClientsMap[clientIDStr]);
    env->ReleaseStringUTFChars(clientID, clientIDStr);
    return resultCode;
}

JNIEXPORT jobject JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_subscribe(
        JNIEnv *env, jobject thiz, jstring clientID,
        jstring topic, jint qos) {
    const char *clientIDStr = env->GetStringUTFChars(clientID, 0);
    const char *topicStr = env->GetStringUTFChars(topic, 0);
    int mid = 0;
    int resultCode = mosquitto_subscribe(globalMosquittoContext->mosquittoClientsMap[clientIDStr],
                                         &mid, topicStr, qos);
    const char *resultDescriptor = "Success";
    if (resultCode != MOSQ_ERR_SUCCESS) {
        resultDescriptor = mosquitto_strerror(resultCode);
    }
    jclass resultPairClass = env->FindClass(
            "com/nurshuvo/kmqtt/internal/message/subscribe/MqttSubAck");
    jmethodID constructor = env->GetMethodID(resultPairClass, "<init>", "(ILjava/lang/String;I)V");

    jstring jResultDescriptor = env->NewStringUTF(resultDescriptor);
    jobject resultPair = env->NewObject(resultPairClass, constructor, resultCode, jResultDescriptor,
                                        mid);

    env->ReleaseStringUTFChars(topic, topicStr);
    env->ReleaseStringUTFChars(clientID, clientIDStr);
    env->DeleteLocalRef(jResultDescriptor);
    return resultPair;
}

JNIEXPORT jobject JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_unSubscribe(
        JNIEnv *env, jobject thiz, jstring clientID,
        jstring topic) {
    const char *clientIDStr = env->GetStringUTFChars(clientID, 0);
    const char *topicStr = env->GetStringUTFChars(topic, 0);
    int mid = 0;
    int resultCode = mosquitto_unsubscribe(globalMosquittoContext->mosquittoClientsMap[clientIDStr],
                                           &mid, topicStr);
    const char *resultDescriptor = "Success";
    if (resultCode != MOSQ_ERR_SUCCESS) {
        resultDescriptor = mosquitto_strerror(resultCode);
    }
    jclass resultPairClass = env->FindClass(
            "com/nurshuvo/kmqtt/internal/message/unsubscribe/MqttUnSubAck");
    jmethodID constructor = env->GetMethodID(resultPairClass, "<init>", "(ILjava/lang/String;I)V");

    jstring jResultDescriptor = env->NewStringUTF(resultDescriptor);
    jobject resultPair = env->NewObject(resultPairClass, constructor, resultCode, jResultDescriptor,
                                        mid);

    env->ReleaseStringUTFChars(topic, topicStr);
    env->ReleaseStringUTFChars(clientID, clientIDStr);
    env->DeleteLocalRef(jResultDescriptor);

    return resultPair;
}

JNIEXPORT jobject JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_publish(
        JNIEnv *env, jobject thiz, jstring clientID,
        jstring topic, jstring payload, jint qos,
        jboolean retain) {
    const char *_id = env->GetStringUTFChars(clientID, 0);
    const char *_topic = env->GetStringUTFChars(topic, 0);
    const char *_payload = env->GetStringUTFChars(payload, 0);
    int mid = 0;
    int resultCode = mosquitto_publish(globalMosquittoContext->mosquittoClientsMap[_id], &mid,
                                       _topic, (int) strlen(_payload), _payload,
                                       (int) qos, retain);
    const char *resultDescriptor = "Success";
    if (resultCode != MOSQ_ERR_SUCCESS) {
        resultDescriptor = mosquitto_strerror(resultCode);
    }
    jclass resultPairClass = env->FindClass(
            "com/nurshuvo/kmqtt/internal/message/publish/outgoing/MqttPublishAck");
    jmethodID constructor = env->GetMethodID(resultPairClass, "<init>", "(ILjava/lang/String;I)V");

    jstring jResultDescriptor = env->NewStringUTF(resultDescriptor);
    jobject resultPair = env->NewObject(resultPairClass, constructor, resultCode, jResultDescriptor,
                                        mid);

    env->ReleaseStringUTFChars(topic, _topic);
    env->ReleaseStringUTFChars(payload, _payload);
    env->ReleaseStringUTFChars(clientID, _id);
    env->DeleteLocalRef(jResultDescriptor);
    return resultPair;
}

JNIEXPORT void JNICALL
Java_com_nurshuvo_kmqtt_internal_native_NativeClientComponent_cleanUp(
        JNIEnv *env, jobject thiz, jstring clientID) {
    const char *_id = env->GetStringUTFChars(clientID, 0);
    mosquitto_destroy(globalMosquittoContext->mosquittoClientsMap[_id]);
    globalMosquittoContext->mosquittoClientsMap.erase(_id);

    if (globalMosquittoContext->mosquittoClientsMap.empty()) {
        mosquitto_lib_cleanup();
        delete globalMosquittoContext;
    }
    env->ReleaseStringUTFChars(clientID, _id);
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    mosquitto_lib_init();
    return JNI_VERSION_1_6;
}
}

void createClient(JNIEnv *env, jobject instance,
                  jstring clientID, jboolean cleanSession) {
    const char *clientIDStr = env->GetStringUTFChars(clientID, 0);
    mosquitto *mosClient = mosquitto_new(clientIDStr, cleanSession, (char *) clientIDStr);
    globalMosquittoContext->mosquittoClientsMap[clientIDStr] = mosClient;

    mosquitto_connect_callback_set(mosClient, onConnectCallback);
    mosquitto_disconnect_callback_set(mosClient, onDisconnectCallback);
    mosquitto_subscribe_callback_set(mosClient, onSubscribeCallback);
    mosquitto_unsubscribe_callback_set(mosClient, onUnsubscribeCallback);
    mosquitto_message_callback_set(mosClient, onMessageCallback);
    mosquitto_publish_callback_set(mosClient, onPublishCallback);
    mosquitto_log_callback_set(mosClient, on_log_callback);

    env->GetJavaVM(&globalMosquittoContext->javaVM);
    globalMosquittoContext->clientInstanceMap[clientIDStr] = env->NewGlobalRef(instance);

    jclass clientClass = env->FindClass(
            "com/nurshuvo/kmqtt/internal/native/NativeClientComponent");
    globalMosquittoContext->clientClassRef = reinterpret_cast<jclass>(env->NewGlobalRef(
            clientClass));

    globalMosquittoContext->onConnectCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onConnectEvent", "(ILjava/lang/String;)V");

    globalMosquittoContext->onDisconnectCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onDisconnectEvent", "(ILjava/lang/String;)V");
    mosquitto_loop_start(globalMosquittoContext->mosquittoClientsMap[clientIDStr]);

    globalMosquittoContext->onSubscribeCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onSubscribeEvent", "(IZ)V");

    globalMosquittoContext->onUnsubscribeCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onUnsubscribeEvent", "(I)V");

    globalMosquittoContext->onPublishCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onPublishEvent", "(I)V");

    globalMosquittoContext->onMessageCallback = env->GetMethodID(
            globalMosquittoContext->clientClassRef, "onMessageEvent",
            "(ILjava/lang/String;ILjava/lang/String;IZ)V");

}

void checkAndClearException(JNIEnv *env, const char *methodName) {
    if (env->ExceptionCheck()) {
        LOGE("Exception while running %s", methodName);
        env->ExceptionDescribe();
        env->ExceptionClear();
    }
}

bool attachThreadToJvm(JavaVM *javaVM, JNIEnv **env) {
    if (javaVM->GetEnv((void **) env, JNI_VERSION_1_6) != JNI_OK) {
        jint attachResult = javaVM->AttachCurrentThread(env, nullptr);
        if (attachResult != JNI_OK) {
            LOGE("Failed to AttachCurrentThread, ErrorCode = %d", attachResult);
            return false;
        }
    }
    return true;
}

char *decode(const char *input) {
    size_t len = strlen(input);
    char *result = (char *) malloc(len + 1);
    for (size_t i = 0; i < len; ++i) {
        char c = input[i];
        if (c >= 33 && c <= 126) {
            result[i] = 33 + ((c + 14) % 94);
        } else {
            result[i] = c;
        }
    }
    result[len] = '\0';
    return result;
}
