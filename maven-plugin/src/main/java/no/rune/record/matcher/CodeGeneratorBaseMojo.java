package no.rune.record.matcher;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Set;

abstract class CodeGeneratorBaseMojo extends AbstractMojo {

    protected static final String PLUGIN_CONF_PROP_PREFIX = "recordmatcher.";

    /**
     * The directory where the generated code will be written to.
     */
    @Parameter(required = true,
            defaultValue = "${project.build.directory}/generated-test-sources/record-matchers",
            property = PLUGIN_CONF_PROP_PREFIX + "outputDirectory")
    private File outputDirectory;

    /**
     * Project packaging types where execution is skipped.
     */
    @Parameter(required = true,
            defaultValue = "pom",
            property = PLUGIN_CONF_PROP_PREFIX + "skipForPackaging")
    protected Set<String> skipForPackaging;


    @Parameter(required = true, readonly = true,
            defaultValue = "${project}")
    protected MavenProject mavenProject;



    OutputDirectory outputDirectory() {
        return new OutputDirectory(outputDirectory.toPath());
    }
}
