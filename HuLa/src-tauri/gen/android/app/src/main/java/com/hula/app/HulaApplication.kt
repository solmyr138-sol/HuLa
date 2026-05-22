package com.hula.app

import android.app.Application

/**
 * Load libc++ before Tauri/Rust native libs (fixes __cxa_pure_virtual on x86_64 emulator).
 */
class HulaApplication : Application() {
    companion object {
        init {
            System.loadLibrary("c++_shared")
        }
    }
}
