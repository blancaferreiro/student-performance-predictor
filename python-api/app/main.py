from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

from .predictor import calculate_predicted_grade


app = FastAPI(title="Student Prediction Service")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)



from typing import Literal

class PredictionRequest(BaseModel):
    study_hours: float = Field(ge=0)
    difficulty: Literal["EASY", "MEDIUM", "HARD"] = "MEDIUM"



class PredictionResponse(BaseModel):
    predicted_grade: float


@app.post("/predict", response_model=PredictionResponse)
def predict_grade(request: PredictionRequest) -> PredictionResponse:
    """Expone la fórmula ficticia mediante una operación HTTP sencilla."""
    predicted_grade = calculate_predicted_grade(
    request.study_hours,
    request.difficulty
    )
    return PredictionResponse(predicted_grade=round(predicted_grade, 2))
