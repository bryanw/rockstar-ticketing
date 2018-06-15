package com.aem.rockstar.ticketing.core.servlets;

import com.aem.rockstar.ticketing.core.services.AdobeIoService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.stream.Collectors;

@Component(service=Servlet.class,
        property={
                Constants.SERVICE_DESCRIPTION + "=Adobe IO GET Servlet",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.methods=" + HttpConstants.METHOD_PUT,
                "sling.servlet.methods=" + HttpConstants.METHOD_POST,
                "sling.servlet.extensions=" + "json",
                "sling.servlet.paths=" + "/bin/rockstar/jwt",
                "sling.servlet.paths=" + "/bin/rockstar/activities",
                "sling.servlet.paths=" + "/bin/rockstar/activity",
                "sling.servlet.paths=" + "/bin/rockstar/audiences",
                "sling.servlet.paths=" + "/bin/rockstar/offerdetails"
        })
public class AdobeIoProxyServlet extends SlingAllMethodsServlet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final long serialVersionUid = 1L;

    @Reference
    private AdobeIoService adobeIoService;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            String servletPath = req.getPathInfo();
            if ("/bin/rockstar/activities".equals(servletPath)) {
                String activities = adobeIoService.getActivities();
                resp.getWriter().write(activities);
            }
            else if ("/bin/rockstar/activity.json".equals(servletPath)) {
                String activityId = req.getParameter("activityId");
                String activities = adobeIoService.getActivity(activityId);
                resp.getWriter().write(activities);
            }
            else if ("/bin/rockstar/audiences".equals(servletPath)) {
                String audiences = adobeIoService.getAudiences();
                resp.getWriter().write(audiences);
            }
            else if ("/bin/rockstar/audience".equals(servletPath)) {
                String audience = adobeIoService.getActivity(req.getParameter("audienceId"));
                resp.getWriter().write(audience);
            }
            else if ("/bin/rockstar/offerdetails".equals(servletPath)) {
                String offerdetails = adobeIoService.getOfferDetails(req.getParameter("offerId"));
                resp.getWriter().write(offerdetails);
            }
        }
        catch (Exception e) {
            resp.sendError(500, "{'error': 'Error calling Adobe IO GET servlet " + req.getPathInfo() + "'}");
        }

    }

    @Override
    protected void doPut(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            String servletPath = req.getPathInfo();
            logger.warn("servletPath = " + servletPath);

            if ("/bin/rockstar/activity.json".equals(servletPath)) {
                String activity = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                String updatedActivity = adobeIoService.updateActivity(activity);
                resp.getWriter().write(updatedActivity);
            } else if ("/bin/rockstar/offerdetails.json".equals(servletPath)) {
                String offer = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                String updatedOffer = adobeIoService.updateOffer(offer);
                resp.getWriter().write(updatedOffer);
            }
        } catch (Exception e) {
            logger.error("Error calling Adobe IO PUT servlet " + req.getPathInfo(), e);
            resp.sendError(500, "{'error': 'Error calling Adobe IO PUT servlet " + req.getPathInfo() + "'}");
        }
    }

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");

        try {
            String servletPath = req.getPathInfo();

            if ("/bin/rockstar/activity.json".equals(servletPath)) {
                String activity = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                String createdOffer = adobeIoService.createActivity(activity);
                resp.getWriter().write(createdOffer);
            }
            else if ("/bin/rockstar/offerdetails.json".equals(servletPath)) {
                String offer = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                String createdOffer = adobeIoService.createOffer(offer);
                resp.getWriter().write(createdOffer);
            }
        } catch (Exception e) {
            logger.error("Error calling Adobe IO POST servlet " + req.getPathInfo(), e);
            resp.sendError(500, "{'error': 'Error calling Adobe IO POST servlet " + req.getPathInfo() + "'}");
        }
    }
}