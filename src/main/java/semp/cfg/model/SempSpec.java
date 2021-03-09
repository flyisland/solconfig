package semp.cfg.model;

import java.util.Map;

public class SempSpec {
    static public final Map<String, String> TOP_RESOURCES = Map.of("vpn", "msgVpns", "cluster", "dmrClusters", "ca", "certAuthorities");
    static public final String SEMP_VERSION = "sempVersion";
}
