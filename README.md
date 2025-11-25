![Maven Central Version](https://img.shields.io/maven-central/v/com.github.runeflobakk/record-matcher-maven-plugin)


# Record Matcher Generator

This library and plugin for Maven generates source code for [Hamcrest Matchers](https://hamcrest.org/JavaHamcrest/) for [Java records](https://openjdk.org/jeps/395). The API of the generated matchers reflect the names of both the record itself as well as its components (i.e. fields), and provide facilities to incrementally constrain how specific you want to express what your expectations are.

This project is currently in its infancy, but should still be usable. You are most welcome to play around with it, and I appreciate any feedback you may have!


## Example

```java
record Book (String title, List<Author> authors, int pageCount, Publisher publisher) { }
```

Given you have defined the record above in your domain, this library can generate a `BookMatcher` which can be used like this:

```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static your.domain.BookMatcher.aBook();
...
Book effectiveJava = // resolve the Effective Java book
assertThat(effectiveJava, is(aBook().withTitle("Effective Java").withAuthors(not(empty()))));

List<Book> effectiveSeries = // resolve Effective Xyz series of books
assertThat(effectiveSeries, everyItem(aBook().withTitle(containsString("Effective"))));
```



## Getting started

Likely, you want to use this via the [Maven plugin](https://central.sonatype.com/artifact/com.github.runeflobakk/record-matcher-maven-plugin), and this is how the minimal configuration of the plugin looks like in your `pom.xml` file:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.runeflobakk</groupId>
			<artifactId>record-matcher-maven-plugin</artifactId>
			<version>0.3.0</version> <!-- replace with any newer version -->
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

No specific configuration of the plugin itself is strictly required, and the example above will **discover all records present in the package (including sub-packages) named the same as the [groupId](https://maven.apache.org/guides/getting-started/#:~:text=groupId%20is%20one%20of%20the%20key%20identifiers)** of your project, generate corresponding Matcher classes for them, and put them in `target/generated-test-sources/record-matchers` in the corresponding packages as each record they match on.

Using the `groupId` of your project as the base package for the plugin to discover records may or may not suit the project you are working with, so you can change this in a `<configuration>` section for the plugin:

```xml
<plugin>
	<groupId>com.github.runeflobakk</groupId>
	<artifactId>record-matcher-maven-plugin</artifactId>
	<version>0.3.0</version> <!-- replace with any newer version -->
	<configuration>
		<scanPackages>
		  com.base.service
		<scanPackages>
	</configuration>
	<executions>
		...
```

The configuration above will discover any records in `com.base.service` and sub-packages and put them in `target/generated-test-sources/record-matchers` in the corresponding packages as each record they match on.

You may separate the execution binding and configuration using `pluginManagement` as you see fit.

Try generating some Matchers using the command `mvn generate-test-sources`, or even `mvn record-matcher:generate` to only run the plugin on an already built project.


You can also **list several packages** separated by comma, if you want:
```xml
<scanPackages>
	com.base.pkg.first,
	com.base.pkg.another
<scanPackages>
```

You can also **disable the scanning**, and instead **list the specific records** you want to generate Matchers for:



```xml
<configuration>
	<scanEnabled>false</scanEnabled>
	<includes>
		<include>your.domain.SomeRecord</include>
		<include>your.domain.sub.SomeOtherRecord</include>
	</includes>
</configuration>
```


The configuration above will disable scanning, but you are free to leave scanning enabled, as both approaches can be used in tandem. This will generate the source code for specifically `SomeRecordMatcher` and `SomeOtherRecordMatcher` (substitute with your own record(s)), and their static factory methods `SomeRecordMatcher.aSomeRecord()` and `SomeOtherRecordMatcher.aSomeOtherRecord()` which should provide their APIs via method chaining; you get autocompletion by typing `.` after the static factory method.

The plugin will itself include the folder where it generates code as a test source root for the compiler used when building with Maven. If your IDE is able to resolve source folders automatically based on any `build-helper-maven-plugin` configuration, you may also want to include this:

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

After running a build, or `mvn generate-test-sources`, you should be able to see the generated Matcher classes in your IDE (a refresh of the project may be required for the IDE to see the new generated files). The example given above would make a `SomeRecordMatcher` and `SomeOtherRecordMatcher` available.

See the [Complete configuration reference](#complete-configuration-reference) for more ways to configure the plugin.



## Use cases

### Tests

This is the most obvious one, and what the project is really made for. Asserting with Hamcrest Matchers are done with the [assertThat(..)](https://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/MatcherAssert.html) method.

Suppose you have this `record` returned somewhere in your code:

```java
public record Person (UUID id, boolean isActive,
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



## Complete configuration reference

```xml
<configuration>
	<!-- default: true
		can be set to false -->
	<scanEnabled>true</scanEnabled>

	<!-- default: ${project.groupId} -->
	<scanPackages>
		com.my.pkg,
		com.my.other.pkg
	</scanPackages>

	<includes>
		com.external.SomeRecord,
		com.external.subpkg.SomeOtherRecord
	</includes>

	<excludes>
		com.my.pkg.aux.RecordToExclude,
		com.my.pkg.other.AnotherRecordToExclude,
	</excludes>

	<!-- default: pom
		Maven module packaging types where you want
		to skip executing the plugin -->
	<skipForPackaging>pom,ear</skipForPackaging>

	<!-- default: ${project.build.directory}/generated-test-sources/record-matchers -->
	<outputDirectory>${project.build.directory}/custom</outputDirectory>

	<!-- default: true
		If you for some reason need to prevent the generated code
		to be included as test sources in your project, set this to false -->
	<includeGeneratedCodeAsTestSources>true</includeGeneratedCodeAsTestSources>

</configuration>
```

The following command can also be used anywhere to view the plugin's own description of its configuration parameters:
```bash
mvn com.github.runeflobakk:record-matcher-maven-plugin::help -Dgoal=generate -Ddetail
```
Or the short form if you have already configured the plugin in your Maven project:
```bash
mvn record-matcher:help
```



## License

The project is licensed as open source under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
