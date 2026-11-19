import json
import unittest

from app.models import ReportRecord, ReportRequest
from app.report_builder import build_csv, build_json


def sample_report() -> ReportRequest:
    return ReportRequest(
        student=1,
        name="Ana",
        academic_year="2025-2026",
        records=[
            ReportRecord(
                subject="Algoritmos",
                course="1º",
                academic_year="2025-2026",
                score=8.5,
            ),
            ReportRecord(
                subject="Bases de datos",
                course="1º",
                academic_year="2025-2026",
                score=None,
            ),
        ],
    )


class ReportBuilderTests(unittest.TestCase):
    def test_csv_contains_header_and_records(self):
        csv_content = build_csv(sample_report())
        lines = csv_content.strip().splitlines()

        self.assertEqual(len(lines), 3)
        self.assertIn("student,name,academic_year,subject", lines[0])
        self.assertIn("Algoritmos", lines[1])
        self.assertIn("8.5", lines[1])
        self.assertIn("Bases de datos", lines[2])

    def test_json_contains_academic_and_historical_data(self):
        data = json.loads(build_json(sample_report()))

        self.assertEqual(data["student"], 1)
        self.assertEqual(data["name"], "Ana")
        self.assertEqual(len(data["records"]), 2)
        self.assertEqual(data["records"][0]["score"], 8.5)
        self.assertIsNone(data["records"][1]["score"])


if __name__ == "__main__":
    unittest.main()
