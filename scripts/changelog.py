#!/usr/bin/env python3
"""Builds release notes from conventional commit messages.

  scripts/changelog.py                      rewrite CHANGELOG.md from every v* tag
  scripts/changelog.py --notes [FROM [TO]]  print the notes for FROM..TO (defaults: latest tag..HEAD)

Only feat, fix, perf and refactor commits are listed. docs, ci, chore, test, build and
style commits, merges and messages that are not in "type(scope): subject" form are skipped.
The first tag also covers everything before it.
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUTPUT = ROOT / "CHANGELOG.md"
SECTIONS = [("feat", "Added"), ("fix", "Fixed"), ("perf", "Performance"), ("refactor", "Changed")]
COMMIT = re.compile(r"^(\w+)(?:\(([^)]+)\))?!?: (.+)$")


def git(*args):
    return subprocess.run(["git", *args], cwd=ROOT, check=True, capture_output=True, text=True).stdout.strip()


def tags():
    names = git("tag", "--list", "v[0-9]*", "--sort=version:refname").splitlines()
    return [t for t in names if re.fullmatch(r"v\d+\.\d+\.\d+", t)]


def subjects(start, end):
    rev = f"{start}..{end}" if start else end
    return git("log", "--no-merges", "--reverse", "--format=%s", rev).splitlines()


def entries(start, end):
    grouped = {kind: [] for kind, _ in SECTIONS}
    for subject in subjects(start, end):
        match = COMMIT.match(subject)
        if not match or match.group(1) not in grouped:
            continue
        kind, scope, text = match.groups()
        line = f"- {'**' + scope + ':** ' if scope else ''}{text[0].lower() + text[1:]}"
        if line not in grouped[kind]:
            grouped[kind].append(line)
    return grouped


def render(grouped):
    blocks = [f"### {title}\n\n" + "\n".join(grouped[kind]) for kind, title in SECTIONS if grouped[kind]]
    return "\n\n".join(blocks)


def notes(start, end):
    return render(entries(start, end)) or "No user-facing changes."


def changelog():
    names = tags()
    parts = [
        "# Changelog\n\nGenerated from commit messages by `scripts/changelog.py`. "
        "Only features, fixes, performance work and refactors are listed.\n"
    ]
    if names:
        unreleased = render(entries(names[-1], "HEAD"))
        if unreleased:
            parts.append(f"## Unreleased\n\n{unreleased}\n")
    previous = None
    sections = []
    for name in names:
        date = git("log", "-1", "--format=%cs", name)
        body = notes(previous, name)
        sections.append(f"## {name[1:]} - {date}\n\n{body}\n")
        previous = name
    parts.extend(reversed(sections))
    return "\n".join(parts)


def main():
    args = sys.argv[1:]
    if args[:1] == ["--notes"]:
        start = args[1] if len(args) > 1 else (tags() or [None])[-1]
        end = args[2] if len(args) > 2 else "HEAD"
        print(notes(start, end))
    elif args:
        sys.exit(__doc__)
    else:
        OUTPUT.write_text(changelog())
        print(f"Wrote {OUTPUT.name} with {len(tags())} version(s)")


if __name__ == "__main__":
    main()
