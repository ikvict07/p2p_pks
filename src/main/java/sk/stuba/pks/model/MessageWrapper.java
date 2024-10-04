package sk.stuba.pks.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SimpleMessage.class, name = "simple"),
        @JsonSubTypes.Type(value = FileMessage.class, name = "file"),
        @JsonSubTypes.Type(value = SynMessage.class, name = "syn")
})
public class MessageWrapper {
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}