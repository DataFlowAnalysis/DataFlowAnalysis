package org.dataflowanalysis.analysis.dsl.selectors.vertex;

import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexNameSelector extends VertexSelector {
    private static final String DSL_KEYWORD = "name";
    private static final Logger logger = LoggerManager.getLogger(VertexNameSelector.class);

    private final String name;
    private final boolean inverted;
    private final boolean contains;

    /**
     * Create a new {@link VertexNameSelector} that matches vertices with the given name.
     * @param name Name the vertex should have
     * @param context Context of the DSL Selector
     */
    public VertexNameSelector(String name, DSLContext context) {
        super(context);
        this.name = name;
        this.inverted = false;
        this.contains = false;
    }

    /**
     * Create a new {@link VertexNameSelector} that matches vertices with the given name. Additionally, the inverted
     * boolean denotes whether the selector is inverted or not
     * @param name Name the vertex should (or should not) have
     * @param inverted Denotes whether the selector should be inverted or not
     * @param context Context of the DSL Selector
     */
    public VertexNameSelector(String name, boolean inverted, boolean contains, DSLContext context) {
        super(context);
        this.name = name;
        this.inverted = inverted;
        this.contains = contains;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return this.matches(vertex, vertex.getPreviousVertexInformation()
                .getPreviousVertexNames()
                .stream()
                .toList(), dslConstraintTrace);
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return this.matches(vertex, Collections.singletonList(vertex.getName()), dslConstraintTrace);
    }

    private boolean matches(AbstractVertex<?> vertex, List<String> vertexNames, DSLConstraintTrace dslConstraintTrace) {
        boolean nameMatches = this.contains ? vertexNames.stream()
                .anyMatch(it -> it.contains(this.name))
                : vertexNames.stream()
                        .anyMatch(it -> it.equalsIgnoreCase(this.name));
        return this.inverted != nameMatches;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append(" ");
        if (this.inverted)
            stringBuilder.append(AbstractParseable.DSL_INVERTED_SYMBOL + " ");
        stringBuilder.append(DSL_KEYWORD);
        stringBuilder.append(" ");
        if (this.contains)
            stringBuilder.append(AbstractSelector.DSL_CONTAINS + " ");
        stringBuilder.append(this.name);
        return stringBuilder.toString();
    }

    /**
     * Parses a {@link VertexNameSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code vertexName <Name>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexNameSelector} object
     */
    public static ParseResult<VertexSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse vertex name selector from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = false;
        if (string.startsWith(AbstractParseable.DSL_INVERTED_SYMBOL)) {
            string.advance(AbstractParseable.DSL_INVERTED_SYMBOL.length());
            inverted = true;
        }

        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse vertex name selector from empty or invalid string!");
        }

        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length());
        string.skipWhitespace();

        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse vertex name selector from empty or invalid string!");
        }

        boolean contains = false;
        if (string.startsWith(AbstractSelector.DSL_CONTAINS)) {
            string.advance(AbstractSelector.DSL_CONTAINS.length());
            contains = true;
        }
        string.skipWhitespace();
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
        return ParseResult.ok(new VertexNameSelector(split[0], inverted, contains, context));
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
