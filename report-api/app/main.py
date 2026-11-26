from fastapi import FastAPI

from .models import ReportFormat, ReportRequest, ReportResponse
from .report_builder import build_csv, build_json


app = FastAPI(title="Student Report Service")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/reports/{format}", response_model=ReportResponse)
def generate_report(format: ReportFormat, report: ReportRequest) -> ReportResponse:
    if format == "csv":
        content = build_csv(report)
        media_type = "text/csv; charset=utf-8"
    else:
        content = build_json(report)
        media_type = "application/json; charset=utf-8"

    safe_student = str(report.student).replace(" ", "_").replace("/", "_")

    return ReportResponse(
        filename=f"report_{safe_student}.{format}",
        media_type=media_type,
        content=content,
    )
