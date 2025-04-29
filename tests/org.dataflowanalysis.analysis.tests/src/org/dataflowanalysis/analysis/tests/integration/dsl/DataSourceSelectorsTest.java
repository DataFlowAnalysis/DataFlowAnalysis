package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.DataSourceSelectors;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DataSourceSelectorsTest {
    @ParameterizedTest
    @MethodSource("correctDataSourceSelectors")
    public void shouldParseCorrectly(String dataSourceSelectorString) {
        StringView stringView = new StringView(dataSourceSelectorString);
        ParseResult<DataSourceSelectors> dataSourceSelectors = DataSourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(dataSourceSelectors.successful());
        assertTrue(stringView.empty());
        assertEquals(dataSourceSelectorString, dataSourceSelectors.getResult().toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectDataSourceSelectors")
    public void shouldNotParse(String dataSourceSelectorString) {
        StringView stringView = new StringView(dataSourceSelectorString);
        ParseResult<DataSourceSelectors> dataSourceSelectors = DataSourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(dataSourceSelectors.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctDataSourceSelectors() {
        return Stream.of(Arguments.of("data A.B"), Arguments.of("data otherA.otherB"), Arguments.of("data A.B named C"),
                Arguments.of("data A.B,C.D named E"), Arguments.of("data A.B,C.D E.F named G"));
    }

    private static Stream<Arguments> incorrectDataSourceSelectors() {
        return Stream.of(Arguments.of("data A"), Arguments.of(""), Arguments.of("data"), Arguments.of("data A.B C"));
    }
}
