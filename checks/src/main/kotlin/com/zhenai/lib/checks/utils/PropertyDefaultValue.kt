package com.zhenai.lib.checks.utils


import java.lang.annotation.Repeatable
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
@Repeatable(PropertyDefaultValues::class)
annotation class PropertyDefaultValue(val language: Language, val defaultValue: String)