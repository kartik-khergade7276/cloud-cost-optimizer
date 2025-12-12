package org.aws.service;

import org.aws.entity.AwsCost;
import org.aws.entity.AwsCostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AwsCostService {

    @Autowired
    private AwsCostRepository repository;

    @Autowired
    private SnsAlertService snsAlertService;

    private final AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
            System.getenv("AWS_ACCESS_KEY_ID"),
            System.getenv("AWS_SECRET_ACCESS_KEY")
    );

    public void fetchDailyCosts() {
        try (CostExplorerClient costExplorerClient = CostExplorerClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(System.getenv("AWS_REGION")))
                .build()) {

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(7);

            GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                    .timePeriod(DateInterval.builder()
                            .start(start.toString())
                            .end(end.toString())
                            .build())
                    .granularity(Granularity.DAILY)
                    .metrics("UnblendedCost")
                    .groupBy(GroupDefinition.builder()
                            .type("DIMENSION")
                            .key("SERVICE")
                            .build())
                    .build();

            GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

            // ✅ collect ML input
            List<Map<String, Object>> costDataList = new ArrayList<>();

            for (ResultByTime result : response.resultsByTime()) {
                for (Group group : result.groups()) {
                    String service = group.keys().get(0);
                    MetricValue cost = group.metrics().get("UnblendedCost");

                    if (cost == null || cost.amount() == null) continue;

                    // Save to DB
                    AwsCost costEntity = new AwsCost();
                    costEntity.setDate(LocalDate.parse(result.timePeriod().start()));
                    costEntity.setAmount(new BigDecimal(cost.amount()));
                    costEntity.setCurrency(cost.unit());
                    costEntity.setService(service);
                    costEntity.setIsAnomaly(0);
                    repository.save(costEntity);

                    // Prepare ML input
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", result.timePeriod().start());
                    dayData.put("service", service);
                    dayData.put("amount", Double.parseDouble(cost.amount()));
                    costDataList.add(dayData);
                }
            }

            // Call anomaly detection
            if (!costDataList.isEmpty()) {
                sendDataToPython(costDataList);
            }
        }
    }

    public void sendDataToPython(List<Map<String, Object>> costData) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(costData, headers);

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        "http://python-ml:5000/detect",
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

        List<Map<String, Object>> anomalies = response.getBody();

        if (anomalies != null && !anomalies.isEmpty()) {
            for (Map<String, Object> anomaly : anomalies) {
                String date = (String) anomaly.get("date");
                String service = (String) anomaly.get("service");

                repository.updateIsAnomaly(LocalDate.parse(date), service);
            }

            snsAlertService.sendAlert(
                    "🚨 AWS Cost Anomaly Detected",
                    "Cloud cost spike detected! Details: " + anomalies
            );
        }

    }
}
