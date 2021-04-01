package com.solace.tools.solconfig.cli;

import com.solace.tools.solconfig.model.SempSpec;

import java.util.HashSet;

class ResourceTypeCandidates extends HashSet<String> {
    ResourceTypeCandidates() {
        super(SempSpec.TOP_RESOURCES.keySet());
    }
}
