package me.thanel.linecalendar.preference

import android.content.Context
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class PreferenceContainer(context: Context, private val keyPrefix: String) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun clear(): Boolean {
        val editor = preferences.edit()
        for ((key, _) in preferences.all) {
            if (key.startsWith(keyPrefix)) {
                editor.remove(key)
            }
        }
        return editor.commit()
    }

    protected inline fun <reified T : Any> bindPreference(default: T) =
        BindPreference(T::class, default)

    private fun getKey(key: String) = "$keyPrefix$key"

    class BindPreference<T : Any>(
        private val clazz: KClass<out T>,
        private val default: T
    ) : ReadWriteProperty<PreferenceContainer, T> {
        private val enumConstants = clazz.java.enumConstants

        override fun getValue(thisRef: PreferenceContainer, property: KProperty<*>): T {
            val widgetPrefKey = thisRef.getKey(property.name)
            val prefs = thisRef.preferences
            @Suppress("UNCHECKED_CAST")
            return when {
                clazz == Boolean::class -> prefs.getBoolean(widgetPrefKey, default as Boolean) as T
                clazz == String::class -> prefs.getString(widgetPrefKey, default as String) as T
                clazz == Set::class -> {
                    prefs.getStringSet(widgetPrefKey, default as Set<String>) as T
                }
                clazz.java.isEnum -> {
                    val value = prefs.getString(widgetPrefKey, (default as Enum<*>).name)
                    enumConstants.first { (it as Enum<*>).name == value }
                }
                else -> throw IllegalArgumentException("Unsupported preference type: $clazz")
            }
        }

        override fun setValue(thisRef: PreferenceContainer, property: KProperty<*>, value: T) {
            val widgetPrefKey = thisRef.getKey(property.name)
            val editor = thisRef.preferences.edit()
            @Suppress("UNCHECKED_CAST")
            when {
                clazz == Boolean::class -> editor.putBoolean(widgetPrefKey, value as Boolean)
                clazz == String::class -> editor.putString(widgetPrefKey, value as String)
                clazz == Set::class -> editor.putStringSet(widgetPrefKey, value as Set<String>)
                clazz.java.isEnum -> editor.putString(widgetPrefKey, (value as Enum<*>).name)
                else -> throw IllegalArgumentException("Unsupported preference type: $clazz")
            }
            editor.apply()
        }
    }
}
