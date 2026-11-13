package org.dataflowanalysis.converter.web2dfd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.dataflowanalysis.converter.web2dfd.model.Child;

public class ChildSerializer extends StdSerializer<Child> {

    public ChildSerializer() {
        super(Child.class);
    }

    @Override
    public void serialize(Child child, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        gen.writeStringField("text", child.text());
        gen.writeStringField("id", child.id());
        gen.writeStringField("type", child.type());

        if (child.sourceId() != null)
            gen.writeStringField("sourceId", child.sourceId());
        if (child.targetId() != null)
            gen.writeStringField("targetId", child.targetId());

        gen.writeObjectField("labels", child.labels());
        gen.writeObjectField("ports", child.ports());
        gen.writeObjectField("annotations", child.annotations());
        gen.writeObjectField("children", child.children());

        // Only include position and size if type starts with "node"
        if (child.type() != null && child.type()
                .startsWith("node")) {
            gen.writeObjectField("position", child.position());
            gen.writeObjectField("size", child.size());
        }

        // Only include routingPoints for edges
        if (child.type() != null && child.type()
                .startsWith("edge")) {
            gen.writeObjectField("routingPoints", child.routingPoints());
        }

        gen.writeEndObject();
    }
}
