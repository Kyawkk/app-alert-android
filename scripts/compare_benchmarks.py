#!/usr/bin/env python3
"""Compares two benchmark result files (before, after) and prints the change per metric.

Accepts Macrobenchmark *-benchmarkData.json files and the JSON files written by
scripts/benchmark-size.sh, scripts/benchmark_build.py and the pipeline unit test.

  scripts/compare_benchmarks.py before.json after.json
"""
import json
import sys


def flatten(node, path=""):
    if isinstance(node, dict):
        if "benchmarks" in node:
            for bench in node["benchmarks"]:
                base = f"{bench['className'].split('.')[-1]}.{bench['name']}"
                for metric, stats in bench.get("metrics", {}).items():
                    yield f"{base}.{metric}.median", stats["median"]
                for metric, stats in bench.get("sampledMetrics", {}).items():
                    for percentile in ("P50", "P90", "P95", "P99"):
                        if percentile in stats:
                            yield f"{base}.{metric}.{percentile}", stats[percentile]
            return
        for key, value in node.items():
            yield from flatten(value, f"{path}.{key}" if path else key)
    elif isinstance(node, (int, float)) and not isinstance(node, bool):
        yield path, node


def load(file):
    with open(file) as handle:
        return dict(flatten(json.load(handle)))


def main() -> None:
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    before, after = load(sys.argv[1]), load(sys.argv[2])
    names = [n for n in before if n in after]
    width = max((len(n) for n in names), default=10)
    print(f"{'metric':<{width}}  {'before':>12}  {'after':>12}  {'change':>8}")
    for name in names:
        b, a = before[name], after[name]
        change = f"{(a - b) / b * 100:+.1f}%" if b else "n/a"
        print(f"{name:<{width}}  {b:>12.2f}  {a:>12.2f}  {change:>8}")
    print("\nLower is better for every metric here.")


if __name__ == "__main__":
    main()
