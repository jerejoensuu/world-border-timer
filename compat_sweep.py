#!/usr/bin/env python3
import subprocess
import shutil
import time
from pathlib import Path

# ---------------------------------------------------------------------------
# CONFIG
# ---------------------------------------------------------------------------

# Define the Minecraft versions you want to test
MC_VERSIONS = [
    # "1.21.11",
    # "1.21.10",
    # "1.21.9",
    # "1.21.8",
    # "1.21.7",
    # "1.21.6",
    # "1.21.5",
    # "1.21.4",
    # "1.21.3",
    # "1.21.2",
    # "1.21.1",
    # "1.21",
    # "1.20.6",
    "1.20.5",
    "1.20.4",
    "1.20.3",
    "1.20.2",
    "1.20.1",
    "1.20",
    # "1.19.4",
    # "1.19.3",
    # "1.19.2",
    # "1.19.1",
    # "1.19",
]

PER_VERSION_TIMEOUT = 120  # seconds, bump if needed

PROJECT_ROOT = Path(__file__).resolve().parent

# Your HeadlessMC install folder
HMC_HOME = Path(r"C:\Users\jerep\Documents\HeadlessMC")

# Launcher jar
HMC_LAUNCHER = HMC_HOME / "headlessmc-launcher.jar"

# Must match hmc.gamedir in HeadlessMC/config.properties
GAME_DIR = Path(r"C:\Users\jerep\Documents\HeadlessMC\HeadlessMC")

# Where you downloaded your fabric-api jars
FABRIC_API_DIR = Path(r"C:\Users\jerep\Documents\HeadlessMC\fabric-api")

# Pattern to find your mod jar from Gradle
MAIN_JAR_PATTERN = "world-border-timer-*.jar"

LATEST_LOG = GAME_DIR / "logs" / "latest.log"


# ---------------------------------------------------------------------------
# UTILITIES
# ---------------------------------------------------------------------------


def find_single_jar(pattern: str) -> Path:
    libs_dir = PROJECT_ROOT / "build" / "libs"
    all_matches = sorted(libs_dir.glob(pattern))
    # Filter out sources/dev jars etc
    matches = [
        p
        for p in all_matches
        if not (
            p.name.endswith("-sources.jar")
            or p.name.endswith("-dev.jar")
            or "sources" in p.name
        )
    ]
    if not matches:
        raise SystemExit(
            f"No non-sources jar found matching {pattern} in {libs_dir}. "
            f"Found only: {[p.name for p in all_matches]}"
        )
    matches.sort(key=lambda p: p.stat().st_mtime, reverse=True)
    return matches[0]


def find_fabric_api_for(version: str) -> Path:
    """
    Matches files like:
        fabric-api-0.xx.x+1.19.jar
        fabric-api-0.xx.x+1.19.1.jar
        fabric-api-0.xx.x+1.19.3.jar

    Matching rule:
        version "1.19"   → match '*+1.19.jar'
        version "1.19.1" → match '*+1.19.1.jar'
        version "1.19.3" → match '*+1.19.3.jar'
    """
    # Pattern: anything ending with +<version>.jar
    search_pattern = f"*+{version}.jar"
    candidates = list(FABRIC_API_DIR.glob(search_pattern))

    if not candidates:
        # Try prefix match to handle variants like 1.19 → 1.19.x
        major_minor = ".".join(version.split(".")[:2])
        prefix_pattern = f"*+{major_minor}*.jar"
        candidates = list(FABRIC_API_DIR.glob(prefix_pattern))

    if not candidates:
        raise SystemExit(
            f"No fabric-api jar found for MC version {version} in {FABRIC_API_DIR}"
        )

    # Pick newest file by modification time
    candidates.sort(key=lambda p: p.stat().st_mtime, reverse=True)
    return candidates[0]


def clear_mods_folder():
    """Remove all old mods from compat gameDir."""
    mods_dir = GAME_DIR / "mods"
    mods_dir.mkdir(parents=True, exist_ok=True)
    for file in mods_dir.glob("*"):
        try:
            file.unlink()
        except Exception:
            pass
    return mods_dir


def ensure_logs_dir():
    """Ensure the logs directory exists."""
    logs_dir = PROJECT_ROOT / "logs"
    logs_dir.mkdir(exist_ok=True, parents=True)
    return logs_dir


def run_hmc_for_version(version: str) -> tuple[bool, str]:
    """Runs HeadlessMC launcher, feeds it commands, waits for Minecraft exit."""
    if not HMC_LAUNCHER.exists():
        raise SystemExit(f"Missing launcher: {HMC_LAUNCHER}")

    if not HMC_HOME.exists():
        raise SystemExit(f"Missing HMC home: {HMC_HOME}")

    if LATEST_LOG.exists():
        try:
            LATEST_LOG.unlink()
        except OSError:
            pass

    # Ensure logs directory exists
    logs_dir = ensure_logs_dir()

    cmd = [
        "java",
        "-jar",
        str(HMC_LAUNCHER),
    ]

    # Commands sent into the HMC shell
    commands = f"launch fabric:{version} \nexit\n"

    print(f"\n[{version}] Launching HeadlessMC...")

    try:
        proc = subprocess.run(
            cmd,
            input=commands,  # string is fine in text mode
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            cwd=HMC_HOME,
            timeout=PER_VERSION_TIMEOUT,
            text=True,
            encoding="utf-8",
            errors="ignore",
        )
    except subprocess.TimeoutExpired as e:
        stdout = e.stdout or ""
        stderr = e.stderr or ""
        combined = stdout + "\n" + stderr

        logpath = logs_dir / f"compat-{version}.log"
        logpath.write_text(combined, encoding="utf-8")

        detail = f"TIMEOUT after {PER_VERSION_TIMEOUT}s"
        print(f"[{version}] FAIL: {detail}")
        return False, detail

    stdout = proc.stdout or ""
    stderr = proc.stderr or ""
    combined = stdout + "\n" + stderr

    logpath = logs_dir / f"compat-{version}.log"
    logpath.write_text(combined, encoding="utf-8")

    if proc.returncode != 0:
        detail = f"Exit code {proc.returncode}"
        print(f"[{version}] FAIL: {detail}")
        return False, detail

    print(f"[{version}] OK")
    return True, "OK"


# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------


def main():
    if not GAME_DIR.exists():
        raise SystemExit(
            f"GAME_DIR {GAME_DIR} does not exist.\n"
            "It MUST match hmc.gamedir in HeadlessMC/config.properties"
        )

    main_jar = find_single_jar(MAIN_JAR_PATTERN)

    print(f"Using mod jar:          {main_jar}")
    print(f"Using fabric-apis from: {FABRIC_API_DIR}")
    print(f"Using HMC:              {HMC_HOME}")
    print(f"Using gameDir:          {GAME_DIR}")

    results = {}

    for version in MC_VERSIONS:
        print(f"\n=== Preparing {version} ===")

        mods_dir = clear_mods_folder()
        fabric_api = find_fabric_api_for(version)

        # Copy mod + fabric-api
        shutil.copy2(main_jar, mods_dir / main_jar.name)
        shutil.copy2(fabric_api, mods_dir / fabric_api.name)

        print(f"Copied fabric-api: {fabric_api.name}")

        ok, detail = run_hmc_for_version(version)
        results[version] = (ok, detail)

        time.sleep(2)

    print("\n========== COMPATIBILITY SUMMARY ==========")
    for v, (ok, detail) in results.items():
        print(f"{v:10} {'OK' if ok else 'FAIL'}  {detail}")


if __name__ == "__main__":
    main()
