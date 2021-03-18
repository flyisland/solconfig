package com.solace.tools.sempcfg.cli;

import com.solace.tools.sempcfg.model.SempSpec;

import java.util.HashSet;

class ResourceTypeCandidates extends HashSet<String> {
    ResourceTypeCandidates() {
        super(SempSpec.TOP_RESOURCES.keySet());
    }
}
