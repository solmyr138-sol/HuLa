use tauri::{Runtime, plugin::TauriPlugin};
#[cfg(not(mobile))]
use tauri_plugin_log::fern::colors::{Color, ColoredLevelConfig};
use tauri_plugin_log::{Target, TargetKind};

pub trait CustomInit {
    fn init_plugin(self) -> Self;
}

fn apply_log_levels(builder: tauri_plugin_log::Builder) -> tauri_plugin_log::Builder {
    builder
        .level(tracing::log::LevelFilter::Info)
        .timezone_strategy(tauri_plugin_log::TimezoneStrategy::UseLocal)
        .level_for("sqlx", tracing::log::LevelFilter::Warn)
        .level_for("sqlx::query", tracing::log::LevelFilter::Warn)
        .level_for("sea_orm", tracing::log::LevelFilter::Warn)
        .level_for("hula_app_lib", tracing::log::LevelFilter::Debug)
        .level_for("tauri", tracing::log::LevelFilter::Warn)
        .level_for("tauri::manager", tracing::log::LevelFilter::Warn)
        .level_for("tauri::event", tracing::log::LevelFilter::Warn)
        .level_for("tauri::plugin", tracing::log::LevelFilter::Warn)
        .level_for("tauri::ipc", tracing::log::LevelFilter::Warn)
        .level_for("tao", tracing::log::LevelFilter::Warn)
        .level_for("wry", tracing::log::LevelFilter::Warn)
        .level_for("tracing::span", tracing::log::LevelFilter::Warn)
}

/// 移动端：Stdout（Android 上由插件转发到 logcat）+ Webview，不用 LogDir/彩色
#[cfg(mobile)]
fn build_log_plugin<R: Runtime>() -> TauriPlugin<R> {
    apply_log_levels(tauri_plugin_log::Builder::new())
        .targets([
            Target::new(TargetKind::Stdout),
            Target::new(TargetKind::Webview),
        ])
        .build()
}

/// 桌面端日志
#[cfg(not(mobile))]
fn build_log_plugin<R: Runtime>() -> TauriPlugin<R> {
    apply_log_levels(tauri_plugin_log::Builder::new())
        .targets([
            Target::new(TargetKind::Stdout),
            Target::new(TargetKind::Webview),
            Target::new(TargetKind::LogDir {
                file_name: Some("logs".to_string()),
            }),
        ])
        .with_colors(ColoredLevelConfig {
            error: Color::Red,
            warn: Color::Yellow,
            debug: Color::White,
            info: Color::Green,
            trace: Color::White,
        })
        .build()
}

/// 初始化公共插件（所有平台通用）
pub fn init_common_plugins<R: Runtime>(builder: tauri::Builder<R>) -> tauri::Builder<R> {
    let builder = builder
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_http::init())
        .plugin(tauri_plugin_upload::init())
        .plugin(tauri_plugin_sql::Builder::new().build())
        .plugin(tauri_plugin_notification::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_clipboard_manager::init());

    #[cfg(desktop)]
    let builder = builder.plugin(tauri_plugin_mic_recorder::init());

    builder.plugin(build_log_plugin())
}
