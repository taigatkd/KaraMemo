package com.taigatkd.karamemo.common

import android.content.Context
import androidx.annotation.StringRes

interface StringResolver {
    fun get(@StringRes resId: Int, vararg formatArgs: Any): String
}

class AndroidStringResolver(
    private val context: Context,
) : StringResolver {
    override fun get(resId: Int, vararg formatArgs: Any): String =
        context.getString(resId, *formatArgs)
}
