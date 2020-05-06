package io.jenkins.plugins.collector.rest;

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.ModelObjectWithContextMenu;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
public class OpalDashboard implements RootAction, ModelObjectWithContextMenu {
    public String getIconFileName() {
        return "/plugin/build-metrics-collector-plugin/images/opal.png";
    }

    public String getDisplayName() {
        return "Opal";
    }

    public String getUrlName() {
        return "opal";
    }

    public Object getDynamic(String name) {
        return null;
    }


    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) {
        return new ContextMenu();
    }
}
