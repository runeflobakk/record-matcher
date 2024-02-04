![Maven Central Version](https://img.shields.io/maven-central/v/com.github.runeflobakk/record-matcher-maven-plugin)


# Record Matcher Generator

A library and plugin for Maven to generate source code for [Hamcrest Matchers](https://hamcrest.org/JavaHamcrest/) for [Java records](https://openjdk.org/jeps/395). The generated matchers provide an API to incrementally constrain how specific you want to express what your expectations are in test. reflecting the names of both the record itself as well as its components (i.e. fields)

This project is currently in its infancy, but should still be usable. You are most welcome to play around with it, and I appreciate any feedback you may have!


## Getting started

Likely, you want to use this via the Maven plugin, and this is how you would set that up in your `pom.xml`:


```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.runeflobakk</groupId>
			<artifactId>record-matcher-maven-plugin</artifactId>
			<version>0.1.0</version> <!-- replace with any newer version -->
			<configuration>
				<includes>
					<!--
					For now, the records for which you want matchers generated needs to be enumerated.
					An automated discovery facility is planned for later.
					-->
					<include>your.domain.SomeRecord</include>
					<include>your.domain.sub.SomeOtherRecord</include>
				</includes>
			</configuration>
			<executions>
				<execution>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
		...
```
Or separate the execution binding and configuration by putting the configuration into `pluginManagement`, if you prefer.

The configuration above will generate the source code for `SomeRecordMatcher` and `SomeOtherRecordMatcher` and put them in `target/generated-test-sources/record-matchers` in the corresponding packages as each record they match on.

The plugin will itself include the folder where it generates code as a test source root for the compiler. If your IDE is able to resolve source folders automatically based on any `build-helper-maven-plugin` configuration, you may also want to include this:

```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>build-helper-maven-plugin</artifactId>
	<version>3.5.0</version>
	<executions>
		<execution>
			<id>include-record-matchers</id>
			<goals>
				<goal>add-test-source</goal>
			</goals>
			<configuration>
				<sources>
					<source>target/generated-test-sources/record-matchers</source>
				</sources>
			</configuration>
		</execution>
	</executions>
</plugin>
```

Eclipse detects this, and to my knowledge IntelliJ should also support this. Alternatively, you will need to manually add `target/generated-test-sources/record-matchers` as a test source folder for your project in your IDE.

After running a build, or `mvn generate-test-sources`, you should be able to see the generated Matcher classes in your IDE. The example given above would make a `SomeRecordMatcher` and `SomeOtherRecordMatcher` available (substitute with your own record(s)), and their static factory methods `SomeRecordMatcher.aSomeRecord()` and `SomeOtherRecordMatcher.aSomeOtherRecord()` which should provide their APIs via method chaining; you get autocompletion by typing `.` after the static factory method.



## Use cases

### Tests

This is the most obvious one, and what the project is really made for. Asserting with Hamcrest Matchers are done with the [assertThat(..)](https://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/MatcherAssert.html) method.

Suppose you have this `record` returned somewhere in your code:

```java
public record Person(UUID id, boolean isActive,
	String givenName, Optional<String> middleName, String surname,
	String primaryEmailAddress, List<String> allEmailAddresses) {}
```

And you need to test a case which concerns whether a person is active or not (whatever that may mean in your domain). With a Hamcrest Matcher generated by Record Matcher Generator, this can be expressed like this:

```java
import your.domain.Person;
import static your.domain.PersonMatcher.aPerson; //generated by record-matcher-generator
...

Person activePerson = //resolve the person expected to be active
assertThat(activePerson, aPerson().withIsActive(true));
```

Now, you may ask what is the point of all this fancyness, instead of just writing `assertEquals(true, activePerson.isActive())`, see the test go green, and be on with your day? In the latter case, the only value which is known by the assertion infrastucture is a boolean, and the best it can do in case of a test failure is to say that "expected true, but hey, it was false", which requires you to look at the code to know _anything_ about what actually failed. In the prior case, the object from your domain is known by the assertion infrastructure, so it has the ability to also provide more context for a test failure message:
- it was a `Person` which was not as expected
- the property `isActive` was expected to be `true`, but was in fact `false`.
- it may also include the whole state of the `Person` for context, which may in many cases provide clues about what happened, and you may not need to fork out a debugger to resolve where things have gotten mixed up. That is a lot more helpful than "expected true, but was false".



### Stubbing with Mockito

While you should keep your mocking code as simple as possible, there are cases where your stubs may need a bit of "smartness" to affect their behavior. Mockito allows to use Hamcrest Matchers to distinguish method invocations and how they respond, without being more specific than necessary.

See [MockitoHamcrest](https://www.javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/hamcrest/MockitoHamcrest.html).






## License

The project is licensed as open source under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
