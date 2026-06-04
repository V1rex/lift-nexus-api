#!/usr/bin/env python3

"""
Parse JaCoCo CSV coverage and expose values for GitHub Actions.

Expected input:
- target/site/jacoco/jacoco.csv

GitHub Actions outputs:
- test_percentage
- test_color
"""

import csv
import os
import sys
from pathlib import Path

from badge_utils import color_for_percentage, normalize_percentage


PROJECT_ROOT = Path(__file__).resolve().parents[2]
JACOCO_CSV = PROJECT_ROOT / "target" / "site" / "jacoco" / "jacoco.csv"


def write_github_output(key: str, value: str) -> None:
    github_output = os.environ.get("GITHUB_OUTPUT")

    if github_output:
        with Path(github_output).open("a", encoding="utf-8") as file:
            file.write(f"{key}={value}\n")
    else:
        print(f"{key}={value}")


def calculate_line_coverage(jacoco_csv: Path) -> int:
    if not jacoco_csv.exists():
        print(f"JaCoCo CSV report not found: {jacoco_csv}", file=sys.stderr)
        return 0

    line_missed = 0
    line_covered = 0

    with jacoco_csv.open("r", encoding="utf-8", newline="") as file:
        reader = csv.DictReader(file)

        required_columns = {"LINE_MISSED", "LINE_COVERED"}
        missing_columns = required_columns - set(reader.fieldnames or [])

        if missing_columns:
            print(
                f"JaCoCo CSV is missing columns: {', '.join(sorted(missing_columns))}",
                file=sys.stderr,
            )
            return 0

        for row in reader:
            line_missed += int(row["LINE_MISSED"])
            line_covered += int(row["LINE_COVERED"])

    total = line_missed + line_covered

    if total == 0:
        return 0

    return round((line_covered * 100) / total)


def main() -> None:
    percentage = normalize_percentage(calculate_line_coverage(JACOCO_CSV))
    color = color_for_percentage(percentage)

    print(f"JaCoCo line coverage: {percentage}%")
    print(f"Badge color: {color}")

    write_github_output("test_percentage", str(percentage))
    write_github_output("test_color", color)


if __name__ == "__main__":
    main()