#!/usr/bin/env python3

"""
Calculate service Javadoc coverage from Java service files and Checkstyle output.

Expected inputs:
- src/main/java/**/*.java
- target/checkstyle-result.xml

GitHub Actions outputs:
- doc_percentage
- doc_color
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

from badge_utils import color_for_percentage, normalize_percentage


PROJECT_ROOT = Path(__file__).resolve().parents[2]
SRC_MAIN_JAVA = PROJECT_ROOT / "src" / "main" / "java"
CHECKSTYLE_XML = PROJECT_ROOT / "target" / "checkstyle-result.xml"


PUBLIC_ELEMENT_PATTERN = re.compile(
    r"\bpublic\s+(class|interface|enum|record|void|[A-Z][A-Za-z0-9_<>, ?]*)\b"
)


def write_github_output(key: str, value: str) -> None:
    github_output = os.environ.get("GITHUB_OUTPUT")

    if github_output:
        with Path(github_output).open("a", encoding="utf-8") as file:
            file.write(f"{key}={value}\n")
    else:
        print(f"{key}={value}")


def is_service_file(path: Path) -> bool:
    normalized_parts = {part.lower() for part in path.parts}

    return "service" in normalized_parts or path.name.endswith("Service.java")


def find_service_files() -> list[Path]:
    if not SRC_MAIN_JAVA.exists():
        print(f"Source directory not found: {SRC_MAIN_JAVA}", file=sys.stderr)
        return []

    return [
        path
        for path in SRC_MAIN_JAVA.rglob("*.java")
        if is_service_file(path)
    ]


def count_public_service_elements(service_files: list[Path]) -> int:
    total = 0

    for file_path in service_files:
        text = file_path.read_text(encoding="utf-8", errors="ignore")
        total += len(PUBLIC_ELEMENT_PATTERN.findall(text))

    return total


def count_missing_javadocs(checkstyle_xml: Path) -> int:
    if not checkstyle_xml.exists():
        print(f"Checkstyle report not found: {checkstyle_xml}", file=sys.stderr)
        return 0

    tree = ET.parse(checkstyle_xml)
    root = tree.getroot()

    missing_docs = 0

    for error in root.iter("error"):
        source = error.attrib.get("source", "")

        if "MissingJavadocMethod" in source or "JavadocVariable" in source:
            missing_docs += 1

    return missing_docs


def calculate_doc_coverage(total_elements: int, missing_docs: int) -> int:
    if total_elements == 0:
        return 100

    if missing_docs >= total_elements:
        return 0

    covered = total_elements - missing_docs
    return round((covered * 100) / total_elements)


def main() -> None:
    service_files = find_service_files()
    total_elements = count_public_service_elements(service_files)
    missing_docs = count_missing_javadocs(CHECKSTYLE_XML)

    percentage = normalize_percentage(
        calculate_doc_coverage(total_elements, missing_docs)
    )
    color = color_for_percentage(percentage)

    print(f"Service files: {len(service_files)}")
    print(f"Public service elements: {total_elements}")
    print(f"Missing Javadocs: {missing_docs}")
    print(f"Service Javadoc coverage: {percentage}%")
    print(f"Badge color: {color}")

    write_github_output("doc_percentage", str(percentage))
    write_github_output("doc_color", color)


if __name__ == "__main__":
    main()