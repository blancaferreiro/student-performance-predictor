import csv
import io
import json

from .models import ReportRequest

CSV_COLUMNS = [
    "student",
    "name",
    "academic_year",
    "subject",
    "course",
    "record_academic_year",
    "score",
]


def build_json(report: ReportRequest) -> str:
    return json.dumps(report.model_dump(), ensure_ascii=False, indent=2)


def build_csv(report: ReportRequest) -> str:
    output = io.StringIO()
    writer = csv.DictWriter(output, fieldnames=CSV_COLUMNS)
    writer.writeheader()

    for record in report.records:
        writer.writerow({
            "student": report.student,
            "name": report.name,
            "academic_year": report.academic_year or "",
            "subject": record.subject,
            "course": record.course or "",
            "record_academic_year": record.academic_year or "",
            "score": "" if record.score is None else record.score,
        })

    return output.getvalue()
