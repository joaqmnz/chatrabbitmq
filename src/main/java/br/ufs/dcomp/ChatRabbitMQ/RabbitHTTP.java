package br.ufs.dcomp.ChatRabbitMQ;

import java.net.URL;
import java.util.Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import com.fasterxml.jackson.databind.*;


public class RabbitHTTP {
    private String urlAPI;
    private String user;
    private String password;
    private String credential;

    public RabbitHTTP(String user, String password)
    {
        this.urlAPI = "http://rabbitmqbalancer-fcd77e6581d8de95.elb.us-east-1.amazonaws.com/api/exchanges/teste/";
        this.user = user;
        this.password = password;
        this.credential = Base64.getEncoder().encodeToString((this.user + ":" + this.password).getBytes());
    }

    private JsonNode requestAPI(String exchange) throws Exception
    {
        String api = this.urlAPI + exchange + "/bindings/source";

        URL url = new URL(api);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + this.credential);

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();

        String linha;

        while((linha = reader.readLine()) != null) response.append(linha);

        reader.close();

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(response.toString());
    }

    public JsonNode responseAPI(String exchange) throws Exception
    {
        return requestAPI(exchange);
    }
}
