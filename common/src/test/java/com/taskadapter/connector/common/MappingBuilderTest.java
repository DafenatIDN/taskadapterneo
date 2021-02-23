package com.taskadapter.connector.common;

import com.taskadapter.connector.MappingBuilder;
import com.taskadapter.connector.definition.ExportDirection;
import com.taskadapter.connector.definition.FieldMapping;
import com.taskadapter.model.Field;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingBuilderTest {
    @Test
    public void skipsFieldsThatAreNotSelected() {
        var rows = MappingBuilder.build(
                List.of(FieldMapping.apply(Field.apply("summary"), Field.apply("summary"), false, "default")),
                ExportDirection.RIGHT

        );
        assertThat(rows).isEmpty();
    }

    @Test
    public void exportRightProcessesSelectedFields() {
        var rows = MappingBuilder.build(
                List.of(FieldMapping.apply(
                        Field.apply("JiraSummary"), Field.apply("RedmineSummary"), true, "default")),
                ExportDirection.RIGHT

        );
        assertThat(rows.get(0).sourceField().get().name()).isEqualTo("JiraSummary");
        assertThat(rows.get(0).targetField().get().name()).isEqualTo("RedmineSummary");
    }

    @Test
    public void exportLeftProcessesSelectedFields() {
        var rows = MappingBuilder.build(
                List.of(FieldMapping.apply(
                        Field.apply("JiraSummary"), Field.apply("RedmineSummary"), true, "default")),
                ExportDirection.LEFT

        );
        assertThat(rows.get(0).sourceField().get().name()).isEqualTo("RedmineSummary");
        assertThat(rows.get(0).targetField().get().name()).isEqualTo("JiraSummary");
    }
}