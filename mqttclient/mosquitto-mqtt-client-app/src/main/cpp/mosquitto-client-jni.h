#include <android/log.h>
#include <jni.h>
#include <mosquitto.h>
#include <string>
#include <map>

using namespace std;

static const char *kTAG = "MosquittoClient";
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

void checkAndClearException(JNIEnv *env, const char *methodName);

bool attachThreadToJvm(JavaVM *javaVM, JNIEnv **env);

void createClient(JNIEnv *env, jobject instance, jstring clientID, jboolean cleanSession);

char* decode(const char* input);

typedef struct mosquitto_context {
    JavaVM *javaVM;
    jclass clientClassRef;
    map<string, mosquitto *> mosquittoClientsMap;
    map<string, jobject> clientInstanceMap;
    jmethodID onConnectCallback;
    jmethodID onSubscribeCallback;
    jmethodID onMessageCallback;
    jmethodID onUnsubscribeCallback;
    jmethodID onPublishCallback;
    jmethodID onDisconnectCallback;
} MosquittoContext;

MosquittoContext *globalMosquittoContext = new MosquittoContext();