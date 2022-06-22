package com.japzio.devops.service;

import com.japzio.devops.model.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StartInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Ec2Mgt {

    public CustomResponse startInstances(Ec2Client ec2Client, List<String> instanceIds) {

        log.debug("Stopping instances...");
        StartInstancesRequest request = StartInstancesRequest.builder().instanceIds(instanceIds).build();
        StartInstancesResponse response = ec2Client.startInstances(request);
        log.debug(String.valueOf(response.sdkHttpResponse().statusCode()));

        return new CustomResponse(response.sdkHttpResponse().isSuccessful(), response.sdkHttpResponse().statusCode());
    }

    public CustomResponse stopInstances(Ec2Client ec2Client, List<String> instanceIds) {

        StopInstancesRequest request = StopInstancesRequest.builder().instanceIds(instanceIds).build();
        StopInstancesResponse response = ec2Client.stopInstances(request);
        log.debug(String.valueOf(response.sdkHttpResponse().statusCode()));

        return new CustomResponse(response.sdkHttpResponse().isSuccessful(), response.sdkHttpResponse().statusCode());
    }

    public List<String> instanceIds(Ec2Client ec2Client, Filter filter) {

        List<String> instanceIds = new ArrayList<>();
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().filters(filter).build();

        log.debug("Running describe instance...");
        DescribeInstancesResponse result = ec2Client.describeInstances(request);

        result.reservations().forEach(reservation -> {
            reservation.instances().forEach(instance -> {
                instanceIds.add(instance.instanceId());
                log.info(instance.instanceId());
            });
        });

        return instanceIds;
    }

}
