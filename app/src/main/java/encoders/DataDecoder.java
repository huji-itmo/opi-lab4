package encoders;


import java.io.Reader;

import beans.RequestData;
import exceptions.RequestBodyIsEmpty;

public interface DataDecoder {
    RequestData getDecodedRequestData(String contentType, Reader reader) throws RequestBodyIsEmpty;
}
