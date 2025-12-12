package org.aws.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.regions.Region;

@Service
public class SnsAlertService {

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsAlertService(@Value("${aws.region}") String region,
                           @Value("${aws.sns.topicArn}") String topicArn) {
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .build();
        this.topicArn = topicArn;
    }

    public void sendAlert(String subject, String message) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build();

        snsClient.publish(request);
    }
}
