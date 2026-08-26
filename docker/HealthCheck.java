import java.net.HttpURLConnection;
import java.net.URI;

public final class HealthCheck {
    private HealthCheck() {
    }

    public static void main(String[] args) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI
                .create("http://127.0.0.1:8080/actuator/health/readiness")
                .toURL().openConnection();
        connection.setConnectTimeout(2_000);
        connection.setReadTimeout(2_000);
        connection.setRequestMethod("GET");
        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Application is not ready");
        }
    }
}

