package com.japzio.devops;


import lombok.extern.slf4j.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.*;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;

/**
 * Start and Stop Instances
 */
@Slf4j
public class App {

    public static void main( String[] args ) {

        log.debug("initializaing ec2 client...");

        Ec2Client ec2 = Ec2Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_WEST_2)
                .build();

        Filter tagFilters = Filter.builder().name("tag:ondemand").values("true").build();

        startInstances(ec2, instanceIds(ec2, tagFilters));

    }

    public static boolean startInstances(Ec2Client ec2Client, List<String> instanceIds) {

        log.debug("Stopping instances...");
        StartInstancesRequest request = StartInstancesRequest.builder().instanceIds(instanceIds).build();
        StartInstancesResponse response = ec2Client.startInstances(request);
        log.debug(response.toString());

        return response.sdkHttpResponse().isSuccessful();
    }

    public static boolean stopInstances(Ec2Client ec2Client, List<String> instanceIds) {

        StopInstancesRequest request = StopInstancesRequest.builder().instanceIds(instanceIds).build();
        StopInstancesResponse response = ec2Client.stopInstances(request);
        log.debug(response.toString());

        return response.sdkHttpResponse().isSuccessful();
    }

    public static List<String> instanceIds(Ec2Client ec2Client, Filter filter) {

        List<String> instanceIds = new ArrayList<>();
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().filters(filter).build();

        log.debug("Running describe instance...");
        DescribeInstancesResponse result = ec2Client.describeInstances(request);

        result.reservations().stream().forEach(reservation -> {
            reservation.instances().stream().forEach(instance -> {
                instanceIds.add(instance.instanceId());
                log.info(instance.instanceId());
            });
        });

        return instanceIds;
    }
}
