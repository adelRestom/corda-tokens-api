package com.template.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.template.webserver.Controller;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.webserver.services.WebServerPluginRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExamplePlugin implements WebServerPluginRegistry {

    private final List<Function<CordaRPCOps, ?>> webApis = ImmutableList.of(Controller::new);

    @NotNull
    @Override
    public Map<String, String> getStaticServeDirs() {
        return null;
    }

    @NotNull
    @Override
    public List<Function<CordaRPCOps, ? extends Object>> getWebApis() {
        return webApis;
    }

    @Override
    public void customizeJSONSerialization(@NotNull ObjectMapper om) {

    }
}
