package com.miqdigital.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.miqdigital.dto.NotificationDto;
import com.miqdigital.dto.BuildResultDto;

/**
 * This class is used to post Slack notification on provided channel.
 */
public class SlackUtils {

  private static final Logger logger = LoggerFactory.getLogger(SlackUtils.class);
  private static final String FILE_UPLOAD_URL = "https://slack.com/api/files.upload";
  private static final String CHAT_POST_URL = "https://slack.com/api/chat.postMessage";
  private static final String ATTACHMENT_FILE_NAME = "./target/FailedTestsInfo.txt";

  /**
   * Makes Http post request to Slack to send the notification and upload the failed scenarios file.
   *
   * @param notificationDto slack channel details and token
   * @param buildResultDto slack notification
   */
  public void sendSlackNotification(final NotificationDto notificationDto,
      final BuildResultDto buildResultDto) throws IOException {
    final HttpClient httpclient = HttpClientBuilder.create().disableContentCompression().build();
    final HttpPost httpPost = buildResultDto.failedTestCount == 0 ?
        new HttpPost(CHAT_POST_URL) :
        new HttpPost(FILE_UPLOAD_URL);

    final HttpEntity httpEntity = getHttpRequestEntity(notificationDto, buildResultDto).build();
    httpPost.setEntity(httpEntity);
    final HttpResponse execute = httpclient.execute(httpPost);
    logger.info(execute.getStatusLine().getReasonPhrase());
  }

  /**
   * Configures the Http request for Slack.
   *
   * @param notificationDto
   * @param buildResultDto
   * @return
   * @throws IOException
   */
  public MultipartEntityBuilder getHttpRequestEntity(final NotificationDto notificationDto,
      final BuildResultDto buildResultDto) throws IOException {
    final MultipartEntityBuilder reqEntity =
        MultipartEntityBuilder.create().addTextBody("token", notificationDto.getHeimdallBotToken());
    if (buildResultDto.failedTestCount == 0) {
      //soft slack notification
      final String generatedFileName = Files.write(Paths.get(ATTACHMENT_FILE_NAME),
          buildResultDto.failedTestDescription.toString().getBytes()).normalize().toAbsolutePath()
          .toString();
      reqEntity.addBinaryBody("file", new File(generatedFileName))
          .addTextBody("channels", notificationDto.getSlackChannel()).addTextBody("media", "file")
          .addTextBody("initial_comment", "<!here> " + buildResultDto.testExecutionInfo.toString());
    } else {
      //slack notification with @here
      final String generatedFileName = Files.write(Paths.get(ATTACHMENT_FILE_NAME),
          buildResultDto.failedTestDescription.toString().getBytes()).normalize().toAbsolutePath()
          .toString();
      reqEntity.addBinaryBody("file", new File(generatedFileName))
          .addTextBody("channels", notificationDto.getSlackChannel()).addTextBody("media", "file")
          .addTextBody("initial_comment", "<!here> " + buildResultDto.testExecutionInfo.toString());
    }
    return reqEntity;
  }
}