package com.aem.rockstar.ticketing.core.services;

public interface AdobeIoService {

    public String getActivities() throws Exception;

    public String getActivity(String activityId) throws Exception;

    public String getAudiences() throws Exception;

    public String getAudience(String audienceId) throws Exception;

    public String getOfferDetails(String offerId) throws Exception;

    public String createActivity(final String activity) throws Exception;

    public String updateActivity(String activity) throws Exception;

    public String createOffer(String offer);

    public String updateOffer(String offer);

}