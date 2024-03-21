/*
 * Copyright 2021 Michelin CERT (https://cert.michelin.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michelin.cert.redscan.utils.mantisbt;

import biz.futureware.mantisconnect.CustomFieldValueForIssueData;
import biz.futureware.mantisconnect.FilterCustomField;
import biz.futureware.mantisconnect.FilterSearchData;
import biz.futureware.mantisconnect.IssueData;
import biz.futureware.mantisconnect.IssueNoteData;
import biz.futureware.mantisconnect.MantisConnectLocator;
import biz.futureware.mantisconnect.MantisConnectPortType;
import biz.futureware.mantisconnect.ObjectRef;
import biz.futureware.mantisconnect.TagData;

import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.logging.log4j.LogManager;

/**
 * MantisBT client.
 *
 * @author Maxime ESCOURBIAC
 */
public class MantisBtClient {

  private static String url;
  private static String username;
  private static String password;


  private static MantisConnectLocator mantisConnectLocator;
  private static MantisConnectPortType mantisConnectPort;

  private static final BigInteger REDSCAN_PROJECT_ID = BigInteger.ONE;
  private static final BigInteger INTERNAL_ID_CUSTOMFIELD_ID = BigInteger.ONE;
  private static final BigInteger URL_CUSTOMFIELD_ID = BigInteger.valueOf(2);
  private static final BigInteger ORIGIN_CUSTOMFIELD_ID = BigInteger.valueOf(3);

  private static final BigInteger SEVERITY_CRITICAL = BigInteger.valueOf(Severity.CRITICAL);
  private static final BigInteger SEVERITY_HIGH = BigInteger.valueOf(Severity.HIGH);
  private static final BigInteger SEVERITY_MEDIUM = BigInteger.valueOf(Severity.MEDIUM);
  private static final BigInteger SEVERITY_LOW = BigInteger.valueOf(Severity.LOW);
  private static final BigInteger SEVERITY_INFO = BigInteger.valueOf(Severity.INFO);

  private static final BigInteger PRIORITY_P1 = BigInteger.valueOf(1);
  private static final BigInteger PRIORITY_P2 = BigInteger.valueOf(2);
  private static final BigInteger PRIORITY_P3 = BigInteger.valueOf(3);
  private static final BigInteger PRIORITY_P4 = BigInteger.valueOf(4);
  private static final BigInteger PRIORITY_P5 = BigInteger.valueOf(5);

  private static final BigInteger RESOLUTION_REOPEN = BigInteger.valueOf(30);
  private static final BigInteger RESOLUTION_WONTFIX = BigInteger.valueOf(90);

  private static final BigInteger STATUS_NEW = BigInteger.valueOf(10);
  private static final BigInteger STATUS_TOVERIFY = BigInteger.valueOf(70);
  private static final BigInteger STATUS_CLOSED = BigInteger.valueOf(90);

  private static final String DEFAULT_CATEGORY = "General";
  private static final String REDSCAN_PROJECT_NAME = "Redscan";

  /**
   * Init the MantisBT client.
   *
   * @param url MantisBT url (ex: http://10.124.2.25:9200 )
   * @param username MantisBT Search user.
   * @param password MantisBT Search password.
   */
  public static void init(String url, String username, String password) {
    MantisBtClient.url = url;
    MantisBtClient.username = username;
    MantisBtClient.password = password;
  }
  
  /**
   * Get MantisBT version.
   *
   * @return MantisBT version.
   */
  public String getMantisVersion() {
    String version = "";
    try {
      version = getClient().mc_version();
    } catch (RemoteException ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("RemoteException : %s", ex.getMessage()));
    }
    return version;
  }
  
  /**
   * Get issue by MantisBT id.
   *
   * @param id MantisBT id.
   * @return MantisBT issue.
   */
  public IssueData getIssue(BigInteger id) {
    LogManager.getLogger(MantisBtClient.class).info(String.format("Get issue id : %s", id.toString()));
    IssueData issueData = null;
    try {
      issueData = getClient().mc_issue_get(username, password, id);
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return issueData;
  }

  /**
   * Get issue by Internal id. In normal case, only one issue should be found.
   *
   * @param id Internal id
   * @return MantisBT issues.
   */
  public IssueData[] getIssuesByInternalId(String id) {
    LogManager.getLogger(MantisBtClient.class).info(String.format("Get issue with internal id : %s", id));
    IssueData[] issueDatas = null;
    try {
      FilterSearchData filter = new FilterSearchData();
      filter.setCustom_fields(new FilterCustomField[]{new FilterCustomField(new ObjectRef(INTERNAL_ID_CUSTOMFIELD_ID, "InternalID"), new String[]{id})});
      issueDatas = getClient().mc_filter_search_issues(username, password, filter, BigInteger.valueOf(1), BigInteger.valueOf(-1));
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return issueDatas;
  }

  /**
   * Delete MantisBT issue.
   *
   * @param id MantisBT id.
   * @return True if the deletion is successful.
   */
  public boolean deleteIssue(BigInteger id) {
    boolean result = false;
    try {
      LogManager.getLogger(MantisBtClient.class).info(String.format("Delete issue id : %s", id.toString()));
      result = getClient().mc_issue_delete(username, password, id);
    } catch (NullPointerException ex) {
      //Weird but NPE is triggered when the deletion is successful.
      result = true;
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Determine if the ticket should be reopened.
   *
   * @param issue Issue to verify.
   * @return True if the issue should be reopened.
   */
  public boolean shouldBeReopened(IssueData issue) {
    return (issue != null
            && ((issue.getStatus().getId().compareTo(STATUS_CLOSED) == 0 || issue.getStatus().getId().compareTo(STATUS_TOVERIFY) == 0))
            && issue.getResolution().getId().compareTo(RESOLUTION_WONTFIX) != 0);
  }

  /**
   * Reopen a ticket.
   *
   * @param id Ticket id.
   * @return True if the ticket has been reopened.
   */
  public boolean reopenTicket(BigInteger id) {
    boolean result = false;
    try {
      LogManager.getLogger(MantisBtClient.class).info(String.format("Reopened issue id : %s", id.toString()));
      IssueData issueData = getIssue(id);
      issueData.setStatus(new ObjectRef(STATUS_NEW, "new"));
      issueData.setResolution(new ObjectRef(RESOLUTION_REOPEN, "reopened"));
      result = getClient().mc_issue_update(username, password, id, issueData);
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Add a note to a ticket.
   *
   * @param id Ticket id.
   * @param comment Comment to add.
   * @return Comment id.
   */
  public BigInteger addComment(BigInteger id, String comment) {
    BigInteger noteId = null;
    try {
      LogManager.getLogger(MantisBtClient.class).info(String.format("Add comment to issue id : %s", id.toString()));
      IssueNoteData issueNoteData = new IssueNoteData();
      issueNoteData.setText(comment);
      noteId = getClient().mc_issue_note_add(username, password, id, issueNoteData);
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return noteId;
  }

  /**
   * Create a MantisBT issue from vulnerability.
   *
   * @param vulnerability Vulnerability to create.
   * @return ID of the created ticket.
   */
  public BigInteger createIssue(Vulnerability vulnerability) {
    BigInteger result = null;
    try {
      LogManager.getLogger(MantisBtClient.class).info(String.format("Create issue for : %s", vulnerability.getId()));

      IssueData issue = new IssueData();
      //Manage projects
      issue.setProject(new ObjectRef(REDSCAN_PROJECT_ID, REDSCAN_PROJECT_NAME));
      issue.setCategory(DEFAULT_CATEGORY);

      issue.setSummary(vulnerability.getSummary());
      issue.setDescription(vulnerability.getDescription());

      //Add tags.
      issue.setTags(manageTags(vulnerability.getTags()));

      //Add severity and priority.
      issue.setSeverity(manageSeverity(vulnerability.getSeverity()));
      issue.setPriority(managePriority(vulnerability.getSeverity()));

      //Add custom fields.
      CustomFieldValueForIssueData[] customFields = new CustomFieldValueForIssueData[3];
      customFields[0] = new CustomFieldValueForIssueData(new ObjectRef(URL_CUSTOMFIELD_ID, "Url"), vulnerability.getUrl());
      customFields[1] = new CustomFieldValueForIssueData(new ObjectRef(ORIGIN_CUSTOMFIELD_ID, "Origin"), vulnerability.getOrigin());
      customFields[2] = new CustomFieldValueForIssueData(new ObjectRef(INTERNAL_ID_CUSTOMFIELD_ID, "InternalID"), vulnerability.getId());
      issue.setCustom_fields(customFields);

      result = getClient().mc_issue_add(username, password, issue);
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
    }
    return result;
  }

  /**
   * Get MantisBT soap client.
   *
   * @return MantisBT soap client
   */
  private MantisConnectPortType getClient() {
    if (mantisConnectLocator == null || mantisConnectPort == null) {
      try {
        LogManager.getLogger(MantisBtClient.class).info(String.format("SOAP url : %s", url));
        mantisConnectLocator = new MantisConnectLocator();
        mantisConnectPort = mantisConnectLocator.getMantisConnectPort(new URL(url));
      } catch (MalformedURLException ex) {
        LogManager.getLogger(MantisBtClient.class).error(String.format("Malformed SOAP url : %s", ex.getMessage()));
      } catch (ServiceException ex) {
        LogManager.getLogger(MantisBtClient.class).error(String.format("Service Exception : %s", ex.getMessage()));
      }
    }
    return mantisConnectPort;
  }

  private ObjectRef[] manageTags(String[] tags) {
    ObjectRef[] issueTags = null;
    try {
      String[] cleanTags = Arrays.stream(tags).distinct().toArray(String[]::new);
      List<TagData> existingTags = Arrays.asList(getClient().mc_tag_get_all(username, password, BigInteger.valueOf(1), BigInteger.valueOf(-1)).getResults());

      issueTags = new ObjectRef[cleanTags.length];
      for (int i = 0; i < cleanTags.length; ++i) {
        String tag = cleanTags[i];

        //Search for existing tags.
        TagData existingTag = existingTags.stream().filter(temp -> tag.equals(temp.getName())).findAny().orElse(null);

        if (existingTag == null) {
          existingTag = new TagData();
          existingTag.setName(tag);
          existingTag.setId(getClient().mc_tag_add(username, password, existingTag));
        }

        issueTags[i] = new ObjectRef(existingTag.getId(), existingTag.getName());
      }
    } catch (Exception ex) {
      LogManager.getLogger(MantisBtClient.class).error(String.format("Exception : %s", ex.getMessage()));
      issueTags = null;
    }
    return issueTags;
  }

  private ObjectRef manageSeverity(int severity) {
    ObjectRef result = null;
    switch (severity) {
      case Severity.CRITICAL:
        result = new ObjectRef(SEVERITY_CRITICAL, "Critical");
        break;
      case Severity.HIGH:
        result = new ObjectRef(SEVERITY_HIGH, "High");
        break;
      case Severity.MEDIUM:
        result = new ObjectRef(SEVERITY_MEDIUM, "Medium");
        break;
      case Severity.LOW:
        result = new ObjectRef(SEVERITY_LOW, "Low");
        break;
      case Severity.INFO:
        result = new ObjectRef(SEVERITY_INFO, "Info");
        break;
      default:
        result = null;
        break;
    }
    return result;
  }

  private ObjectRef managePriority(int severity) {
    ObjectRef result = null;
    switch (severity) {
      case Severity.CRITICAL:
        result = new ObjectRef(PRIORITY_P1, "P1");
        break;
      case Severity.HIGH:
        result = new ObjectRef(PRIORITY_P2, "P2");
        break;
      case Severity.MEDIUM:
        result = new ObjectRef(PRIORITY_P3, "P3");
        break;
      case Severity.LOW:
        result = new ObjectRef(PRIORITY_P4, "P4");
        break;
      case Severity.INFO:
        result = new ObjectRef(PRIORITY_P5, "P5");
        break;
      default:
        result = null;
        break;
    }
    return result;
  }

}
