package com.ultraflynn.sygrl.industry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Blueprint> getBlueprints() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://esi.tech.ccp.is/latest/characters/96239915/industry/jobs/?token=wR6g4EJ7QxJJLkhV9qWelvLfOBnN-oT6fCEzwQ8Asoq8DMKxe8z81EPTT8FfS9wnlJMBuIRiSOV18QXWu6jbaA2");
            CloseableHttpResponse response = client.execute(httpGet);
            return ImmutableList.of(Optional.ofNullable(response.getEntity())
                    .map(entity -> {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(entity.getContent());
                            logger.info("Response {}", jsonNode);
                            return new Blueprint("name", LocalDateTime.now());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new RuntimeException("Failed to obtain industry jobs")));
        } catch (IOException e) {
            logger.error("Error requesting jobs", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

/*
2017-05-15T20:43:46.636708+00:00 app[web.1]: 2017-05-15 20:43:46.625  INFO 4 --- [io-16656-exec-4] com.ultraflynn.sygrl.industry.Industry   : Response {"error":"Invalid 200 response: failed to coerce value '1022726117556' into type integer (format: int32), failed to coerce value '1022726117556' into type integer (format: int32), failed to coerce value '1022726117556' into type integer (format: int32)","response":[{"probability":1.0,"licensed_runs":100,"blueprint_location_flag_id":4,"cost":102988.0,"duration":1305600,"installer_id":96239915,"blueprint_type_id":2294,"successful_runs":10,"job_id":327367524,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":1022726117556,"start_date":"2017-04-27T19:06:13Z","blueprint_location_id":1022726117556,"product_type_id":2294,"activity_id":3,"solar_system_id":30003286,"end_date":"2017-05-14T03:46:43Z","blueprint_id":1021199433021,"completed_date":"2017-05-14T20:24:43Z","station_id":1022726117556,"facility_id":1022726117556,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":200,"blueprint_location_flag_id":4,"cost":14071.0,"duration":652800,"installer_id":96239915,"blueprint_type_id":785,"successful_runs":10,"job_id":327367515,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":1022726117556,"start_date":"2017-04-27T19:05:58Z","blueprint_location_id":1022726117556,"product_type_id":785,"activity_id":4,"solar_system_id":30003286,"end_date":"2017-05-06T14:26:29Z","blueprint_id":1023498028309,"completed_date":"2017-05-09T19:34:10Z","station_id":1022726117556,"facility_id":1022726117556,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":200,"blueprint_location_flag_id":4,"cost":47234.0,"duration":652800,"installer_id":96239915,"blueprint_type_id":1105,"successful_runs":10,"job_id":327367486,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":1022726117556,"start_date":"2017-04-27T19:05:41Z","blueprint_location_id":1022726117556,"product_type_id":1105,"activity_id":4,"solar_system_id":30003286,"end_date":"2017-05-06T14:26:12Z","blueprint_id":1023498025954,"completed_date":"2017-05-09T19:34:12Z","station_id":1022726117556,"facility_id":1022726117556,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":100,"blueprint_location_flag_id":4,"cost":691045.0,"duration":1280000,"installer_id":96239915,"blueprint_type_id":37860,"successful_runs":10,"job_id":325636526,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-04-09T20:36:30Z","blueprint_location_id":60013417,"product_type_id":37860,"activity_id":3,"solar_system_id":30003287,"end_date":"2017-04-24T16:09:50Z","blueprint_id":1023507636919,"completed_date":"2017-04-27T18:42:18Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":100,"blueprint_location_flag_id":4,"cost":346055.0,"duration":1280000,"installer_id":96239915,"blueprint_type_id":37856,"successful_runs":10,"job_id":325636518,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-04-09T20:36:19Z","blueprint_location_id":60013417,"product_type_id":37856,"activity_id":3,"solar_system_id":30003287,"end_date":"2017-04-24T16:09:39Z","blueprint_id":1023507653673,"completed_date":"2017-04-27T18:42:17Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":200,"blueprint_location_flag_id":4,"cost":253022.0,"duration":768000,"installer_id":96239915,"blueprint_type_id":4314,"successful_runs":10,"job_id":324142406,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-03-25T21:20:55Z","blueprint_location_id":60013417,"product_type_id":4314,"activity_id":3,"solar_system_id":30003287,"end_date":"2017-04-03T18:40:55Z","blueprint_id":1023507947053,"completed_date":"2017-04-09T20:35:18Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":100,"blueprint_location_flag_id":4,"cost":325095.0,"duration":1280000,"installer_id":96239915,"blueprint_type_id":37856,"successful_runs":10,"job_id":323111287,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-03-15T22:20:49Z","blueprint_location_id":60013417,"product_type_id":37856,"activity_id":4,"solar_system_id":30003287,"end_date":"2017-03-30T17:54:09Z","blueprint_id":1023507653673,"completed_date":"2017-04-09T20:35:18Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":100,"blueprint_location_flag_id":4,"cost":649094.0,"duration":1280000,"installer_id":96239915,"blueprint_type_id":37860,"successful_runs":10,"job_id":323111103,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-03-15T22:18:44Z","blueprint_location_id":60013417,"product_type_id":37860,"activity_id":4,"solar_system_id":30003287,"end_date":"2017-03-30T17:52:04Z","blueprint_id":1023507636919,"completed_date":"2017-04-09T20:35:17Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"},{"probability":1.0,"licensed_runs":200,"blueprint_location_flag_id":4,"cost":229342.0,"duration":768000,"installer_id":96239915,"blueprint_type_id":4314,"successful_runs":10,"job_id":323006275,"output_location_flag_id":4,"status_id":101,"blueprint_copy":false,"output_location_id":60013417,"start_date":"2017-03-14T22:02:27Z","blueprint_location_id":60013417,"product_type_id":4314,"activity_id":4,"solar_system_id":30003287,"end_date":"2017-03-23T19:22:27Z","blueprint_id":1023507947053,"completed_date":"2017-03-25T21:20:11Z","station_id":60013417,"facility_id":60013417,"runs":10,"completed_character_id":96239915,"status":"delivered"}]}
 */
