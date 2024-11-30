package app;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpSender {
    private static final String BASE_URL = "http://data-store:8080";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    public static void sendData(String endpoint, Object data) {
        try {
            HttpPost request = new HttpPost(BASE_URL + endpoint);
            String jsonData = mapper.writeValueAsString(data);
            StringEntity entity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            httpClient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} {
    
}
