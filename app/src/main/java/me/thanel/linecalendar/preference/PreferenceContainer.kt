package me.thanel.linecalendar.preference

import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.VisibleForTesting
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class PreferenceContainer<T : PreferenceContainer<T>>(
    context: Context,
    private val number: Int
) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    @VisibleForTesting
    internal val defaults = mutableMapOf<String, Any>()

    abstract val identifier: String

    private val keyPrefix get() = "$identifier${number}_"

    fun clear(): Boolean {
        val editor = preferences.edit()
        for (key in preferences.all.keys) {
            if (key.startsWith(keyPrefix)) {
                editor.remove(key)
            }
        }
        return editor.commit()
    }

    fun hasSameSettings(other: T): Boolean =
        getPreferencesMap() == other.getPreferencesMap()

    fun copyFrom(other: T): Boolean {
        val preferenceMap = other.getPreferencesMap()
        val editor = preferences.edit()
        for (pref in preferenceMap) {
            val value = pref.value
            val targetKey = getKey(pref.key)
            @Suppress("UNCHECKED_CAST")
            when (value) {
                null -> editor.putString(targetKey, null)
                is Boolean -> editor.putBoolean(targetKey, value)
                is Float -> editor.putFloat(targetKey, value)
                is Int -> editor.putInt(targetKey, value)
                is Long -> editor.putLong(targetKey, value)
                is String -> editor.putString(targetKey, value)
                is Set<*> -> editor.putStringSet(targetKey, value as Set<String>)
            }
        }
        return editor.commit()
    }

    protected inline fun <reified P : Any> bindPreference(
        key: String,
        default: P
    ): BindPreference<P> = createBinder(P::class, key, default)

    protected fun <T : Any> createBinder(
        clazz: KClass<out T>,
        key: String,
        default: T
    ): BindPreference<T> {
        defaults[key] = default
        return BindPreference(clazz, key, default)
    }

    private fun getKey(key: String) = "$keyPrefix$key"

    private fun getPreferencesMap(): Map<String, Any?> = defaults + preferences.all
        .filter { it.key.startsWith(keyPrefix) }
        .mapKeys { it.key.drop(keyPrefix.length) }

    inner class BindPreference<P : Any>(
        private val clazz: KClass<out P>,
        private val key: String,
        private val default: P
    ) : ReadWriteProperty<PreferenceContainer<T>, P> {
        private val enumConstants = clazz.java.enumConstants

        override fun getValue(thisRef: PreferenceContainer<T>, property: KProperty<*>): P {
            val widgetPrefKey = thisRef.getKey(key)
            val prefs = thisRef.preferences
            @Suppress("UNCHECKED_CAST")
            return when (clazz) {
                Boolean::class -> prefs.getBoolean(widgetPrefKey, default as Boolean) as P
                Float::class -> prefs.getFloat(widgetPrefKey, default as Float) as P
                Int::class -> prefs.getInt(widgetPrefKey, default as Int) as P
                Long::class -> prefs.getLong(widgetPrefKey, default as Long) as P
                String::class -> prefs.getString(widgetPrefKey, default as String) as P
                Set::class -> {
                    prefs.getStringSet(widgetPrefKey, default as Set<String>) as P
                }
                else -> if (clazz.java.isEnum) {
                    val value = prefs.getString(widgetPrefKey, (default as Enum<*>).name)
                    enumConstants.first { (it as Enum<*>).name == value }
                } else {
                    throw IllegalArgumentException("Unsupported preference type: $clazz")
                }
            }
        }

        override fun setValue(thisRef: PreferenceContainer<T>, property: KProperty<*>, value: P) {
            val widgetPrefKey = thisRef.getKey(property.name)
            val editor = thisRef.preferences.edit()
            @Suppress("UNCHECKED_CAST")
            when (clazz) {
                Boolean::class -> editor.putBoolean(widgetPrefKey, value as Boolean)
                Float::class -> editor.putFloat(widgetPrefKey, value as Float)
                Int::class -> editor.putInt(widgetPrefKey, value as Int)
                Long::class -> editor.putLong(widgetPrefKey, value as Long)
                String::class -> editor.putString(widgetPrefKey, value as String)
                Set::class -> editor.putStringSet(widgetPrefKey, value as Set<String>)
                else -> if (clazz.java.isEnum) {
                    editor.putString(widgetPrefKey, (value as Enum<*>).name)
                } else {
                    throw IllegalArgumentException("Unsupported preference type: $clazz")
                }
            }
            editor.apply()
        }
    }
}
