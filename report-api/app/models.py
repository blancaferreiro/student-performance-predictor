from typing import Literal

from pydantic import BaseModel, Field


class ReportRecord(BaseModel):
    subject: str
    course: str | None = None
    academic_year: str | None = None
    score: float | None = Field(default=None, ge=0, le=10)


class ReportRequest(BaseModel):
    student: int | str
    name: str
    academic_year: str | None = None
    records: list[ReportRecord]


class ReportResponse(BaseModel):
    filename: str
    media_type: str
    content: str


ReportFormat = Literal["csv", "json"]
