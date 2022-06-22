# ec2-mgt

ec2 management - start and stop, accepts only start and stop command with a specified tag key (and tag value - optional)

# Credentials

Ensure aws credentials has been set

# Building 

Generate jar with dependencies

`./mvnw install package shade:shade`

# Running

`java -jar ec2-mgt.jar START | STOP ondemand -t true -r us-west-2`

`java -jar ec2-mgt.jar -h`

# Credits

@japzio

# Special Thanks To

External library authors see [pom.xml](pom.xml)
