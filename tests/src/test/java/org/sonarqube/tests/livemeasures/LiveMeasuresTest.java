/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarqube.tests.livemeasures;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.Collections;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.sonarqube.qa.util.Tester;
import org.sonarqube.tests.Category4Suite;
import org.sonarqube.ws.client.issues.DoTransitionRequest;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;
import util.ItUtils;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;

public class LiveMeasuresTest {

  private static final String PROJECT_KEY = "LiveMeasuresTestExample";
  private static final String PROJECT_DIR = "livemeasures/LiveMeasuresTest";

  @ClassRule
  public static Orchestrator orchestrator = Category4Suite.ORCHESTRATOR;

  @Rule
  public Tester tester = new Tester(orchestrator);

  @Before
  public void resetData() {
    orchestrator.resetData();
  }

  @Test
  public void should_not_exclude_anything() {
    ItUtils.restoreProfile(orchestrator, getClass().getResource("/livemeasures/LiveMeasuresTest/one-bug-per-line-profile.xml"));
    orchestrator.getServer().provisionProject(PROJECT_KEY, "LiveMeasuresTestExample");
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "xoo", "one-bug-per-line-profile");

    orchestrator.executeBuildQuietly(SonarScanner.create(ItUtils.projectDir(PROJECT_DIR)));

    assertMeasure("bugs", 1);

    String issueKey = tester.wsClient().issues().search(new SearchRequest()).getIssuesList().get(0).getKey();
    tester.wsClient().issues().doTransition(
      new DoTransitionRequest().setIssue(issueKey).setTransition("falsepositive")
    );

    assertMeasure("bugs", 0);

    orchestrator.executeBuildQuietly(SonarScanner.create(ItUtils.projectDir(PROJECT_DIR)));

    assertMeasure("bugs", 0);
  }

  private void assertMeasure(String metricKey, int expectedValue) {
    int actual = parseInt(tester.wsClient().measures().component(
      new ComponentRequest()
        .setMetricKeys(Collections.singletonList(metricKey))
        .setComponent(PROJECT_KEY)
    ).getComponent().getMeasuresList().get(0).getValue());
    assertThat(actual).as("Value of measure " + metricKey).isEqualTo(expectedValue);
  }
}
