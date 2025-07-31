package com.nurshuvo.kmqtt.internal.native

import androidx.annotation.Keep
import com.nurshuvo.kmqtt.internal.MqttClientConfig
import com.nurshuvo.kmqtt.internal.annotation.CarefulRenameClassForJNI
import com.nurshuvo.kmqtt.internal.message.connack.MqttConnAck
import com.nurshuvo.kmqtt.internal.message.publish.outgoing.MqttPublishAck
import com.nurshuvo.kmqtt.internal.message.subscribe.MqttSubAck
import com.nurshuvo.kmqtt.internal.message.unsubscribe.MqttUnSubAck

@Keep
@CarefulRenameClassForJNI
abstract class NativeClientComponent :
    NativeConnectEvent,
    NativeDisconnectEvent,
    NativeSubscribeEvent,
    NativeUnsubscribeEvent,
    NativePublishEvent,
    NativeMessageEvent {

    external fun connect(
        clientConfig: MqttClientConfig,
    ): MqttConnAck

    external fun disconnect(
        clientID: String?,
    ): Int

    external fun subscribe(
        clientID: String,
        topic: String,
        qos: Int,
    ): MqttSubAck

    external fun unSubscribe(
        clientID: String,
        topic: String,
    ): MqttUnSubAck

    external fun publish(
        clientID: String,
        topic: String,
        payload: String,
        qos: Int,
        retain: Boolean,
    ): MqttPublishAck

    external fun cleanUp(
        clientID: String?,
    )
}
