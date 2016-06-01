package letmelisten.unibo.studio.letmelisten.brondo_protocol;


import java.io.Serializable;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 14/04/2016.
 */
public class BrondoMetadataMessage extends BrondoBaseMessage implements IBrondoMetadataMessage, Serializable {

    private final int dataSize;
    private final ITrack dataInfo;

    public BrondoMetadataMessage(final ITrack dataInfo) {
        super(BrondoMessageContent.METADATA.getContent());
        this.dataSize = (int) dataInfo.getMediaFile().length();
        this.dataInfo = dataInfo;
    }


    @Override
    public int getDataSize() {
        return this.dataSize;
    }

    @Override
    public ITrack getDataInfo() {
        return this.dataInfo;
    }

}
