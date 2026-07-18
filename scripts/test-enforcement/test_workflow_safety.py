#!/usr/bin/env python3
"""Regression tests for protected-branch workflow safety."""

import re
import unittest
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
WORKFLOW_DIRECTORY = REPOSITORY_ROOT / ".github" / "workflows"


class WorkflowSafetyTest(unittest.TestCase):
    def workflow_sources(self) -> list[tuple[Path, str]]:
        sources: list[tuple[Path, str]] = []
        for pattern in ("*.yml", "*.yaml"):
            for path in WORKFLOW_DIRECTORY.glob(pattern):
                sources.append((path, path.read_text(encoding="utf-8")))
        return sources

    def test_workflows_never_request_admin_merge_bypass(self) -> None:
        offenders = [
            str(path.relative_to(REPOSITORY_ROOT))
            for path, source in self.workflow_sources()
            if re.search(r"\bgh\s+pr\s+merge\b[^\n]*\s--admin(?:\s|$)", source)
        ]
        self.assertEqual([], offenders, f"admin merge bypass found in: {offenders}")

    def test_workflows_never_allow_failed_checks_to_pass(self) -> None:
        offenders = [
            str(path.relative_to(REPOSITORY_ROOT))
            for path, source in self.workflow_sources()
            if re.search(
                r"allowed-conclusions\s*:\s*[^\n#]*\bfailure\b",
                source,
                flags=re.IGNORECASE,
            )
        ]
        self.assertEqual([], offenders, f"failed checks allowed in: {offenders}")


if __name__ == "__main__":
    unittest.main()
