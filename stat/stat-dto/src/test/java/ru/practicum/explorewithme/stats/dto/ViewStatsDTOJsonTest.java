package ru.practicum.explorewithme.stats.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ViewStatsDTOJsonTest {

    @SuppressWarnings("unused")
    @Autowired
    private JacksonTester<ViewStatsDTO> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        var dto = new ViewStatsDTO("ewm-main-service", "/events", 42L);
        var json = jacksonTester.write(dto);
        assertThat(json).hasJsonPathStringValue("$.app");
        assertThat(json).hasJsonPathStringValue("$.uri");
        assertThat(json).hasJsonPathNumberValue("$.hits");
        assertThat(json).extractingJsonPathNumberValue("$.hits").isEqualTo(42);
    }

    @Test
    void testDeserialize() throws Exception {
        // CHECKSTYLE:OFF
        String content = """
                {
                    "app": "app1",
                    "uri": "/uri",
                    "hits": 7
                }
                """;
        // CHECKSTYLE:ON
        var dto = jacksonTester.parse(content).getObject();
        assertThat(dto.getApp()).isEqualTo("app1");
        assertThat(dto.getUri()).isEqualTo("/uri");
        assertThat(dto.getHits()).isEqualTo(7L);
    }
}