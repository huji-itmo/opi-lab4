package encoders;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import exceptions.CantAcceptNaNsException;

public class SmartDouble extends TypeAdapter<Double> {

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        out.value(value);
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        String readString = in.nextString();
        return parseDouble(readString);
    }

    public static Double parseDouble(String input) {
        Double parsedDouble = Double.parseDouble(input);

        if (Double.isNaN(parsedDouble)) {
            throw new CantAcceptNaNsException("Program can't accept NaN value.");
        }

        String parsedString = Double.toString(parsedDouble);

        if (input.length() > parsedString.length()) {
            int i = parsedString.length();
            while (input.length() > i) {
                if (input.charAt(i) != '0') {
                    return parsedDouble += 0.00001D * Double.compare(parsedDouble, 0F);
                }
                i++;
            }
        }
        return parsedDouble;
    }
}
