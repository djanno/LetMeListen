package letmelisten.unibo.studio.letmelisten.brondo_protocol;

import java.io.Serializable;

/**
 * Created by Federico on 14/04/2016.
 */
public class BrondoPlayMessage extends BrondoBaseMessage implements IBrondoPlayMessage, Serializable {

    private final int index;

    public BrondoPlayMessage(final String content, final int index) {
        super(content);
        this.index = index;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

}
