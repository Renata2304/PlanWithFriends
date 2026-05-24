buildscript {
    extra.apply {
        set("room_version", "2.6.1")
        set("nav_version", "2.8.4")
    }
}

plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
}