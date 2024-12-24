package beans;

import java.io.Serializable;

import exceptions.BadParameterException;
import exceptions.MissingParametersException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestData implements Serializable {

    private Long x;
    private Double y;
    private Long r;

    public void throwIfBadData() throws MissingParametersException, BadParameterException {
        throwIfXYRParametersEmpty();
        checkYValue();
        checkXValue();
        checkRValue();
    }

    private void throwIfXYRParametersEmpty() throws MissingParametersException{

        String errorString = "Missing parameters:";
        boolean hasMissingParameter = false;

        if (x == null) {
            hasMissingParameter = true;
            errorString += " \"x\"";
        }

        if (y == null) {
            hasMissingParameter = true;
            errorString += " \"y\"";
        }

        if (r == null) {
            hasMissingParameter = true;
            errorString += " \"r\"";
        }

        if (hasMissingParameter) {
            throw new MissingParametersException(errorString);
        }
    }

    private long checkRValue() throws BadParameterException {
        if (r < 1 || r > 5) {
            throw new BadParameterException("R parameter must be in range [1,5]");
        }

        return r;
    }

    private double checkYValue() throws BadParameterException {
        if (y < -5.0D || y > 5.0D) {
            throw new BadParameterException("Y parameter must be in range [-5,3]");
        }

        return y;
    }

    private long checkXValue() throws BadParameterException {
        if (x <= -5L || x >= 3L) {
            throw new BadParameterException("X parameter must be in range [-5,3]");
        }

        return x;
    }
}
