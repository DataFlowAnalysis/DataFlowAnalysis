package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.palladiosimulator.pcm.core.entity.Entity;
import tools.mdsd.modelingfoundations.identifier.NamedElement;

public class VertexNameSelector extends VertexSelector {
    private static final String DSL_KEYWORD = "named";
    private static final Logger logger = LoggerManager.getLogger(VertexNameSelector.class);

    private final String name;
    private final boolean inverted;

    /**
     * Create a new {@link VertexNameSelector} that matches vertices with the given name.
     * @param name Name the vertex should have
     * @param context Context of the DSL Selector
     */
    public VertexNameSelector(String name, DSLContext context) {
        super(context);
        this.name = name;
        this.inverted = false;
    }

    /**
     * Create a new {@link VertexNameSelector} that matches vertices with the given name. Additionally, the inverted boolean
     * denotes whether the selector is inverted or not
     * @param name Name the vertex should (or should not) have
     * @param inverted Denotes whether the selector should be inverted or not
     * @param context Context of the DSL Selector
     */
    public VertexNameSelector(String name, boolean inverted, DSLContext context) {
        super(context);
        this.name = name;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        String vertexName;
        if (vertex.getReferencedElement() instanceof NamedElement namedElement) {
            vertexName = namedElement.getEntityName();
        } else if (vertex.getReferencedElement() instanceof Entity pcmEntity) {
            vertexName = pcmEntity.getEntityName();
        } else {
            return false;
        }
        return this.inverted ? !vertexName.equalsIgnoreCase(this.name) : vertexName.equalsIgnoreCase(this.name);
    }

    @Override
    public String toString() {
        return this.inverted ? DSL_INVERTED_SYMBOL + DSL_KEYWORD + " " + this.name : DSL_KEYWORD + " " + this.name;
    }

    /**
     * Parses a {@link VertexNameSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code named <Name>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexNameSelector} object
     */
    public static ParseResult<VertexNameSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse vertex name selector from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = false;
        if (string.startsWith(DSL_INVERTED_SYMBOL)) {
            string.advance(DSL_INVERTED_SYMBOL.length());
            inverted = true;
        }
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse vertex name selector from empty or invalid string!");
        }
        String[] split = string.getString()
                .split(" ");
        if (split.length == 0 || split[0].isEmpty()) {
            string.setPosition(position);
            return ParseResult.error("Invalid vertex name in vertex name selector!");
        }
        string.advance(split[0].length());
        string.advance(1);
        return ParseResult.ok(new VertexNameSelector(split[0], inverted, context));
    }

    /**
     * Returns the vertex name stored in the vertex name selector
     * @return Returns the name stored in the selector
     */
    public String getName() {
        return name;
    }

    /**
     * Returns, whether the vertex name selector is inverted
     * @return Returns true, if the vertex name selector is inverted. Otherwise, this method returns false
     */
    public boolean isInverted() {
        return inverted;
    }

}
