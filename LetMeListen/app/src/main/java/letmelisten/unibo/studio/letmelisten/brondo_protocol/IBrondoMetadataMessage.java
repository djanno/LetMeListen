package letmelisten.unibo.studio.letmelisten.brondo_protocol;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 14/04/2016.
 */
public interface IBrondoMetadataMessage extends IBrondoBaseMessage {

    int getDataSize();

    ITrack getDataInfo();

}
