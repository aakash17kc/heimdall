package com.miqdigital.slack.dto;

import lombok.Builder;

@Builder
public class SlackMessageInfo {
  public StringBuilder testExecutionInfo;
  public StringBuilder failedTestDescription;
  public long failedTestCount;
}