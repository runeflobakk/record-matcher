package no.rune.record;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

@Mojo(name = "generate", defaultPhase = GENERATE_TEST_SOURCES, requiresDependencyResolution = COMPILE)
public class GenerateRecordMatcherMojo extends AbstractMojo {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateRecordMatcherMojo.class);
    private static final String PLUGIN_CONF_PROP_PREFIX = "recordmatcher.";

    /**
     * Specifies the fully qualified class names of the records to
     * generate Hamcrest matchers for.
     */
    @Parameter
    private Set<String> includes;


    /**
     * The directory where the generated Hamcrest matchers will be written to.
     */
    @Parameter(required = true,
            defaultValue = "${project.build.directory}/generated-test-sources/record-matchers",
            property = PLUGIN_CONF_PROP_PREFIX + "outputDirectory")
    private File outputDirectory;


    /**
     * Whether the {@link #outputDirectory} should be included as a test sources root.
     */
    @Parameter(required = true,
            defaultValue = "true",
            property = PLUGIN_CONF_PROP_PREFIX + "includeGeneratedCodeAsTestSources")
    private boolean includeGeneratedCodeAsTestSources;


    @Parameter(required = true, readonly = true,
            defaultValue = "${project}")
    private MavenProject mavenProject;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (includes != null && !includes.isEmpty()) {
            Path outputDirectory = resolveOutputDirectory();
            if (includeGeneratedCodeAsTestSources) {
                mavenProject.addTestCompileSourceRoot(outputDirectory.toString());
                LOG.debug("{} has been added as a compiler test sources directory", outputDirectory);
            }
            try {
                Files.createDirectories(outputDirectory);
                LOG.info("Generating Hamcrest matchers in {}", outputDirectory);
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Unable to create output directory " + outputDirectory + ", " +
                        "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
            var generator = new RecordMatcherGenerator();
            resolveIncludedRecords().forEach(recordClass -> {
                var recordMatcherSourceCode = generator.generateFromRecord(recordClass);
                var matcherClassFileTargetDirectory = outputDirectory.resolve(Path.of(recordClass.getPackageName().replace('.', '/')));
                var matcherTargetFile = matcherClassFileTargetDirectory.resolve(recordClass.getSimpleName() + "Matcher.java");
                try {
                    Files.createDirectories(matcherClassFileTargetDirectory);
                    Files.writeString(matcherTargetFile, recordMatcherSourceCode);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to write to " + matcherTargetFile + ", " +
                            "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                }
                LOG.info("Generated matcher {}", outputDirectory.relativize(matcherTargetFile));
            });
        } else {
            LOG.warn("No records to generate Hamcrest matchers from!");
        }
    }

    private Path resolveOutputDirectory() {
        return outputDirectory.toPath();
    }


    private Stream<Class<? extends Record>> resolveIncludedRecords() {
        ClassLoader classLoader = buildProjectClassLoader(mavenProject, this.getClass().getClassLoader());
        return includes.stream()
                .filter(not(String::isBlank))
                .map(String::trim)
                .map(recordClassName -> {
                    try {
                        return classLoader.loadClass(recordClassName);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(
                                "Unable to resolve Class from " + recordClassName +
                                ", because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    }
                })
                .map(c -> c.asSubclass(Record.class));
    }


    private static ClassLoader buildProjectClassLoader(MavenProject project, ClassLoader parent) {
        try {
            @SuppressWarnings("unchecked")
            List<String> classpathElements = project.getCompileClasspathElements();
            URL urls[] = new URL[classpathElements.size()];
            for ( int i = 0; i < classpathElements.size(); ++i )
            {
                urls[i] = new File( classpathElements.get( i ) ).toURI().toURL();
            }
            return new URLClassLoader(urls, parent);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to build classloader for resolving record classes, " +
                    ", because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}
