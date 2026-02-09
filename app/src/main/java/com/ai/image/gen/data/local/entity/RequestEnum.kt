package com.ai.image.gen.data.local.entity

enum class RequestStatus {
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED
}

enum class RequestType {
    TEXT_TO_IMAGE,
    IMAGE_TO_IMAGE
}