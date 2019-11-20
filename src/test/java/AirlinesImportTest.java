import Dto.AirlinesDTO;
import Firebase.FirebaseConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
public class AirlinesImportTest {

    @Test
    void importAirlines() throws IOException, ExecutionException, InterruptedException {

        FirebaseConfig firebaseConfig = new FirebaseConfig();
        firebaseConfig.connect();
        for (int i = 97; i < 123; i++) {
            final String s = Character.toString((char) i);
            final String endpoint = "https://www.wego.com/airlines/" + s;
            System.out.println(endpoint);
            Request.Builder requestBuilder = new Request.Builder()
                    .url(endpoint).get();
            final Request request = requestBuilder.build();
            boolean retry = true;
            while (retry) {
                retry = false;
                try (Response response = new OkHttpClient().newCall(request).execute()) {
                    Map<String, AirlinesDTO> airlines = new HashedMap<String, AirlinesDTO>();
                    assertStatusCode(endpoint, request, response, 200);
                    if (response.body() != null) {
                        String html = Objects.requireNonNull(response.body()).string();
                        while (html.indexOf("div class=\"airline\"") > 0) {
                            AirlinesDTO airlinesDTO = new AirlinesDTO();
                            html = html.substring(html.indexOf("<div class=\"airline\""));
                            String tag = html.substring(0, html.indexOf("</div>") + 6);
                            html = html.substring(html.indexOf("</div>") + 5);
                            try {
                                airlinesDTO.name = html.substring(html.indexOf("<span>") + 6, html.indexOf("<span ")).trim();
                                airlinesDTO.code = html.substring(html.indexOf("\"code\">") + 7, html.indexOf("</span>")).trim().replace("&rlm;", "");
                                if (airlinesDTO.code.length() < 9) {
                                    airlines.put(airlinesDTO.code, airlinesDTO);
                                } else {
                                    System.out.println(airlinesDTO.code);
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                    firebaseConfig.addAirlines(airlines);
                }
                catch (Exception e){
                    retry = true;
                    System.out.println("Retry for "+request);
                }
            }
        }

    }

    public static void assertStatusCode(String endpoint, Request request, Response response, int expectedStatusCode) {
        int actualStatusCode = response.code();
        if (actualStatusCode != expectedStatusCode) {
            throw new RuntimeException("Status different than expected=" + expectedStatusCode + " actual=" + actualStatusCode + " when executing HTTP request to endpoint=" + endpoint);
        }
    }
}
