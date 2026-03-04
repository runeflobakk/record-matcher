package no.rune.record.matcher;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;

@Mojo(
        name = PrepareOutputDirectoryMojo.GOAL_NAME,
        defaultPhase = GENERATE_TEST_SOURCES)
public class PrepareOutputDirectoryMojo extends CodeGeneratorBaseMojo {

    static final String GOAL_NAME = "prepare-output-directory";

    private static final Logger LOG = LoggerFactory.getLogger(PrepareOutputDirectoryMojo.class);

    /**
     * Whether the {@link #outputDirectory} should be included as a test sources root.
     */
    @Parameter(required = true,
            defaultValue = "true",
            property = PLUGIN_CONF_PROP_PREFIX + "includeAsTestSources")
    private boolean includeAsTestSources;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        var outputDirectory = outputDirectory().create();

        if (includeAsTestSources) {
            mavenProject.addTestCompileSourceRoot(outputDirectory.toString());
            LOG.info("Prepared output directory {}, and included as test sources", outputDirectory);
        } else {
            LOG.info("Prepared output directory {}, but not included as compilation source", outputDirectory);
        }

    }



}
