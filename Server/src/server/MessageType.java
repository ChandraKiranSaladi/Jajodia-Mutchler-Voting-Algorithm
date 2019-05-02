package server;

public enum MessageType {
    VOTE_REQUEST,
    VOTE_RESPONSE,
    ABORT,
    COMMIT,
    REQUEST,
    PARTITION,
    COMPLETION,
    CURRENT_COPY_REQUEST,
}