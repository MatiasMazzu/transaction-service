package io.github.matiasmazzu.transactionservice.adapter.in.web.dto;

public record StatusResponse(String status) {

    public static StatusResponse ok() {
        return new StatusResponse("ok");
    }
}
