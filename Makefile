# This is the BUILD target, do not remove it, and do not modify it's name
.PHONY: java11-cli-build
java11-cli-build:
	mvn install -DtestFailureIgnore=true

# This is the RUN target, do not remove it, and do not modify it's name
.PHONY: java11-cli-run
java11-cli-run: ;

# This is the command run by the IDE's run button
.PHONY: lab-run
lab-run:
	mvn clean package -DtestFailureIgnore=true
	pgrep java | xargs -r kill -9
	java -jar airline-server/airlines-4.2.0-jar-with-dependencies.jar &
	java -jar target/flight-reservation-0.0.1-SNAPSHOT-jar-with-dependencies.jar &

# This is the command run by the IDE's test button
.PHONY: lab-test
lab-test:
	mvn -Dtest=$(subst src/test/java/,,$(basename $(FILE))) test
