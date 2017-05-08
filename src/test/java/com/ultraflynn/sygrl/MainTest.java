package com.ultraflynn.sygrl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class MainTest {
    @Test
    public void shouldConvertAuthorizationResponse() throws Exception {
        String json = "{\"access_token\":\"ZypW9izIh2_MU1dXgzog1Y3SNUL1lP-2AI48tL2j9qu8frZaW4oQujmVyUSAIS1NRyGsrjhkNUNAqN_N3H6mYQ2\",\"token_type\":\"Bearer\",\"expires_in\":1200,\"refresh_token\":\"1TCITXtRxsO2Kf5xAHDiv-LFh3fqt-lGWDP0g_1y8pzZKvGvtiw-3_KXdp5gWF020\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        JsonNode access_token = jsonNode.get("access_type");
    }
}