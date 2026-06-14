package io.github.matiasmazzu.transactionservice.domain.exception;

public class ParentNotFoundException extends RuntimeException {

    public ParentNotFoundException(String message) {
        super(message);
    }
}
