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
package org.sonar.server.measure.live;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import org.sonar.api.rules.RuleType;
import org.sonar.db.issue.IssueGroupDto;
import org.sonar.db.rule.SeverityUtil;

public class IssueCounterImpl implements IssueCounter {

  private final Collection<IssueGroupDto> groups;

  public IssueCounterImpl(Collection<IssueGroupDto> groups) {
    this.groups = groups;
}

  @Override
  public Optional<String> getMaxSeverityOfUnresolved(RuleType ruleType, boolean onlyInLeak) {
    OptionalInt max = groups.stream()
      .filter(g -> g.getResolution() == null)
      .filter(g -> g.getRuleType() == ruleType.getDbConstant())
      .filter(g -> !onlyInLeak || g.isInLeak())
      .mapToInt(g -> SeverityUtil.getOrdinalFromSeverity(g.getSeverity()))
      .max();
    if (max.isPresent()) {
      return Optional.of(SeverityUtil.getSeverityFromOrdinal(max.getAsInt()));
    }
    return Optional.empty();
  }

  @Override
  public double getEffortOfUnresolved(RuleType type, boolean onlyInLeak) {
    int typeAsInt = type.getDbConstant();
    return groups.stream()
      .filter(g -> g.getResolution() == null)
      .filter(g -> typeAsInt == g.getRuleType())
      .filter(g -> !onlyInLeak || g.isInLeak())
      .mapToDouble(IssueGroupDto::getEffort)
      .sum();
  }

  @Override
  public long countUnresolvedBySeverity(String severity, boolean onlyInLeak) {
    return groups.stream()
      .filter(g -> g.getResolution() == null)
      .filter(g -> severity.equals(g.getSeverity()))
      .filter(g -> !onlyInLeak || g.isInLeak())
      .mapToLong(IssueGroupDto::getCount)
      .sum();
  }

  @Override
  public long countByResolution(String resolution, boolean onlyInLeak) {
    return groups.stream()
      .filter(g -> Objects.equals(resolution, g.getResolution()))
      .filter(g -> !onlyInLeak || g.isInLeak())
      .mapToLong(IssueGroupDto::getCount)
      .sum();
  }

  @Override
  public long countUnresolvedByType(RuleType type, boolean onlyInLeak) {
    int typeAsInt = type.getDbConstant();
    return groups.stream()
      .filter(g -> !onlyInLeak || g.isInLeak())
      .filter(g -> g.getResolution() == null)
      .filter(g -> typeAsInt == g.getRuleType())
      .mapToLong(IssueGroupDto::getCount)
      .sum();
  }

  @Override
  public long countByStatus(String status, boolean onlyInLeak) {
    return groups.stream()
      .filter(g -> !onlyInLeak || g.isInLeak())
      .filter(g -> Objects.equals(status, g.getStatus()))
      .mapToLong(IssueGroupDto::getCount)
      .sum();
  }

  @Override
  public long countUnresolved(boolean onlyInLeak) {
    return groups.stream()
      .filter(g -> !onlyInLeak || g.isInLeak())
      .filter(g -> g.getResolution() == null)
      .mapToLong(IssueGroupDto::getCount)
      .sum();
  }
}
