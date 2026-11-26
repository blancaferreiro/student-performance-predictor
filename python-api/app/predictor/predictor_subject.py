
def calculate_predicted_grade(study_hours: float, difficulty: str = "MEDIUM") -> float:
    """Calculates a fictional predicted grade based on study hours and subject difficulty."""

    difficulty_factors = {
        "EASY": 1.2,
        "MEDIUM": 1.0,
        "HARD": 0.8
    }

    if difficulty not in difficulty_factors:
        raise ValueError("Difficulty must be EASY, MEDIUM or HARD")

    predicted_grade = study_hours * 1.5

    predicted_grade *= difficulty_factors[difficulty]

    return max(0.0, min(predicted_grade, 10.0))
