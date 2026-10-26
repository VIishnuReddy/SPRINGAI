package com.example.openAI.eavlutions;

public record EvaluationRequest( String question,
                                String answer,
                                String context,
                                String referenceAnswer) {

}
