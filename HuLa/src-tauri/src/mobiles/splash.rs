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
    use jni::{JavaVM, objects::JObject};

    fn invoke(method: &str) -> Option<()> {
        let ctx = std::panic::catch_unwind(ndk_context::android_context).ok()?;
        let vm = unsafe { JavaVM::from_raw(ctx.vm().cast()) }.ok()?;
        let mut env = vm.attach_current_thread().ok()?;
        let activity = unsafe { JObject::from_raw(ctx.context() as jni::sys::jobject) };

        let result = env.call_method(&activity, method, "()V", &[]);
        let _ = activity.into_raw();

        match result {
            Ok(_) => Some(()),
            Err(err) => {
                if env.exception_check().unwrap_or(false) {
                    let _ = env.exception_describe();
                    let _ = env.exception_clear();
                }
                tracing::warn!("[Splashscreen] JNI {} failed: {}", method, err);
                None
            }
        }
    }

    pub fn show() {
        // 启动页由 MainActivity launch_screen 负责
        let _ = invoke("show");
    }

    pub fn hide() {
        let _ = invoke("hide");
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
    tracing::info!("hide_splash_screen called from frontend");
    hide();
    Ok(())
}
