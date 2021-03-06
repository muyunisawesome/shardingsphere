/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.dbtest.engine.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.dbtest.engine.SQLType;
import org.apache.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Integrate test parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrateTestParameters {
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private static IntegrateTestEnvironment integrateTestEnvironment = IntegrateTestEnvironment.getInstance();
    
    /**
     * Get parameters with assertions.
     * 
     * @param sqlType SQL type
     * @return integrate test parameters.
     */
    public static Collection<Object[]> getParametersWithAssertion(final SQLType sqlType) {
        Map<DatabaseType, Collection<Object[]>> availableCases = new LinkedHashMap<>();
        Map<DatabaseType, Collection<Object[]>> disabledCases = new LinkedHashMap<>();
        getIntegrateTestCase(sqlType).forEach(integrateTestCase -> {
            getDatabaseTypes(integrateTestCase.getDbTypes()).forEach(databaseType -> {
                if (IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType)) {
                    availableCases.putIfAbsent(databaseType, new LinkedList<>());
                    availableCases.get(databaseType).addAll(getParametersWithAssertion(databaseType, SQLCaseType.Literal, integrateTestCase));
                    availableCases.get(databaseType).addAll(getParametersWithAssertion(databaseType, SQLCaseType.Placeholder, integrateTestCase));
                } else {
                    disabledCases.putIfAbsent(databaseType, new LinkedList<>());
                    disabledCases.get(databaseType).addAll(getParametersWithAssertion(databaseType, SQLCaseType.Literal, integrateTestCase));
                    disabledCases.get(databaseType).addAll(getParametersWithAssertion(databaseType, SQLCaseType.Placeholder, integrateTestCase));
                }
            });
        });
        printTestPlan(availableCases, disabledCases, calculateRunnableTestAnnotation());
        return availableCases.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    @SneakyThrows
    private static Collection<Object[]> getParametersWithAssertion(final DatabaseType databaseType, final SQLCaseType caseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        if (integrateTestCase.getIntegrateTestCaseAssertions().isEmpty()) {
            result.addAll(getParametersWithAssertion(integrateTestCase, null, databaseType, caseType));
            return result;
        }
        for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
            result.addAll(getParametersWithAssertion(integrateTestCase, each, databaseType, caseType));
        }
        return result;
    }
    
    private static Collection<Object[]> getParametersWithAssertion(
            final IntegrateTestCase integrateTestCase, final IntegrateTestCaseAssertion assertion, final DatabaseType databaseType, final SQLCaseType caseType) throws ParseException {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[6];
            data[0] = integrateTestCase.getPath();
            data[1] = assertion;
            data[2] = each;
            data[3] = databaseType.getName();
            data[4] = caseType;
            data[5] = getSQL(integrateTestCase.getSql(), assertion, caseType);
            result.add(data);
        }
        return result;
    }
    
    /**
     * Get parameters with test cases.
     *
     * @param sqlType SQL type
     * @return integrate test parameters.
     */
    public static Collection<Object[]> getParametersWithCase(final SQLType sqlType) {
        Map<DatabaseType, Collection<Object[]>> availableCases = new LinkedHashMap<>();
        Map<DatabaseType, Collection<Object[]>> disabledCases = new LinkedHashMap<>();
        getIntegrateTestCase(sqlType).forEach(integrateTestCase ->
            getDatabaseTypes(integrateTestCase.getDbTypes()).forEach(databaseType -> {
                if (IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(databaseType)) {
                    availableCases.putIfAbsent(databaseType, new LinkedList<>());
                    availableCases.get(databaseType).addAll(getParametersWithCase(databaseType, integrateTestCase));
                } else {
                    disabledCases.putIfAbsent(databaseType, new LinkedList<>());
                    disabledCases.get(databaseType).addAll(getParametersWithCase(databaseType, integrateTestCase));
                }
            }));
        printTestPlan(availableCases, disabledCases, calculateRunnableTestAnnotation());
        return availableCases.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private static Collection<Object[]> getParametersWithCase(final DatabaseType databaseType, final IntegrateTestCase integrateTestCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : integrateTestEnvironment.getRuleTypes()) {
            Object[] data = new Object[4];
            data[0] = integrateTestCase;
            data[1] = each;
            data[2] = databaseType.getName();
            data[3] = integrateTestCase.getSql();
            result.add(data);
        }
        return result;
    }
    
    private static Collection<DatabaseType> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? IntegrateTestEnvironment.getInstance().getDatabaseTypes()
            : Splitter.on(',').trimResults().splitToList(databaseTypes).stream().map(DatabaseTypes::getActualDatabaseType).collect(Collectors.toList());
    }
    
    private static String getSQL(final String sql, final IntegrateTestCaseAssertion assertion, final SQLCaseType sqlCaseType) throws ParseException {
        return sqlCaseType == SQLCaseType.Literal ? getLiteralSQL(sql, assertion) : sql;
    }
    
    private static String getLiteralSQL(final String sql, final IntegrateTestCaseAssertion assertion) throws ParseException {
        final List<Object> parameters = null != assertion ? assertion.getSQLValues().stream().map(SQLValue::toString).collect(Collectors.toList()) : null;
        if (null == parameters || parameters.isEmpty()) {
            return sql;
        }
        return String.format(sql.replace("%", "$").replace("?", "%s"), parameters.toArray()).replace("$", "%")
            .replace("%%", "%").replace("'%'", "'%%'");
    }
    
    private static List<? extends IntegrateTestCase> getIntegrateTestCase(final SQLType sqlType) {
        switch (sqlType) {
            case DQL:
                return integrateTestCasesLoader.getDqlIntegrateTestCases();
            case DML:
                return integrateTestCasesLoader.getDmlIntegrateTestCases();
            case DDL:
                return integrateTestCasesLoader.getDdlIntegrateTestCases();
            case DCL:
                return integrateTestCasesLoader.getDclIntegrateTestCases();
            default:
                throw new UnsupportedOperationException(sqlType.name());
        }
    }
    
    private static void printTestPlan(final Map<DatabaseType, Collection<Object[]>> availableCases, final Map<DatabaseType, Collection<Object[]>> disabledCases, final long factor) {
        Collection<String> activePlan = new LinkedList<>();
        for (Map.Entry<DatabaseType, Collection<Object[]>> entry : availableCases.entrySet()) {
            activePlan.add(String.format("%s(%s)", entry.getKey().getName(), entry.getValue().size() * factor));
        }
        Collection<String> disabledPlan = new LinkedList<>();
        for (Map.Entry<DatabaseType, Collection<Object[]>> entry : disabledCases.entrySet()) {
            disabledPlan.add(String.format("%s(%s)", entry.getKey().getName(), entry.getValue().size() * factor));
        }
        System.out.println("[INFO] ======= Test Plan =======");
        String summary = String.format("[%s] Total: %s, Active: %s, Disabled: %s",
            disabledPlan.isEmpty() ? "INFO" : "WARN",
            (availableCases.values().stream().mapToLong(Collection::size).sum() + disabledCases.values().stream().mapToLong(Collection::size).sum()) * factor,
            activePlan.isEmpty() ? 0 : Joiner.on(", ").join(activePlan), disabledPlan.isEmpty() ? 0 : Joiner.on(", ").join(disabledPlan));
        System.out.println(summary);
    }
    
    @SneakyThrows
    private static long calculateRunnableTestAnnotation() {
        long result = 0;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTraceElements.length; i++) {
            Class<?> callerClazz = Class.forName(stackTraceElements[i].getClassName());
            result += Arrays.stream(callerClazz.getMethods()).filter(method -> method.isAnnotationPresent(Test.class)).count();
        }
        return result;
    }
}
