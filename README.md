## VAB Directory
	http://localhost:8081/oven
	http://localhost:8081/ovenController

## AAS Registory
	http://localhost:8080/handson/registry/api/v1/registry/

## Running the oven example
After successful run of the command `mvn clean install`, you can try the examples in the code by running the following Java classes in the specified order:
1. [VABDirectoryStarter.java](https://github.com/objective-partner/basyx-distributed-example/blob/master/directory-vab/src/main/java/basyx/distributed/directory_vab/VABDirectoryStarter.java)
2. [OCCStarter.java](https://github.com/objective-partner/basyx-distributed-example/blob/master/oven-control/src/main/java/basyx/distributed/oven_control/OCCStarter.java)
3. [OvenStarter.java](https://github.com/objective-partner/basyx-distributed-example/blob/master/oven/src/main/java/basyx/distributed/oven/OvenStarter.java)
4. [AasStarter.java](https://github.com/objective-partner/basyx-distributed-example/blob/master/oven-aas/src/main/java/basyx/distributed/oven_aas/AasStarter.java)
5. [AASRunner.java](https://github.com/objective-partner/basyx-distributed-example/blob/master/oven-aas/src/test/java/basyx/distributed/oven_aas/AASRunner.java)
