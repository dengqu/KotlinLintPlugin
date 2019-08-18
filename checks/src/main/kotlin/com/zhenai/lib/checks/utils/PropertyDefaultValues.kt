package com.zhenai.lib.checks.utils


import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PropertyDefaultValues(vararg val value: PropertyDefaultValue)