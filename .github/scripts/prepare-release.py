"""Prepare a dispatch release locally; the workflow pushes it after tests pass."""

import datetime
import os
from pathlib import Path
import re
import subprocess


def run(*args):
    return subprocess.check_output(args, text=True).strip()


def release_notes(changelog, version):
    match = re.search(
        rf"^## \[{re.escape(version)}\] - \d{{4}}-\d{{2}}-\d{{2}}\n(.*?)(?=^## |\Z)",
        changelog,
        re.MULTILINE | re.DOTALL,
    )
    return match.group(1).strip() if match else ""


def main():
    version = os.environ["RELEASE_VERSION"]
    if not re.fullmatch(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", version):
        raise SystemExit("Enter a release version such as 0.1.1, without a v prefix.")
    tag = f"v{version}"
    if run("git", "status", "--porcelain"):
        raise SystemExit("Release preparation requires a clean working tree.")

    if run("git", "tag", "--list", tag):
        run("git", "checkout", "--detach", tag)
        if run("kpm", "version") != version:
            raise SystemExit(f"{tag} does not match package.yml.")
        if not release_notes(Path("CHANGELOG.md").read_text(), version):
            raise SystemExit(f"{tag} has no release notes.")
        print(f"Reusing {tag} for publication.")
        return

    changelog_path = Path("CHANGELOG.md")
    changelog = changelog_path.read_text()
    if not release_notes(changelog, version):
        unreleased = re.search(
            r"^## \[Unreleased\]\n(.*?)(?=^## |\Z)",
            changelog,
            re.MULTILINE | re.DOTALL,
        )
        if not unreleased or not unreleased.group(1).strip():
            raise SystemExit("Add release notes under ## [Unreleased] before dispatching.")
        date = datetime.datetime.now(datetime.timezone.utc).date()
        heading = f"## [Unreleased]\n\n## [{version}] - {date}\n"
        changelog = changelog[:unreleased.start()] + unreleased.group(0).replace(
            "## [Unreleased]\n", heading, 1
        ) + changelog[unreleased.end():]

    readme_path = Path("README.md")
    readme, count = re.subn(
        r"(com\.composables:system-ui:)[^\s\"')]+",
        lambda match: match.group(1) + version,
        readme_path.read_text(),
    )
    if not count:
        raise SystemExit("Could not find the README dependency version.")

    # Include package metadata, release notes and installation docs in one commit.
    run("kpm", "version", version, "--no-git-tag-version", "--allow-same-version")
    changelog_path.write_text(changelog)
    readme_path.write_text(readme)
    run("git", "add", "package.yml", "CHANGELOG.md", "README.md")
    run("git", "commit", "--allow-empty", "-m", f"Release {version}")
    run("git", "tag", "-a", tag, "-m", f"Release {version}")
    print(f"Prepared {tag}; publication checks run before pushing.")


if __name__ == "__main__":
    main()
