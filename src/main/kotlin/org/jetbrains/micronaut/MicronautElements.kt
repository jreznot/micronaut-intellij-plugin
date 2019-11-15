package org.jetbrains.micronaut

const val APPLICATION_CLASS = "io.micronaut.runtime.Micronaut"

const val CONTROLLER_CLASS = "io.micronaut.http.annotation.Controller"
const val SINGLETON_CLASS = "javax.inject.Singleton"

const val INJECT = "javax.inject.Inject"
const val PROPERTY = "io.micronaut.context.annotation.Property"
const val VALUE = "io.micronaut.context.annotation.Value"

val BEAN_ANNOTATIONS = listOf(CONTROLLER_CLASS, SINGLETON_CLASS)

const val HEAD_METHOD = "io.micronaut.http.annotation.Head"
const val GET_METHOD = "io.micronaut.http.annotation.Get"
const val POST_METHOD = "io.micronaut.http.annotation.Post"
const val DELETE_METHOD = "io.micronaut.http.annotation.Delete"
const val PUT_METHOD = "io.micronaut.http.annotation.Put"
const val PATCH_METHOD = "io.micronaut.http.annotation.Patch"

const val ERROR_METHOD = "io.micronaut.http.annotation.Error"

const val SCHEDULED_METHOD = "io.micronaut.scheduling.annotation.Scheduled"
const val SCHEDULED_CRON_ATTRIBUTE = "cron"

const val EVENT_LISTENER = "io.micronaut.runtime.event.annotation.EventListener"
const val EVENT_PUBLISHER = "io.micronaut.context.event.ApplicationEventPublisher"
const val PUBLISH_EVENT_METHOD = "publishEvent"

val METHOD_ANNOTATIONS = listOf(
    HEAD_METHOD,
    GET_METHOD,
    POST_METHOD,
    DELETE_METHOD,
    PUT_METHOD,
    PATCH_METHOD,
    ERROR_METHOD
)