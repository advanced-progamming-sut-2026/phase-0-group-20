package io.java.pvz.net.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkMessage {

    private MessageType type;
    private String requestId;
    private boolean success = true;
    private String errorMessage;
    private Map<String, Object> data;

    public NetworkMessage() {
        this.data = new HashMap<>();
    }

    @JsonCreator
    public NetworkMessage(@JsonProperty("type") MessageType type,
                          @JsonProperty("requestId") String requestId,
                          @JsonProperty("success") boolean success,
                          @JsonProperty("errorMessage") String errorMessage,
                          @JsonProperty("data") Map<String, Object> data) {
        this.type = type;
        this.requestId = requestId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = (data != null) ? data : new HashMap<>();
    }

    public static NetworkMessage request(MessageType type) {
        NetworkMessage msg = new NetworkMessage();
        msg.type = type;
        msg.requestId = UUID.randomUUID().toString();
        return msg;
    }

    public static NetworkMessage success(NetworkMessage requestMsg) {
        NetworkMessage response = new NetworkMessage();
        response.type = requestMsg.type;
        response.requestId = requestMsg.requestId;
        response.success = true;
        return response;
    }

    public static NetworkMessage failure(NetworkMessage requestMsg, String errorMessage) {
        NetworkMessage response = new NetworkMessage();
        response.type = requestMsg.type;
        response.requestId = requestMsg.requestId;
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }

    public NetworkMessage put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object v = data.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public Integer getInt(String key) {
        Object v = data.get(key);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    public Boolean getBoolean(String key) {
        Object v = data.get(key);
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = (data != null) ? data : new HashMap<>();
    }

    @Override
    public String toString() {
        return "NetworkMessage{type=" + type + ", requestId=" + requestId +
            ", success=" + success + ", errorMessage=" + errorMessage + ", data=" + data + "}";
    }
}
