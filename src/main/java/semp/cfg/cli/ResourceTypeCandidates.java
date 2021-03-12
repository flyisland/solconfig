package semp.cfg.cli;

import semp.cfg.model.SempSpec;

import java.util.HashSet;

class ResourceTypeCandidates extends HashSet<String> {
    ResourceTypeCandidates() {
        super(SempSpec.TOP_RESOURCES.keySet());
    }
}
