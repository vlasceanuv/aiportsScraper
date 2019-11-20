import Dto.AirportDto;
import Firebase.FirebaseConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
class AirportImportTest {

    @Test
    void importAirports() throws IOException, ExecutionException, InterruptedException {
        Map<String, AirportDto> airports = new HashedMap<String, AirportDto>();
        FirebaseConfig firebaseConfig = new FirebaseConfig();
        firebaseConfig.connect();
        for (int i = 97; i < 123; i++) {
            final String s = Character.toString((char) i);
            try {
                int pageNumber = 1;
                boolean found = true;
                while (found) {
                    found = false;
                    final String endpoint = "https://www.world-airport-codes.com/search/?s=" + s + "&page=" + pageNumber;
                    System.out.println(endpoint);
                    Request.Builder requestBuilder = new Request.Builder()
                            .url(endpoint).get();
                    final Request request = requestBuilder.build();
                    try (Response response = new OkHttpClient().newCall(request).execute()) {
                        assertStatusCode(endpoint, request, response, 200);
                        if (response.body() != null) {
                            String html = Objects.requireNonNull(response.body()).string();
                            while (html.indexOf("<tr>") > 0) {
                                AirportDto airportDto = new AirportDto();
                                html = html.substring(html.indexOf("<tr>"));
                                String tag = html.substring(0, html.indexOf("</tr>") + 5);
                                html = html.substring(html.indexOf("</tr>") + 5);
                                try {
                                    Document doc = convertStringToXMLDocument(tag);
                                    airportDto.airportName = doc.getElementsByTagName("th").item(0).getFirstChild().getNextSibling().getTextContent().trim();
                                    final NodeList tds = doc.getElementsByTagName("td");

                                    for (int j = 0; j < tds.getLength(); j++) {
                                        final String textContent = tds.item(j).getTextContent();
                                        if (textContent.contains("City:")) {
                                            airportDto.city = textContent.substring(6);
                                        }
                                        if (textContent.contains("Country:")) {
                                            airportDto.country = textContent.substring(9);
                                        }
                                        if (textContent.contains("IATA:")) {
                                            airportDto.IATA = textContent.substring(6);
                                        }
                                        if (textContent.contains("ICAO:")) {
                                            airportDto.ICAO = textContent.substring(6);
                                        }
                                        if (textContent.contains("FAA:")) {
                                            airportDto.FAA = textContent.substring(5);
                                        }
                                    }
                                    if (airportDto.city != null && !airportDto.city.equals("") &&
                                            airportDto.IATA != null && !airportDto.IATA.equals("") &&
                                            airportDto.airportName != null && !airportDto.airportName.equals("")) {
                                        airports.put(airportDto.airportName, airportDto);
                                    }
                                    if(airports.values().size() % 500 ==0 ){
                                        System.out.println(airports.values().size());
                                    }
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                found = true;
                            }
                        }
                    }
                    pageNumber++;

                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        firebaseConfig.add(airports);


    }

    public static Document convertStringToXMLDocument(String xmlString) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void assertStatusCode(String endpoint, Request request, Response response, int expectedStatusCode) {
        int actualStatusCode = response.code();
        if (actualStatusCode != expectedStatusCode) {
            throw new RuntimeException("Status different than expected=" + expectedStatusCode + " actual=" + actualStatusCode + " when executing HTTP request to endpoint=" + endpoint);
        }
    }
}
