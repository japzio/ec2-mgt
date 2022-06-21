package com.japzio.devops;


import lombok.extern.slf4j.*;
import picocli.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.*;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.utils.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Start and Stop Instances
 */
@Slf4j
@CommandLine.Command(
        name="on-off-ec2",
        mixinStandardHelpOptions = true,
        version = "on-off-ec2 0.1-dev",
        description = "Starts or stops ec2 instances based on specified tag"
)
public class App implements Callable<Integer> {

    @CommandLine.Parameters(index = "0" , description = "option start or stop")
    private Command command;

    @CommandLine.Parameters(index = "1" , description = "tag key")
    private String tagKey;

    @CommandLine.Option(names={"-v", "--tag-values"}, description = "Possible values of the tag key parameter provided, command separated.")
    private String tagValues;

    @CommandLine.Option(names={"-r", "--region"}, description = "Specific region.")
    private String awsRegion;

    private Region region = StringUtils.isBlank(awsRegion) ? Region.US_WEST_2 : Region.of(awsRegion);

    @Override
    public Integer call() throws Exception {

        log.debug("initializaing ec2 client...");

        Ec2Client ec2 = Ec2Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(region)
                .build();

        Filter tagFilters  = StringUtils.isBlank(tagValues) ? Filter.builder().name("tag:" + tagKey).build() : Filter.builder().name("tag:" + tagKey).values("true").build();

        CustomResponse customResponse = command.equals(Command.START) ? startInstances(ec2, instanceIds(ec2, tagFilters)) : stopInstances(ec2, instanceIds(ec2, tagFilters));

        return customResponse.responseCode == 200 ? 0 : customResponse.responseCode;
    }

    public static void main( String[] args ) {

        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);

    }

    public static CustomResponse startInstances(Ec2Client ec2Client, List<String> instanceIds) {

        log.debug("Stopping instances...");
        StartInstancesRequest request = StartInstancesRequest.builder().instanceIds(instanceIds).build();
        StartInstancesResponse response = ec2Client.startInstances(request);
        log.debug(response.toString());

        return new CustomResponse(response.sdkHttpResponse().isSuccessful(), response.sdkHttpResponse().statusCode());
    }

    public static CustomResponse stopInstances(Ec2Client ec2Client, List<String> instanceIds) {

        StopInstancesRequest request = StopInstancesRequest.builder().instanceIds(instanceIds).build();
        StopInstancesResponse response = ec2Client.stopInstances(request);
        log.debug(response.toString());

        return new CustomResponse(response.sdkHttpResponse().isSuccessful(), response.sdkHttpResponse().statusCode());
    }

    public static List<String> instanceIds(Ec2Client ec2Client, Filter filter) {

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
