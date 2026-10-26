package com.example.openAI.eavlutions;

public record EvaluationResult(double answerRelevance,
                               double faithfulness,
                               Double correctness,
                               String explanation) {
}
