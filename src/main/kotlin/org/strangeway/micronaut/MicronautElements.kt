package org.strangeway.micronaut

const val CONTROLLER_CLASS = "io.micronaut.http.annotation.Controller"
const val SINGLETON_CLASS = "javax.inject.Singleton"

val BEAN_ANNOTATIONS = listOf(CONTROLLER_CLASS, SINGLETON_CLASS)

const val HEAD_METHOD = "io.micronaut.http.annotation.Head"
const val GET_METHOD = "io.micronaut.http.annotation.Get"
const val POST_METHOD = "io.micronaut.http.annotation.Post"
const val DELETE_METHOD = "io.micronaut.http.annotation.Delete"
const val PUT_METHOD = "io.micronaut.http.annotation.Put"
const val PATCH_METHOD = "io.micronaut.http.annotation.Patch"

const val ERROR_METHOD = "io.micronaut.http.annotation.Error"

val METHOD_ANNOTATIONS = listOf(
    HEAD_METHOD, GET_METHOD, POST_METHOD, DELETE_METHOD, PUT_METHOD, PATCH_METHOD,
    ERROR_METHOD
)