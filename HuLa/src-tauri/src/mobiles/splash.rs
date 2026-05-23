#[cfg(target_os = "ios")]
mod platform {
    unsafe extern "C" {
        fn hula_show_splashscreen();
        fn hula_hide_splashscreen();
    }

    pub fn show() {
        unsafe { hula_show_splashscreen() };
    }

    pub fn hide() {
        unsafe { hula_hide_splashscreen() };
    }
}

#[cfg(target_os = "android")]
mod platform {
    // ndk_context::android_context() 在未就绪时会 abort（非 panic），catch_unwind 无效。
    // 启动页由 MainActivity launch_screen + onWebViewCreate/onProgressChanged 负责。
    pub fn show() {}

    pub fn hide() {
        tracing::debug!("[Splashscreen] hide ignored on Android (native launch_screen)");
    }
}

#[cfg(not(any(target_os = "ios", target_os = "android")))]
mod platform {
    pub fn show() {}
    pub fn hide() {}
}

pub fn show() {
    platform::show();
}

pub fn hide() {
    platform::hide();
}

/// Tauri command: 隐藏启动画面（由前端调用）
#[tauri::command]
pub fn hide_splash_screen() -> Result<(), String> {
    #[cfg(target_os = "android")]
    {
        tracing::debug!("hide_splash_screen: no-op on Android (MainActivity handles launch_screen)");
        return Ok(());
    }
    tracing::info!("hide_splash_screen called from frontend");
    hide();
    Ok(())
}
