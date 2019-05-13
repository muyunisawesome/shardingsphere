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

package org.apache.shardingsphere.core.parse;

import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.core.parse.antlr.autogen.OracleStatementParser;
import org.apache.shardingsphere.core.parse.api.SQLParser;
import org.apache.shardingsphere.core.parse.parser.advanced.AdvancedErrorStrategy;

/**
 * SQL parser for Oracle.
 * 
 * @author duhongjun
 */
public final class OracleParser extends OracleStatementParser implements SQLParser {
    
    public OracleParser(final TokenStream input) {
        super(input);
        setErrorHandler(new AdvancedErrorStrategy(IDENTIFIER_));
    }
}
