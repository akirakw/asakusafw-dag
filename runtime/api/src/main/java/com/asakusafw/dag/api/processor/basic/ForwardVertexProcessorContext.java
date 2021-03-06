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
package com.asakusafw.dag.api.processor.basic;

import com.asakusafw.dag.api.processor.VertexProcessorContext;

/**
 * A {@link VertexProcessorContext} context which forwards method invocations to obtain properties and resources.
 */
public interface ForwardVertexProcessorContext extends VertexProcessorContext, ForwardEdgeIoProcessorContext {

    @Override
    VertexProcessorContext getForward();

    @Override
    default String getVertexId() {
        return getForward().getVertexId();
    }
}
