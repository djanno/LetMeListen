package letmelisten.unibo.studio.letmelisten.brondo_protocol;

import java.io.Serializable;

/**
 * Created by Federico on 14/04/2016.
 */
public class BrondoBaseMessage implements IBrondoBaseMessage, Serializable {

    private final String content;

    public BrondoBaseMessage(final String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

}

