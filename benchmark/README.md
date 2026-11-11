# Benchmarks

Four things are measured. Startup and frame timing need a real device. The rest runs anywhere.

| What | How | Where |
| --- | --- | --- |
| Startup (cold, warm, hot) | Macrobenchmark `StartupBenchmark` | physical device |
| Frame timing (scroll, tab switches) | Macrobenchmark `UiSmoothnessBenchmark` | physical device |
| Notification pipeline cost | unit test `NotificationPipelineBenchmarkTest` | any machine |
| APK size and build time | `scripts/benchmark-size.sh`, `scripts/benchmark_build.py` | any machine |

## Before you run on a device

1. Use a physical phone running Android 9 or newer. Emulators give noisy numbers.
2. Nothing to install by hand: the Gradle task installs the app, the benchmarks click through onboarding, and the app is uninstalled again afterwards.
3. Close other apps' activity: airplane mode on, screen on, battery above 50 percent, no charging animation.
4. Optional but steadier: `adb shell cmd power set-fixed-performance-mode-enabled true`.

## Run

```bash
# startup and frame timing (device connected)
./gradlew :benchmark:connectedBenchmarkAndroidTest

# notification pipeline (JVM)
RUN_BENCHMARKS=1 ./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest \
    --tests '*NotificationPipelineBenchmarkTest'

# size and build time
./scripts/benchmark-size.sh
python3 scripts/benchmark_build.py --runs 3
```

Results land in:

- `benchmark/build/outputs/connected_android_test_additional_output/` (Macrobenchmark JSON and traces; open the `.perfetto-trace` files in https://ui.perfetto.dev)
- `app/build/benchmark-results/notification-pipeline.json`
- `build/benchmark-results/size.json` and `build-time.json`

## Compare before and after

Run everything once on this branch before changing anything (the baseline), copy the JSON files somewhere, make your change, run again, then:

```bash
python3 scripts/compare_benchmarks.py baseline/size.json build/benchmark-results/size.json
```

It prints every metric with the percentage change. Lower is better for all of them.

## Reading the numbers

- `timeToInitialDisplayMs`: time until the first frame. Compare the median.
- `frameDurationCpuMs` P50 / P90 / P95 / P99: CPU time per frame. P95 and P99 show jank. Frames over 16.6 ms (60 Hz) or 8.3 ms (120 Hz) can drop.
- `frameOverrunMs`: how far past the deadline a frame ran. Negative is good.
- Pipeline `p50_us` and `p99_us`: microseconds of our own logic per notification, with Room, DataStore and audio replaced by fakes.

Only trust a difference larger than the spread between your own repeated runs. Run the baseline twice to see that spread.

## Troubleshooting

- `Unable to acquire intent for package`: the benchmark app could not see the target app. The `<queries>` entry in `benchmark/src/main/AndroidManifest.xml` fixes this, and the app must be installed from the `benchmark` build (the Gradle task does that).
- `DROP_SHADER_CACHE broadcast was not received`: open the app once on the device so it leaves the "stopped" state, then run again. To test the receiver by hand:

  ```bash
  adb shell am broadcast -a androidx.profileinstaller.action.BENCHMARK_OPERATION \
      -e EXTRA_BENCHMARK_OPERATION DROP_SHADER_CACHE \
      com.kyawzinlinn.app.alert/androidx.profileinstaller.ProfileInstallReceiver
  ```

  It should print `result=14`.
- "Could not get past onboarding automatically": the onboarding screens changed. Update the button labels in `Onboarding.kt`.

## CI

The `Benchmark` workflow runs on pushes to `perf/**` branches and on demand. It covers the JVM pipeline, APK size and build time. Startup and frame timing stay manual because hosted runners have no physical device.
