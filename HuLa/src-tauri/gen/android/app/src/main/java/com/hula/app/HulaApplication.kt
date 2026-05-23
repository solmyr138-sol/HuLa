package com.hula.app

import android.app.Application

/** Application hook; libc++ is statically linked in libhula_app_lib.so (see .cargo/config.toml). */
class HulaApplication : Application()
