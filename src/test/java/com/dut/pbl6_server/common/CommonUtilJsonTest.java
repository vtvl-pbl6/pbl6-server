package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.util.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonUtilJsonTest {
    @Test
    void encode_ValidObject_ReturnsJsonString() {
        // Arrange
        Person person = new Person("John", 30);

        // Act
        String json = CommonUtils.Json.encode(person);

        // Assert
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.contains("John"));
        Assertions.assertTrue(json.contains("30"));
    }

    @Test
    void encode_NullObject_ReturnsNull() {
        // Act
        String json = CommonUtils.Json.encode(null);

        // Assert
        Assertions.assertNull(json);
    }

    @Test
    void decode_ValidJson_ReturnsObject() {
        // Arrange
        String json = "{\"name\":\"John\",\"age\":30}";

        // Act
        Person person = CommonUtils.Json.decode(json, Person.class);

        // Assert
        Assertions.assertNotNull(person);
        Assertions.assertEquals("John", person.getName());
        Assertions.assertEquals(30, person.getAge());
    }

    @Test
    void decode_InvalidJson_ReturnsNull() {
        // Arrange
        String json = "invalid json";

        // Act
        Person person = CommonUtils.Json.decode(json, Person.class);

        // Assert
        Assertions.assertNull(person);
    }

    @Test
    void decode_ValidMap_ReturnsObject() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);

        // Act
        Person person = CommonUtils.Json.decode(map, Person.class);

        // Assert
        Assertions.assertNotNull(person);
        Assertions.assertEquals("John", person.getName());
        Assertions.assertEquals(30, person.getAge());
    }

    @Test
    void decode_InvalidMap_ReturnsNull() {
        // Arrange
        Map<String, Object> map = null;

        // Act
        Person person = CommonUtils.Json.decode(map, Person.class);

        // Assert
        Assertions.assertNull(person);
    }

    @Test
    void decode_ValidListOfMaps_ReturnsListOfObjects() {
        // Arrange
        Map<String, Object> map1 = new HashMap<>();
        map1.put("name", "John");
        map1.put("age", 30);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "Jane");
        map2.put("age", 25);

        List<Map<String, Object>> list = Arrays.asList(map1, map2);

        // Act
        List<Person> people = CommonUtils.Json.decode(list, Person.class);

        // Assert
        Assertions.assertNotNull(people);
        Assertions.assertEquals(2, people.size());
        Assertions.assertEquals("John", people.get(0).getName());
        Assertions.assertEquals("Jane", people.get(1).getName());
    }

    @Test
    void decode_InvalidListOfMaps_ReturnsNull() {
        // Arrange
        List<Map<String, Object>> list = null;

        // Act
        List<Person> people = CommonUtils.Json.decode(list, Person.class);

        // Assert
        Assertions.assertNull(people);
    }

    @Test
    void convertObjectToMap_ValidObject_ReturnsMap() {
        // Arrange
        Person person = new Person("John", 30);

        // Act
        Map<String, Object> map = CommonUtils.Json.convertObjectToMap(person);

        // Assert
        Assertions.assertNotNull(map);
        Assertions.assertEquals("John", map.get("name"));
        Assertions.assertEquals(30, map.get("age"));
    }

    @Test
    void convertObjectToMap_NullObject_ReturnsNull() {
        // Act
        Map<String, Object> map = CommonUtils.Json.convertObjectToMap(null);

        // Assert
        Assertions.assertNull(map);
    }

    // Example Person class for testing
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    static class Person {
        private String name;
        private int age;
    }
}
