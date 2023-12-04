package io.github.rvost.lemminx.dayz.model;

public record GlobalsValidationError(GlobalsValidationErrorCode code, String message) {
    public enum GlobalsValidationErrorCode {
        TYPE_MISMATCH,
        VALUE_OUT_OF_RANGE
    }
}
