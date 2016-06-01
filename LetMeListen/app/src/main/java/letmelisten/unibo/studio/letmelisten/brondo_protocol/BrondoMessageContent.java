package letmelisten.unibo.studio.letmelisten.brondo_protocol;

/**
 * Created by Federico on 21/04/2016.
 */
public enum BrondoMessageContent {

    DATA("DATA"),
    PLAY("PLAY"),
    DONE("DONE"),
    QUEUE("QUEU"),
    PAUSE("PAUS"),
    RESUME("RESU"),
    FAILED("FAIL"),
    HEARTBEAT("BEAT"),
    METADATA("METADATA");

    private final String content;

    private BrondoMessageContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
