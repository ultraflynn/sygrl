package com.ultraflynn.sygrl.industry;

import com.google.common.collect.ImmutableList;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Industry {
    private static final Logger logger = LoggerFactory.getLogger(Industry.class);

    public List<Blueprint> getBlueprints() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://esi.tech.ccp.is/latest/characters/96239915/industry/jobs/?token=knkSuVC4nh6_mDfReXME5WJRDu53fEcyXZmHfN0_KiJKJ6w5RfHhW_wz0yn7Se3Oic6Qcnu-WBLM-wfd8EAwUw2");
            CloseableHttpResponse response = client.execute(httpGet);
            return ImmutableList.of(Optional.ofNullable(response.getEntity())
                    .map(entity -> {
                        logger.info("entity {}", entity);
                        return new Blueprint("name", LocalDateTime.now());
                    })
                    .orElseThrow(() -> new RuntimeException("Failed to obtain industry jobs")));
        } catch (IOException e) {
            logger.error("Error requesting jobs", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
