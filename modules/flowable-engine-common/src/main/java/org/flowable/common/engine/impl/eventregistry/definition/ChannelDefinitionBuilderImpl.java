/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl.eventregistry.definition;

import org.flowable.common.engine.api.eventregistry.EventRegistry;
import org.flowable.common.engine.api.eventregistry.InboundEventChannelAdapter;
import org.flowable.common.engine.api.eventregistry.InboundEventDeserializer;
import org.flowable.common.engine.api.eventregistry.InboundEventKeyDetector;
import org.flowable.common.engine.api.eventregistry.InboundEventPayloadExtractor;
import org.flowable.common.engine.api.eventregistry.InboundEventProcessingPipeline;
import org.flowable.common.engine.api.eventregistry.InboundEventTransformer;
import org.flowable.common.engine.api.eventregistry.definition.ChannelDefinition;
import org.flowable.common.engine.api.eventregistry.definition.ChannelDefinitionBuilder;
import org.flowable.common.engine.impl.eventregistry.JsonFieldBasedInboundEventKeyDetector;
import org.flowable.common.engine.impl.eventregistry.JsonPathBasedInboundEventKeyDetector;
import org.flowable.common.engine.impl.eventregistry.deserializer.StringToJsonDeserializer;
import org.flowable.common.engine.impl.eventregistry.payload.JsonFieldToMapPayloadExtractor;
import org.flowable.common.engine.impl.eventregistry.pipeline.DefaultEventProcessingPipeline;
import org.flowable.common.engine.impl.eventregistry.transformer.DefaultInboundEventTransformer;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Joram Barrez
 * @author Filip Hrisafov
 */
public class ChannelDefinitionBuilderImpl implements ChannelDefinitionBuilder {

    protected EventRegistry eventRegistry;

    protected String key;
    protected InboundEventChannelAdapter inboundEventChannelAdapter;
    protected InboundEventProcessingPipelineBuilder inboundEventProcessingPipelineBuilder;

    public ChannelDefinitionBuilderImpl(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    @Override
    public ChannelDefinitionBuilder key(String key) {
        this.key = key;
        return this;
    }

    @Override
    public InboundEventProcessingPipelineBuilder channelAdapter(InboundEventChannelAdapter inboundEventChannelAdapter) {
        this.inboundEventChannelAdapter = inboundEventChannelAdapter;
        this.inboundEventProcessingPipelineBuilder = new InboundEventProcessingPipelineBuilderImpl<>(eventRegistry, this);
        return this.inboundEventProcessingPipelineBuilder;
    }

    @Override
    public ChannelDefinition register() {
        ChannelDefinitionImpl channelDefinition = new ChannelDefinitionImpl();
        channelDefinition.setKey(key);
        channelDefinition.setInboundEventChannelAdapter(inboundEventChannelAdapter);

        InboundEventProcessingPipeline inboundEventProcessingPipeline = inboundEventProcessingPipelineBuilder.build();
        channelDefinition.setInboundEventProcessingPipeline(inboundEventProcessingPipeline);

        eventRegistry.registerChannelDefinition(channelDefinition);

        return channelDefinition;
    }

    public static class InboundEventProcessingPipelineBuilderImpl<T> implements InboundEventProcessingPipelineBuilder {

        protected EventRegistry eventRegistry;
        protected ChannelDefinitionBuilderImpl channelDefinitionBuilder;

        protected InboundEventProcessingPipeline customInboundEventProcessingPipeline;
        protected InboundEventDeserializer<T> inboundEventDeserializer;
        protected InboundEventKeyDetector<T> inboundEventKeyDetector;
        protected InboundEventPayloadExtractor<T> inboundEventPayloadExtractor;
        protected InboundEventTransformer inboundEventTransformer;

        public InboundEventProcessingPipelineBuilderImpl(EventRegistry eventRegistry, ChannelDefinitionBuilderImpl channelDefinitionBuilder) {
            this.eventRegistry = eventRegistry;
            this.channelDefinitionBuilder = channelDefinitionBuilder;
        }

        @Override
        public InboundEventKeyJsonDetectorBuilder jsonDeserializer() {

            InboundEventProcessingPipelineBuilderImpl<JsonNode> jsonPipelineBuilder = new InboundEventProcessingPipelineBuilderImpl<>(eventRegistry,
                channelDefinitionBuilder);
            this.channelDefinitionBuilder.inboundEventProcessingPipelineBuilder = jsonPipelineBuilder;

            jsonPipelineBuilder.inboundEventDeserializer = new StringToJsonDeserializer();
            return new InboundEventKeyJsonDetectorBuilderImpl(jsonPipelineBuilder);
        }

        @Override
        public <D> InboundEventKeyDetectorBuilder<D> deserializer(InboundEventDeserializer<D> deserializer) {
            InboundEventProcessingPipelineBuilderImpl<D> customPipelineBuilder = new InboundEventProcessingPipelineBuilderImpl<>(eventRegistry,
                channelDefinitionBuilder);
            this.channelDefinitionBuilder.inboundEventProcessingPipelineBuilder = customPipelineBuilder;
            customPipelineBuilder.inboundEventDeserializer = deserializer;
            return new InboundEventDefinitionKeyDetectorBuilderImpl<>(customPipelineBuilder);
        }

        @Override
        public ChannelDefinitionBuilder eventProcessingPipeline(InboundEventProcessingPipeline inboundEventProcessingPipeline) {
            this.customInboundEventProcessingPipeline = inboundEventProcessingPipeline;
            return channelDefinitionBuilder;
        }

        @Override
        public InboundEventProcessingPipeline build() {
            if (customInboundEventProcessingPipeline != null) {
                return customInboundEventProcessingPipeline;
            } else {
                return new DefaultEventProcessingPipeline<>(eventRegistry,
                    inboundEventDeserializer, inboundEventKeyDetector, inboundEventPayloadExtractor, inboundEventTransformer);
            }
        }

    }

    public static class InboundEventKeyJsonDetectorBuilderImpl implements InboundEventKeyJsonDetectorBuilder {

        protected InboundEventProcessingPipelineBuilderImpl<JsonNode> inboundEventProcessingPipelineBuilder;

        public InboundEventKeyJsonDetectorBuilderImpl(InboundEventProcessingPipelineBuilderImpl<JsonNode> inboundEventProcessingPipelineBuilder) {
            this.inboundEventProcessingPipelineBuilder = inboundEventProcessingPipelineBuilder;
        }

        @Override
        public InboundEventPayloadJsonExtractorBuilder detectEventKeyUsingJsonField(String field) {
            this.inboundEventProcessingPipelineBuilder.inboundEventKeyDetector = new JsonFieldBasedInboundEventKeyDetector(field);
            return new InboundEventPayloadJsonExtractorBuilderImpl(inboundEventProcessingPipelineBuilder);
        }

        @Override
        public InboundEventPayloadJsonExtractorBuilder detectEventKeyUsingJsonPathExpression(String jsonPathExpression) {
            this.inboundEventProcessingPipelineBuilder.inboundEventKeyDetector = new JsonPathBasedInboundEventKeyDetector(jsonPathExpression);
            return new InboundEventPayloadJsonExtractorBuilderImpl(inboundEventProcessingPipelineBuilder);
        }
    }

    public static class InboundEventDefinitionKeyDetectorBuilderImpl<T> implements InboundEventKeyDetectorBuilder<T> {

        protected InboundEventProcessingPipelineBuilderImpl<T> inboundEventProcessingPipelineBuilder;

        public InboundEventDefinitionKeyDetectorBuilderImpl(InboundEventProcessingPipelineBuilderImpl<T> inboundEventProcessingPipelineBuilder) {
            this.inboundEventProcessingPipelineBuilder = inboundEventProcessingPipelineBuilder;
        }

        @Override
        public InboundEventPayloadExtractorBuilder<T> detectEventKeyUsingKeyDetector(InboundEventKeyDetector<T> inboundEventKeyDetector) {
            this.inboundEventProcessingPipelineBuilder.inboundEventKeyDetector = inboundEventKeyDetector;
            return new InboundEventPayloadExtractorBuilderImpl<>(inboundEventProcessingPipelineBuilder);
        }

    }

    public static class InboundEventPayloadJsonExtractorBuilderImpl implements InboundEventPayloadJsonExtractorBuilder {

        protected InboundEventProcessingPipelineBuilderImpl<JsonNode> inboundEventProcessingPipelineBuilder;

        public InboundEventPayloadJsonExtractorBuilderImpl(InboundEventProcessingPipelineBuilderImpl<JsonNode> inboundEventProcessingPipelineBuilder) {
            this.inboundEventProcessingPipelineBuilder = inboundEventProcessingPipelineBuilder;
        }

        @Override
        public InboundEventTransformerBuilder jsonFieldsMapDirectlyToPayload() {
            this.inboundEventProcessingPipelineBuilder.inboundEventPayloadExtractor = new JsonFieldToMapPayloadExtractor();
            return new InboundEventTransformerBuilderImpl(inboundEventProcessingPipelineBuilder);
        }

    }

    public static class InboundEventPayloadExtractorBuilderImpl<T> implements InboundEventPayloadExtractorBuilder<T> {

        protected InboundEventProcessingPipelineBuilderImpl<T> inboundEventProcessingPipelineBuilder;

        public InboundEventPayloadExtractorBuilderImpl(InboundEventProcessingPipelineBuilderImpl<T> inboundEventProcessingPipelineBuilder) {
            this.inboundEventProcessingPipelineBuilder = inboundEventProcessingPipelineBuilder;
        }

        @Override
        public InboundEventTransformerBuilder payloadExtractor(InboundEventPayloadExtractor<T> inboundEventPayloadExtractor) {
            this.inboundEventProcessingPipelineBuilder.inboundEventPayloadExtractor = inboundEventPayloadExtractor;
            return new InboundEventTransformerBuilderImpl(inboundEventProcessingPipelineBuilder);
        }

    }

    public static class InboundEventTransformerBuilderImpl implements InboundEventTransformerBuilder {

        protected InboundEventProcessingPipelineBuilderImpl<?> inboundEventProcessingPipelineBuilder;

        public InboundEventTransformerBuilderImpl(InboundEventProcessingPipelineBuilderImpl<?> inboundEventProcessingPipelineBuilder) {
            this.inboundEventProcessingPipelineBuilder = inboundEventProcessingPipelineBuilder;
        }

        @Override
        public ChannelDefinitionBuilder transformer(InboundEventTransformer inboundEventTransformer) {
            this.inboundEventProcessingPipelineBuilder.inboundEventTransformer = inboundEventTransformer;
            return this.inboundEventProcessingPipelineBuilder.channelDefinitionBuilder;
        }

        @Override
        public ChannelDefinition register() {
            this.inboundEventProcessingPipelineBuilder.inboundEventTransformer = new DefaultInboundEventTransformer();
            return this.inboundEventProcessingPipelineBuilder.channelDefinitionBuilder.register();
        }

    }

}