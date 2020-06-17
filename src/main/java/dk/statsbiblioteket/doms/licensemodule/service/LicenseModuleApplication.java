package dk.statsbiblioteket.doms.licensemodule.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import dk.statsbiblioteket.doms.licensemodule.service.exception.ServiceExceptionMapper;

public class LicenseModuleApplication extends Application {

    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(
            JacksonJsonProvider.class,
            LicenseModuleResource.class,
            ServiceExceptionMapper.class
            ));
    }


}