package no.rune.record.matcher;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.burningwave.core.assembler.ComponentSupplier;
import org.burningwave.core.classes.ClassCriteria;
import org.burningwave.core.classes.SearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElseGet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static no.rune.record.matcher.ScanHelper.isAccessibleFromSamePackage;
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
    @Parameter(property = PLUGIN_CONF_PROP_PREFIX + "includes")
    private Set<String> includes;


    /**
     * Specify which packages (includes any sub packages) to scan for records.
     */
    @Parameter(
            defaultValue = "${project.groupId}",
            property = PLUGIN_CONF_PROP_PREFIX + "scanPackages")
    private Set<String> scanPackages;


    /**
     * Set if scanning for records is enabled or not.
     */
    @Parameter(required = true,
            defaultValue = "true",
            property = PLUGIN_CONF_PROP_PREFIX + "scanEnabled")
    private boolean scanEnabled;


    /**
     * Specifies fully qualified class names of records to
     * exclude from the Matcher generator.
     */
    @Parameter(property = PLUGIN_CONF_PROP_PREFIX + "excludes")
    private Set<String> excludes;


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


    /**
     * Project packaging types where execution is skipped.
     */
    @Parameter(required = true,
            defaultValue = "pom",
            property = PLUGIN_CONF_PROP_PREFIX + "skipForPackaging")
    private Set<String> skipForPackaging;


    @Parameter(required = true, readonly = true,
            defaultValue = "${project}")
    private MavenProject mavenProject;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (requireNonNullElseGet(skipForPackaging, Set::of).contains(mavenProject.getPackaging())) {
            LOG.info("Skipping " + mavenProject.getPackaging() + " module");
            return;
        }

        Path outputDirectory = resolveOutputDirectory();
        try {
            Files.createDirectories(outputDirectory);
            LOG.info("Generating Hamcrest matchers in {}", outputDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Unable to create output directory " + outputDirectory + ", " +
                            "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        if (includeGeneratedCodeAsTestSources) {
            mavenProject.addTestCompileSourceRoot(outputDirectory.toString());
            LOG.debug("{} has been added as a compiler test sources directory", outputDirectory);
        }

        var generator = new RecordMatcherGenerator();
        var writtenFiles = resolveIncludedRecords()
            .map(generator::generateFromRecord)
            .map(compilationUnit -> {
                try {
                    return compilationUnit.writeToBaseDirectory(outputDirectory);
                } catch (IOException e) {
                    throw new UncheckedIOException(
                            "Unable to write " + compilationUnit + " to file, " +
                            "because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
                }
            })
            .sorted()
            .toList();


        if (writtenFiles.isEmpty()) {
            LOG.warn("No matchers were generated!");
        } else {
            LOG.info("Generated matchers:");
            for (var writtenFile : writtenFiles) {
                LOG.info("  {}", outputDirectory.relativize(writtenFile));
            }
            LOG.info("Total files written: {}", writtenFiles.size());
        }

    }

    private Path resolveOutputDirectory() {
        return outputDirectory.toPath();
    }


    private Stream<Class<? extends Record>> resolveIncludedRecords() {
        ClassLoader classLoader = buildProjectClassLoader(mavenProject, this.getClass().getClassLoader());

        Stream<Class<? extends Record>> scannedRecords;
        if (scanEnabled) {
            scannedRecords = scanForRecords(classLoader, scanPackages)
                    .filter(foundRecord -> {
                        var typeParams = foundRecord.getTypeParameters();
                        if (typeParams.length != 0) {
                            LOG.debug("Not including {}<{}> because type parameters are not supported",
                                    foundRecord.getName(), Stream.of(typeParams).map(t -> t.getName()).collect(joining(", ")));
                            return false;
                        }
                        return true;
                    });
        } else {
            LOG.info("Not scanning for records");
            scannedRecords = Stream.empty();
        }

        Stream<Class<? extends Record>> configuredRecords;
        if (includes != null && !includes.isEmpty()) {
            configuredRecords = includes.stream()
                    .filter(not(String::isBlank))
                    .map(String::trim)
                    .<Class<? extends Record>>map(recordClassName -> load(recordClassName, Record.class, classLoader))
                    .filter(configuredInclusion -> {
                        var typeParams = configuredInclusion.getTypeParameters();
                        if (typeParams.length != 0) {
                            throw new UnsupportedOperationException(
                                    "Can not include " + configuredInclusion.getName() +
                                    "<" + Stream.of(typeParams).map(t -> t.getName()).collect(joining(", ")) + "> " +
                                    "because type parameters are not supported");
                        }
                        return true;
                    });
        } else {
            if (!scanEnabled) {
                LOG.info("No specific record types configured for inclusion");
            }
            configuredRecords = Stream.empty();
        }

        var foundRecords = concat(scannedRecords, configuredRecords).distinct();
        if (excludes != null && !excludes.isEmpty()) {
            var santizedExcludes = excludes.stream().filter(not(String::isBlank)).map(String::trim).collect(toUnmodifiableSet());
            return foundRecords.filter(r -> {
                if (santizedExcludes.contains(r.getName())) {
                    LOG.info("Excluding record {}", r.getName());
                    return false;
                } else {
                    return true;
                }
            });
        } else {
            return foundRecords;
        }
    }

    private static Stream<Class<? extends Record>> scanForRecords(ClassLoader classLoader, Collection<String> packageNames) {
        packageNames = packageNames.stream().filter(not(String::isBlank)).map(String::trim).distinct().toList();
        if (packageNames.isEmpty()) {
            LOG.debug("No packages configured for scanning");
            return Stream.empty();
        }

        LOG.info("Scanning packages {} for records", packageNames);
        var allRecordsInClassLoader = SearchConfig.forResources(packageNames.stream().map(p -> p.replace('.', '/')).toList())
                .by(ClassCriteria.create().allThoseThatMatch(cls -> cls.isRecord() && isAccessibleFromSamePackage(cls)))
                .useAsParentClassLoader(classLoader);

        var classHunter = ComponentSupplier.getInstance().getClassHunter();
        try (var searchResult = classHunter.findBy(allRecordsInClassLoader)) {
            return searchResult.getClasses().stream().map(c -> c.asSubclass(Record.class));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "There was an error scanning for records in package(s) " + packageNames + ": " +
                    e.getClass().getSimpleName() + " '" + e.getMessage() + "'. Ensure that the packages " +
                    "are correctly defined and exists.", e);
        }
    }

    private static <C> Class<? extends C> load(String className, Class<C> target, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className).asSubclass(target);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Unable to resolve Class from " + className +
                    ", because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private static ClassLoader buildProjectClassLoader(MavenProject project, ClassLoader parent) {
        try {
            @SuppressWarnings("unchecked")
            List<String> classpathElements = project.getCompileClasspathElements();
            URL urls[] = new URL[classpathElements.size()];
            for ( int i = 0; i < classpathElements.size(); ++i )
            {
                urls[i] = new File(classpathElements.get( i ) ).toURI().toURL();
            }
            return new URLClassLoader(urls, parent);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to build classloader for resolving record classes, " +
                    ", because " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

}
