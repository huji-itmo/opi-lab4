package encoders;


import java.util.*;
import java.util.stream.*;

import beans.HitResult;

import java.util.concurrent.atomic.*;



public class JsonEncoder implements DataEncoder{

    public String getEncodedHitResult(String acceptFormat, HitResult data) {
        String accept = Objects.requireNonNullElse(acceptFormat, "application/json") ;

        String responseString = "";

        switch (accept) {

            case "text/html":
                responseString = data.toHTMLTable();
                break;
            default:
                responseString = "{" + data.toJsonFields() + "}";
                break;
        }

        return responseString;
    }

    public String getEncodedHitTable(String acceptFormat, Stream<HitResult> hitHistory) {

        switch (acceptFormat) {
            case "text/html":
                String html = hitHistory
                .map(data -> data.toHTMLTable())
                .collect(Collectors.joining("\n"));

                return html;
            default:
                AtomicInteger counter = new AtomicInteger(0);

                String json = hitHistory
                    .map(data -> "\"%d\": {%s}\n".formatted(counter.getAndIncrement(), data.toJsonFields()))
                    .collect(Collectors.joining(", ", "{", "}"));

                return json;
        }
    }
}
