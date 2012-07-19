package com.heroku;

import com.heroku.api.App;
import com.heroku.api.HerokuAPI;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.util.DirScanner;
import hudson.util.FormValidation;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Ryan Brainard
 */
public class IncrementalWorkspaceDeployment extends AbstractHerokuBuildStep {

    private static final String NULL_BUILDPACK = "https://github.com/ryandotsmith/null-buildpack.git";

    private final String globIncludes;
    private final String globExcludes;

    @DataBoundConstructor
    public IncrementalWorkspaceDeployment(String apiKey, String appName, String globIncludes, String globExcludes) {
        super(apiKey, appName);
        this.globIncludes = globIncludes;
        this.globExcludes = globExcludes;
    }

    // Overriding and delegating to parent because Jelly only looks at concrete class when rendering views
    @Override
    public String getAppName() {
        return super.getAppName();
    }

    // Overriding and delegating to parent because Jelly only looks at concrete class when rendering views
    @Override
    public String getApiKey() {
        return super.getApiKey();
    }

    public String getGlobIncludes() {
        return globIncludes;
    }

    public String getGlobExcludes() {
        return globExcludes;
    }

    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener, final HerokuAPI api, final App app) throws IOException, InterruptedException {
        return build.getWorkspace().act(new AnvilDeployer(
                listener,
                getEffectiveApiKey(),
                app,
                new DirScanner.Glob(globIncludes, globExcludes),
                NULL_BUILDPACK,
                new HashMap<String, String>()));
    }

    @Override
    public IncrementalWorkspaceDeploymentDescriptor getDescriptor() {
        return (IncrementalWorkspaceDeploymentDescriptor) super.getDescriptor();
    }

    @Extension
    public static class IncrementalWorkspaceDeploymentDescriptor extends AbstractHerokuBuildStepDescriptor {

        public String getDisplayName() {
            return "Heroku: Deploy Workspace (Incremental)";
        }

        public FormValidation doCheckGlobIncludes(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

        public FormValidation doCheckGlobExcludes(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException {
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }

    }
}
