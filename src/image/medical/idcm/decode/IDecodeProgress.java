package image.medical.idcm.decode;

import image.medical.idcm.cache.PageCache;

public interface IDecodeProgress {

    int decodeStart();

    void decoding(int progress);

    void decodeFinished(PageCache cache);

    void decodeFailed(DecodeError error);
}
