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

import biz.futureware.mantisconnect.IssueData;
import com.michelin.cert.redscan.utils.models.reports.Severity;
import com.michelin.cert.redscan.utils.models.reports.Vulnerability;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MantisBT client test classes.
 *
 * @author Maxime ESCOURBIAC
 */
public class MantisBtClientTest {

  MantisBtClient client;

  public MantisBtClientTest() {
    client = new MantisBtClient();
    MantisBtClient.init("http://localhost:8888/api/soap/mantisconnect.php","administrator","redscan");
  }

  /**
   * Test of all implemented method, of class MantisBtClient.
   */
  @Test
  public void testAll() {
    System.out.println("MantisBtClient:GetMantisVersion");
    String expResult = "2.26.0";
    String result = client.getMantisVersion();
    assertEquals(expResult, result);

    System.out.println("MantisBtClient:CreateIssue");
    Vulnerability vuln = new Vulnerability("INTERNALID1", Severity.CRITICAL, "Critical vulnerability", "Vuln description", "http://localhost/vuln", "Unit-Test", new String[]{"tag1", "tag2"});
    BigInteger issueCreated = client.createIssue(vuln);
    assertNotNull(issueCreated);

    System.out.println("MantisBtClient:GetIssue");
    IssueData issue = client.getIssue(issueCreated);
    assertNotNull(issue);
    issue = client.getIssue(BigInteger.ZERO);
    assertNull(issue);

    System.out.println("MantisBtClient:GetIssue");
    IssueData[] issues = client.getIssuesByInternalId("INTERNALID1");
    assertNotNull(issues);
    issues = client.getIssuesByInternalId("lkjdfdslkjfdslkf");
    assertNotNull(issues);
    assertEquals(0, issues.length);

    
    System.out.println("MantisBtClient:AddComment");
    BigInteger addComment = client.addComment(issueCreated, "Test comment");
    assertNotNull(addComment);

    System.out.println("MantisBtClient:DeleteIssue");
    boolean deleteIssue = client.deleteIssue(issueCreated);
    assertTrue(deleteIssue);
    deleteIssue = client.deleteIssue(BigInteger.ZERO);
    assertFalse(deleteIssue);
  }

}
