/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhenai.lib.idea.i18n

import com.intellij.CommonBundle
import com.zhenai.lib.idea.config.ZhenaiConfig
import com.zhenai.lib.common.util.getService
import com.zhenai.lib.core.slang.utils.I18nResources
import java.util.*

/**
 *
 *
 * @author dengqu
 * @date 2017/06/20
 */
object P3cBundle {
    private val p3cConfig = ZhenaiConfig::class.java.getService()
    private val resourceBundle = ResourceBundle.getBundle(
        "messages.P3cBundle",
        Locale(p3cConfig.locale), I18nResources.XmlControl()
    )

    fun getMessage(key: String): String {
        return resourceBundle.getString(key).trim()
    }

    fun message(key: String, vararg params: Any): String {
        return CommonBundle.message(resourceBundle, key, *params).trim()
    }
}
