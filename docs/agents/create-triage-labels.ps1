# Create Matt Pocock triage labels on the current repo's GitHub remote.
# Prereq: gh auth login (once). See docs/agents/README.md

$ErrorActionPreference = "Stop"

$ghCandidates = @(
  "gh",
  "$env:ProgramFiles\GitHub CLI\gh.exe",
  "${env:ProgramFiles(x86)}\GitHub CLI\gh.exe"
)

$gh = $null
foreach ($c in $ghCandidates) {
  if ($c -eq "gh") {
    $cmd = Get-Command gh -ErrorAction SilentlyContinue
    if ($cmd) { $gh = $cmd.Source; break }
  } elseif (Test-Path $c) {
    $gh = $c
    break
  }
}

if (-not $gh) {
  Write-Error "gh not found. Install from https://cli.github.com/ and restart the terminal."
}

Write-Host "Using: $gh"
& $gh auth status
if ($LASTEXITCODE -ne 0) {
  Write-Host ""
  Write-Host "Run first:  gh auth login"
  Write-Host "Then run this script again."
  exit 1
}

$labels = @(
  @{ name = "needs-triage";      color = "FEF2C0"; description = "Maintainer needs to evaluate this issue" },
  @{ name = "needs-info";        color = "D4C5F9"; description = "Waiting on reporter for more information" },
  @{ name = "ready-for-agent";   color = "C2E0FF"; description = "Fully specified, ready for an AFK agent" },
  @{ name = "ready-for-human";   color = "BFDADC"; description = "Requires human implementation" },
  @{ name = "wontfix";           color = "E4E4E4"; description = "Will not be actioned" }
)

# --force creates or updates; avoids jq (labels like needs-triage break jq: needs - triage)
foreach ($l in $labels) {
  Write-Host "Ensuring label: $($l.name)"
  & $gh label create $l.name --color $l.color --description $l.description --force
  if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to create label: $($l.name)"
  }
}

Write-Host ""
Write-Host "Done. Current labels matching triage:"
& $gh label list | Select-String -Pattern "needs-triage|needs-info|ready-for-agent|ready-for-human|wontfix"
