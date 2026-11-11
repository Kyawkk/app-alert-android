#!/usr/bin/env python3
"""Times Gradle builds. Output: build/benchmark-results/build-time.json

  scripts/benchmark_build.py [--runs N]
"""
import argparse
import json
import statistics
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GRADLE = [str(ROOT / "gradlew"), "--console=plain", "-q", "--no-build-cache"]


def run(*args: str) -> float:
    start = time.perf_counter()
    subprocess.run([*GRADLE, *args], cwd=ROOT, check=True)
    return time.perf_counter() - start


def stop_daemons() -> None:
    # --stop only ends the Gradle daemons; a stale Kotlin daemon would break the next build
    subprocess.run([str(ROOT / "gradlew"), "--stop", "-q"], cwd=ROOT, check=False)
    subprocess.run(["pkill", "-f", "KotlinCompileDaemon"], check=False)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--runs", type=int, default=3)
    runs = parser.parse_args().runs

    stop_daemons()  # start from cold daemons
    clean_release = [run("clean", "assembleRelease", "--no-configuration-cache") for _ in range(runs)]
    clean_debug = [run("clean", "assembleDebug", "--no-configuration-cache") for _ in range(runs)]
    up_to_date = [run("assembleDebug") for _ in range(runs)]

    result = {
        name: {"median_s": round(statistics.median(values), 1), "runs_s": [round(v, 1) for v in values]}
        for name, values in {
            "clean_release": clean_release,
            "clean_debug": clean_debug,
            "up_to_date_debug": up_to_date,
        }.items()
    }
    out = ROOT / "build" / "benchmark-results"
    out.mkdir(parents=True, exist_ok=True)
    (out / "build-time.json").write_text(json.dumps(result, indent=2) + "\n")
    for name, value in result.items():
        print(f"{name:18} median {value['median_s']:6.1f}s  runs {value['runs_s']}")


if __name__ == "__main__":
    main()
