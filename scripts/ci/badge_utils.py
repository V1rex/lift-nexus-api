#!/usr/bin/env python3

"""
Shared helper functions for CI badge generation.

This file does not read project reports directly.
It only contains reusable logic used by other CI scripts.
"""


def color_for_percentage(value: int) -> str:
    """
    Return a shields.io badge color based on a percentage value.

    Args:
        value: Percentage between 0 and 100.

    Returns:
        A shields.io color name.
    """
    if value >= 85:
        return "brightgreen"
    if value >= 70:
        return "green"
    if value >= 50:
        return "yellow"
    return "red"


def normalize_percentage(value: int) -> int:
    """
    Keep a percentage value inside the 0-100 range.
    """
    return max(0, min(100, value))


if __name__ == "__main__":
    # Small manual test when running:
    # python scripts/ci/badge_utils.py

    examples = [95, 80, 65, 40, -5, 120]

    for raw_value in examples:
        value = normalize_percentage(raw_value)
        color = color_for_percentage(value)
        print(f"{raw_value} -> {value}% -> {color}")