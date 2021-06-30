## In-memory pub-sub system

Unzip the contents and from the project's root directory (called `rootDir`), run `./gradlew clean build fatJar --refresh-dependencies` : This will build the jar and place it under
`rootDir/build/libs`

For running the code, from the `rootDir` run `java -jar build/libs/<filename>.jar`

The input `orders.json` file is under `src/main/resources` directory. If this file changes, please build the jar once again before running as the CSVs are loaded from
the classpath.

### Testing the code
There are tests for all major flows using groovy and spock testing framework.

Run `./gradlew test` from the `rootDir`.

### Notes
Design patterns used: Singleton, callback, pubsub

### Author
Nitin Mishra
geekymishnit@gmail.com
