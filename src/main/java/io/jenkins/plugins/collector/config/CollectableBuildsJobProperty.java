package io.jenkins.plugins.collector.config;

import hudson.Extension;
import jenkins.model.OptionalJobProperty;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class CollectableBuildsJobProperty extends OptionalJobProperty<WorkflowJob> {

  @DataBoundConstructor
  public CollectableBuildsJobProperty() {
  }

  @Extension
  @Symbol("opalCollector")
  public static class DescriptorImpl extends OptionalJobPropertyDescriptor {

    @Override
    public String getDisplayName() {
      return "Collectable for Opal";
    }
  }
}
