package io.jenkins.plugins.collector.model;

public enum TriggerEnum {

  SCM_TRIGGER(null), MANUAL_TRIGGER(""), UNKNOWN(null);
  private String triggerBy;

  TriggerEnum(String triggerBy) {
    this.triggerBy = triggerBy;
  }

  public String getTriggerBy() {
    return triggerBy;
  }

  public void setTriggerBy(String triggerBy) {
    this.triggerBy = triggerBy;
  }
}
