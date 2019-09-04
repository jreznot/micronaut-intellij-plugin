/*
 *  Copyright (c) 2008-2016 StrangeWayOrg.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.strangeway.micronaut

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

val METHOD_ANNOTATIONS = listOf(
    HEAD_METHOD, GET_METHOD, POST_METHOD, DELETE_METHOD, PUT_METHOD, PATCH_METHOD,
    ERROR_METHOD, SCHEDULED_METHOD
)