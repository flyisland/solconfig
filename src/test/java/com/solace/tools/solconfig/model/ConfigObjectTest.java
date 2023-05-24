package com.solace.tools.solconfig.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigObjectTest {
    private static List<String> reservedExcludeObjectsList;
    private static List<String> reservedExcludeObjectIdsList;

    @BeforeAll
    static void setup() throws IOException {
        reservedExcludeObjectsList = List.of("/msgVpns/queues", "/msgVpns/queues/subscriptions",
                "/msgVpns/topicEndpoints");

        reservedExcludeObjectIdsList = List.of("/msgVpns/aclProfiles/%23acl-profile");
    }

    @Test
    void testisReservedObject() {
        ConfigObject configObject = new ConfigObject();
        boolean isReserved = configObject.isReservedObject("%23test", "/msgVpns/queues", reservedExcludeObjectsList, reservedExcludeObjectIdsList);
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23subs", "/msgVpns/queues/subscriptions", reservedExcludeObjectsList, reservedExcludeObjectIdsList);
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23te1", "/msgVpns/topicEndpoints", reservedExcludeObjectsList, reservedExcludeObjectIdsList);
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23test", "/msgVpns/clientProfiles", reservedExcludeObjectsList, reservedExcludeObjectIdsList);
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23acl-profile", "/msgVpns/aclProfiles", reservedExcludeObjectsList, reservedExcludeObjectIdsList);
        assertEquals(false, isReserved);
    }

    @Test
    void testisReservedObjectNoExcludeObjects() {
        ConfigObject configObject = new ConfigObject();
        boolean isReserved = configObject.isReservedObject("%23test", "/msgVpns/queues", new ArrayList<>(), reservedExcludeObjectIdsList);
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23subs", "/msgVpns/queues/subscriptions", new ArrayList<>(), reservedExcludeObjectIdsList);
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23te1", "/msgVpns/topicEndpoints", new ArrayList<>(), reservedExcludeObjectIdsList);
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23test", "/msgVpns/clientProfiles", new ArrayList<>(), reservedExcludeObjectIdsList);
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23acl-profile", "/msgVpns/aclProfiles", new ArrayList<>(), reservedExcludeObjectIdsList);
        assertEquals(false, isReserved);
    }

    @Test
    void testisReservedObjectNoExcludeObjectIds() {
        ConfigObject configObject = new ConfigObject();
        boolean isReserved = configObject.isReservedObject("%23test", "/msgVpns/queues", reservedExcludeObjectsList, new ArrayList<>());
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23subs", "/msgVpns/queues/subscriptions", reservedExcludeObjectsList, new ArrayList<>());
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23te1", "/msgVpns/topicEndpoints", reservedExcludeObjectsList, new ArrayList<>());
        assertEquals(false, isReserved);
        isReserved = configObject.isReservedObject("%23test", "/msgVpns/clientProfiles", reservedExcludeObjectsList, new ArrayList<>());
        assertEquals(true, isReserved);
        isReserved = configObject.isReservedObject("%23acl-profile", "/msgVpns/aclProfiles", reservedExcludeObjectsList, new ArrayList<>());
        assertEquals(true, isReserved);
    }
}

