package letmelisten.unibo.studio.letmelisten.brondo_protocol;

/**
 * Created by Federico on 11/05/2016.
 */
public class BrondoDataMessage extends BrondoBaseMessage implements IBrondoDataMessage {

    private final int bytes;

    public BrondoDataMessage(final int bytes) {
        super(BrondoMessageContent.DATA.getContent());
        this.bytes = bytes;
    }

    @Override
    public int getBytes() {
        return this.bytes;
    }

}
