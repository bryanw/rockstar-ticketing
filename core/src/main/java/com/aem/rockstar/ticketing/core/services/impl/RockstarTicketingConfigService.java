package com.aem.rockstar.ticketing.core.services.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Rockstar Ticketing Service Configuration", description = "Service Configuration for Rockstar Ticketing")
public @interface RockstarTicketingConfigService {

    @AttributeDefinition(name = "Tennant", description = "Your company marketing cloud identifier")
    String getTenant();

    @AttributeDefinition(name = "API Key (Client ID)", description = "API Key (Client ID)")
    String getApiKey();

    @AttributeDefinition(name = "Technical account ID (sub)", description = "Technical account ID (sub)")
    String getTechnicalAccountId();

    @AttributeDefinition(name = "Organization ID (iss)", description = "Organization ID (iss)")
    String getOrganizationId();

    @AttributeDefinition(name = "IMS Host", description = "IMS Host")
    String getImsHost();

    @AttributeDefinition(name = "Client Secret", description = "Client Secret")
    String getClientSecret();

    @AttributeDefinition(name = "Private Key File", description = "Path to the Adobe IO private key")
    String getPrivateKeyFile();

    @AttributeDefinition(name = "Youtube API Key", description = "Youtube API Key")
    String getYoutubeApiKey();

}