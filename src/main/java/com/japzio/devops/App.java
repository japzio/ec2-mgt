package com.japzio.devops;

import com.japzio.devops.service.Ec2Mgt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Filter;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Start and Stop Instances
 */
@Slf4j
@CommandLine.Command(
        name="ec2-mgt",
        mixinStandardHelpOptions = true,
        version = "ec2-mgt 0.1-dev",
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

    private Ec2Mgt ec2Mgt = new Ec2Mgt();

    @Override
    public Integer call() throws Exception {

        log.debug("initializaing ec2 client...");

        Ec2Client ec2 = Ec2Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(region)
                .build();

        Filter tagFilters  = StringUtils.isBlank(tagValues) ? Filter.builder().name("tag:" + tagKey).build() : Filter.builder().name("tag:" + tagKey).values("true").build();

        List<String> instancesByTag = ec2Mgt.instanceIds(ec2, tagFilters);

        if (instancesByTag.isEmpty()) {
            log.debug("No instance found by tag key(and value) specified");
            return 404;
        }

        CustomResponse customResponse = command.equals(Command.START) ? ec2Mgt.startInstances(ec2, ec2Mgt.instanceIds(ec2, tagFilters)) : ec2Mgt.stopInstances(ec2, ec2Mgt.instanceIds(ec2, tagFilters));

        return customResponse.responseCode == 200 ? 0 : customResponse.responseCode;
    }

    public static void main( String[] args ) {

        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);

    }
}
