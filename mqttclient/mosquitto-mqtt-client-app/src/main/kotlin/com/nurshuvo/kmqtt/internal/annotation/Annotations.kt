package com.nurshuvo.kmqtt.internal.annotation

/**
 * Marks a field as crucial for JNI access.
 *
 * **Important:** Fields annotated with this must be renamed with care, as they are
 * accessed directly from native code.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class CarefulFieldForJNI

/**
 * Marks a class as crucial for JNI access.
 *
 * **Important:** Class annotated with this must be renamed with care, as they are
 * accessed directly from native code.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CarefulRenameClassForJNI
