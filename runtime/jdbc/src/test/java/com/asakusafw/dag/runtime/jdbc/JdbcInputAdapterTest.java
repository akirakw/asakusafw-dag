/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dag.runtime.jdbc;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.asakusafw.bridge.stage.StageInfo;
import com.asakusafw.dag.api.processor.TaskInfo;
import com.asakusafw.dag.api.processor.TaskProcessorContext;
import com.asakusafw.dag.api.processor.TaskSchedule;
import com.asakusafw.dag.api.processor.testing.MockTaskProcessorContext;
import com.asakusafw.dag.api.processor.testing.MockVertexProcessorContext;
import com.asakusafw.dag.runtime.adapter.ExtractOperation.Input;
import com.asakusafw.dag.runtime.adapter.InputHandler;
import com.asakusafw.dag.runtime.adapter.InputHandler.InputSession;
import com.asakusafw.dag.runtime.jdbc.basic.BasicJdbcInputDriver;
import com.asakusafw.dag.runtime.jdbc.testing.KsvJdbcAdapter;
import com.asakusafw.dag.runtime.jdbc.testing.KsvModel;

/**
 * Test for {@link JdbcInputAdapter}.
 */
public class JdbcInputAdapterTest extends JdbcDagTestRoot {

    private static final StageInfo STAGE = new StageInfo("u", "b", "f", "s", "e", Collections.emptyMap());

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        insert(0, null, "Hello, world!");
        try (JdbcEnvironment environment = environment("testing")) {
            MockVertexProcessorContext vc = new MockVertexProcessorContext()
                    .withResource(StageInfo.class, STAGE)
                    .withResource(JdbcEnvironment.class, environment);
            try (JdbcInputAdapter adapter = new JdbcInputAdapter(vc)) {
                adapter.bind("t", driver(environment.getProfile("testing")));
                adapter.initialize();
                assertThat(collect(adapter), contains(new KsvModel(0, null, "Hello, world!")));
            }
        }
    }

    /**
     * multiple records.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        insert(1, null, "Hello1");
        insert(2, null, "Hello2");
        insert(3, null, "Hello3");
        try (JdbcEnvironment environment = environment("testing")) {
            MockVertexProcessorContext vc = new MockVertexProcessorContext()
                    .withResource(StageInfo.class, STAGE)
                    .withResource(JdbcEnvironment.class, environment);
            try (JdbcInputAdapter adapter = new JdbcInputAdapter(vc)) {
                adapter.bind("t", driver(environment.getProfile("testing")));
                adapter.initialize();
                assertThat(collect(adapter), contains(
                        new KsvModel(1, null, "Hello1"),
                        new KsvModel(2, null, "Hello2"),
                        new KsvModel(3, null, "Hello3")));
            }
        }
    }

    private BasicJdbcInputDriver driver(JdbcProfile profile) {
        BasicJdbcInputDriver driver = new BasicJdbcInputDriver(
                profile,
                "SELECT * FROM KSV ORDER BY M_KEY",
                new KsvJdbcAdapter());
        return driver;
    }

    private static List<KsvModel> collect(JdbcInputAdapter adapter) throws IOException, InterruptedException {
        List<KsvModel> results = new ArrayList<>();
        TaskSchedule schedule = adapter.getSchedule();
        InputHandler<Input, ? super TaskProcessorContext> handler = adapter.newHandler();
        for (TaskInfo info : schedule.getTasks()) {
            try (InputSession<Input> session = handler.start(new MockTaskProcessorContext(info))) {
                while (session.next()) {
                    Input input = session.get();
                    results.add(new KsvModel(input.getObject()));
                }
            }
        }
        return results;
    }
}
