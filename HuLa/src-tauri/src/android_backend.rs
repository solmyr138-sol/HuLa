//! Android 本地开发：真机用 LAN IP，模拟器自动改用 10.0.2.2

use crate::configuration::BackendSettings;

const EMULATOR_LOOPBACK_HOST: &str = "10.0.2.2";

/// 仅在 active_config=local 时调用：模拟器把 backend 主机换成 10.0.2.2
pub fn apply_local_emulator_hosts(backend: &mut BackendSettings) {
    #[cfg(target_os = "android")]
    {
        if !is_android_emulator() {
            return;
        }
        use tracing::info;
        let before_api = backend.base_url.clone();
        let before_ws = backend.ws_url.clone();
        backend.base_url = rewrite_url_host(&backend.base_url, EMULATOR_LOOPBACK_HOST);
        backend.ws_url = rewrite_url_host(&backend.ws_url, EMULATOR_LOOPBACK_HOST);
        info!(
            "Android emulator backend: {} -> {}, {} -> {}",
            before_api, backend.base_url, before_ws, backend.ws_url
        );
    }
}

#[cfg(target_os = "android")]
fn is_android_emulator() -> bool {
    if std::path::Path::new("/dev/qemu_pipe").exists()
        || std::path::Path::new("/dev/socket/qemud").exists()
    {
        return true;
    }
    emulator_sysprop("ro.kernel.qemu").as_deref() == Some("1")
        || matches!(
            emulator_sysprop("ro.hardware").as_deref(),
            Some("goldfish" | "ranchu" | "qemu" | "vexpress")
        )
        || emulator_sysprop("ro.product.model")
            .map(|m| {
                let lower = m.to_lowercase();
                lower.contains("sdk") || lower.contains("emulator") || lower.contains("simulator")
            })
            .unwrap_or(false)
}

#[cfg(target_os = "android")]
fn emulator_sysprop(key: &str) -> Option<String> {
    let output = std::process::Command::new("getprop").arg(key).output().ok()?;
    if !output.status.success() {
        return None;
    }
    let value = String::from_utf8_lossy(&output.stdout).trim().to_string();
    if value.is_empty() {
        None
    } else {
        Some(value)
    }
}

fn rewrite_url_host(raw: &str, new_host: &str) -> String {
    match url::Url::parse(raw) {
        Ok(mut url) => {
            if url.set_host(Some(new_host)).is_err() {
                return raw.to_string();
            }
            url.to_string()
        }
        Err(_) => raw.to_string(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn rewrite_url_host_replaces_host() {
        let out = rewrite_url_host("http://192.168.1.154:18760/api", "10.0.2.2");
        assert_eq!(out, "http://10.0.2.2:18760/api");
    }

    #[test]
    fn rewrite_ws_url_host() {
        let out = rewrite_url_host("ws://192.168.1.154:18760/api/ws/ws", "10.0.2.2");
        assert_eq!(out, "ws://10.0.2.2:18760/api/ws/ws");
    }
}
