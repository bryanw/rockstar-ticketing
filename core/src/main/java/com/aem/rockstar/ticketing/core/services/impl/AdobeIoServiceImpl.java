package com.aem.rockstar.ticketing.core.services.impl;

import com.aem.rockstar.ticketing.core.exceptions.RockstarException;
import com.aem.rockstar.ticketing.core.services.AdobeIoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.sling.commons.html.HtmlParser;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@Component(service = AdobeIoService.class, configurationPolicy= ConfigurationPolicy.REQUIRE)
@Designate(ocd=RockstarTicketingConfigService.class)
public class AdobeIoServiceImpl implements AdobeIoService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_AUDIENCE_ID = 0;
    //TODO: Change to Adobe IO to version 2 with local offers if time permits
    private static final String CONTENT_TYPE_TARGET_JSON_V1 = "application/vnd.adobe.target.v1+json";
    private static final String CONTENT_TYPE_FORM = ContentType.APPLICATION_FORM_URLENCODED.withCharset(Consts.UTF_8).toString();
    private static final String CONTENT_TYPE_APPLICATION_JSON = ContentType.APPLICATION_JSON.toString();
    private static final String UTF_8 = Consts.UTF_8.name();
    private static final String X_API_KEY = "X-Api-Key";
    private static final String ADOBE_IO_DOMAIN = "https://mc.adobe.io/";

    @Reference
    private HtmlParser htmlParser;

    private RockstarTicketingConfigService config;
    private String bearerToken;

    @Override
    public String getActivities() {

        String activities;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            HttpGet httpGet = new HttpGet(ADOBE_IO_DOMAIN + tenant + "/target/activities?limit=100");
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpGet.addHeader(X_API_KEY, apiKey);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            activities = httpclient.execute(httpGet, responseHandler);

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode activitiesJson = mapper.readTree(activities);

            Iterator<JsonNode> activitiesIterator = activitiesJson.get("activities").elements();
            while (activitiesIterator.hasNext()) {
                JsonNode activityJson = activitiesIterator.next();
                String activityName = activityJson.get("name").asText();
                // TODO: This can be removed
                if (/*!activityName.startsWith("Bangarang") && */!activityName.startsWith("Rockstar")) {
                    activitiesIterator.remove();
                }
            }

            activities = activitiesJson.toString();

            //TODO: Add logic to refetch Bearer token if expired
            //TODO: Filter out AB activities
        } catch (Exception e) {
            LOG.error("Error getting activities.", e);
            throw new RockstarException("There was an error getting the activities.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return activities;
    }

    @Override
    public String getActivity(final String activityId) {
        String activity;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            HttpGet httpGet = new HttpGet(ADOBE_IO_DOMAIN + tenant + "/target/activities/xt/" + activityId);
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpGet.addHeader(X_API_KEY, apiKey);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            activity = httpclient.execute(httpGet, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode activityJson = mapper.readTree(activity);

            //List result = StreamSupport.stream(activityJson.spliterator(), false).filter(item -> item.get("foo").asText() != "bar3").collect(Collectors.toList());

            activityJson = transformActivityForClient(activityJson);

            activity = activityJson.toString();

        } catch (Exception e) {
            LOG.error("Error getting activity: " + activityId, e);
            throw new RockstarException("There was an error getting the activity.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return activity;
    }

    @Override
    public String getAudiences() {
        String audiences;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            HttpGet httpGet = new HttpGet(ADOBE_IO_DOMAIN + tenant + "/target/audiences");
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpGet.addHeader(X_API_KEY, apiKey);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            audiences = httpclient.execute(httpGet, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired

            // Add All Visitors audience
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode audiencesJson = mapper.readTree(audiences);

            // Add a default audience representing All Visitors
            ObjectNode allVisitorsAudienceJson = mapper.createObjectNode();
            allVisitorsAudienceJson.put("id", DEFAULT_AUDIENCE_ID);
            allVisitorsAudienceJson.put("name", "All Visitors");
            allVisitorsAudienceJson.put("description", "This audience covers all visitors and should be used as a default catch all.");

            ArrayNode audiencesArray = (ArrayNode)audiencesJson.get("audiences");
            audiencesArray.add(allVisitorsAudienceJson);

            audiences = audiencesJson.toString();
        } catch (Exception e) {
            LOG.error("Error getting audiences." , e);
            throw new RockstarException("There was an error getting the list of audiences.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return audiences;
    }

    @Override
    public String getAudience(final String audienceId) {
        String audience;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            HttpGet httpGet = new HttpGet(ADOBE_IO_DOMAIN + tenant + "/target/audiences/" + audienceId);
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpGet.addHeader(X_API_KEY, apiKey);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            audience = httpclient.execute(httpGet, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired
        } catch (Exception e) {
            LOG.error("Error getting audience: " + audienceId, e);
            throw new RockstarException("There was an error getting the audience.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return audience;
    }

    @Override
    public String getOfferDetails(final String offerId) {
        String offerDetails;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            HttpGet httpGet = new HttpGet(ADOBE_IO_DOMAIN + tenant + "/target/offers/content/" + offerId);
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpGet.addHeader(X_API_KEY, apiKey);
            httpGet.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            offerDetails = httpclient.execute(httpGet, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired
        } catch (Exception e) {
            LOG.error("Error getting offer: " + offerId, e);
            throw new RockstarException("There was an error getting the offer.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return offerDetails;
    }

    @Override
    public String createActivity(final String activity) {
        String createdActivity;

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode activityJson = mapper.readTree(activity);

            transformActivityForTarget(activityJson);

            HttpPost httpPost = new HttpPost(ADOBE_IO_DOMAIN + tenant + "/target/activities/xt/");
            httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpPost.addHeader(X_API_KEY, apiKey);
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            String activityJsonString = activityJson.toString();
            StringEntity body = new StringEntity(activityJsonString);
            httpPost.setEntity(body);
            createdActivity = httpclient.execute(httpPost, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired
        } catch (Exception e) {
            LOG.error("Error creating activity.", e);
            throw new RockstarException("Error creating activity.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return createdActivity;
    }

    @Override
    public String updateActivity(final String activity) {
        String updatedActivity;

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode activityJson = mapper.readTree(activity);

            String activityId = activityJson.get("id").asText();

            activityJson = transformActivityForTarget(activityJson);

            HttpPut httpPut = new HttpPut(ADOBE_IO_DOMAIN + tenant + "/target/activities/xt/" + activityId);
            httpPut.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpPut.addHeader(X_API_KEY, apiKey);
            httpPut.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            ResponseHandler<String> responseHandler = getDefaultResponseHandler();

            StringEntity body = new StringEntity(activityJson.toString());
            httpPut.setEntity(body);
            updatedActivity = httpclient.execute(httpPut, responseHandler);
            //TODO: Add logic to refetch Bearer token if expired
        } catch (Exception e) {
            LOG.error("Error updating activity: " + activity, e);
            throw new RockstarException("Error updating activity.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return updatedActivity;
    }

    @Override
    public String createOffer(final String offer) {
        String createdOffer;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = getDefaultResponseHandler();

        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode offerJson = mapper.readTree(offer);

            ObjectNode createdOfferJson = mapper.createObjectNode();

            String offerVideoId = offerJson.get("videoId").asText();
            String offerHeader = offerJson.get("header").asText();
            String offerHref = offerJson.get("href").asText();

            createdOfferJson.put("name", "Rockstar Offer " + offerVideoId);
            createdOfferJson.put("content", getOfferMarkup(offerVideoId, offerHeader, offerHref));

            HttpPost httpPost = new HttpPost(ADOBE_IO_DOMAIN + tenant + "/target/offers/content/");
            httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpPost.addHeader(X_API_KEY, apiKey);
            httpPost.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            String createdOfferAsString = createdOfferJson.toString();
            StringEntity body = new StringEntity(createdOfferAsString);
            httpPost.setEntity(body);
            createdOffer = httpclient.execute(httpPost, responseHandler);

        } catch (Exception e) {
            LOG.error("Error creating offer.", e);
            throw new RockstarException("Error creating offer.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return createdOffer;
    }

    @Override
    public String updateOffer(final String offer) {
        String updatedOffer;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        ResponseHandler<String> responseHandler = getDefaultResponseHandler();

        try {
            String bearerToken = getBearerToken();
            String apiKey = this.config.getApiKey();
            String tenant = this.config.getTenant();

            final ObjectMapper mapper = new ObjectMapper();
            JsonNode offerJson = mapper.readTree(offer);

            ObjectNode updatedOfferJson = mapper.createObjectNode();

            String offerVideoId = offerJson.get("videoId").asText();
            String offerHeader = offerJson.get("header").asText();
            String offerHref = offerJson.get("href").asText();

            updatedOfferJson.put("name", "Rockstar Offer " + offerVideoId);
            updatedOfferJson.put("content", getOfferMarkup(offerVideoId, offerHeader, offerHref));

            Integer offerId = offerJson.get("id").asInt(0);
            HttpPut httpPut = new HttpPut(ADOBE_IO_DOMAIN + tenant + "/target/offers/content/" + offerId);
            httpPut.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            httpPut.addHeader(X_API_KEY, apiKey);
            httpPut.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_TARGET_JSON_V1);

            StringEntity body = new StringEntity(updatedOfferJson.toString());
            httpPut.setEntity(body);
            updatedOffer = httpclient.execute(httpPut, responseHandler);

        } catch (Exception e) {
            LOG.error("Error updating offer.", e);
            throw new RuntimeException("Error updating offer.", e);
        } finally {
            try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
        }

        return updatedOffer;
    }

    private String getBearerToken() {

        if (bearerToken == null || bearerToken.trim().length() == 0) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                String clientId = this.config.getApiKey();
                String clientSecret = this.config.getClientSecret();
                String jwtToken = generateJwt();

                String imsHost = config.getImsHost();
                HttpPost httpPost = new HttpPost("https://" + imsHost + "/ims/exchange/v1/jwt");
                httpPost.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_FORM);
                httpPost.addHeader(HttpHeaders.ACCEPT, CONTENT_TYPE_APPLICATION_JSON);

                StringEntity body = new StringEntity("client_id=" + clientId + "&client_secret=" + clientSecret + "&jwt_token=" + jwtToken);
                httpPost.setEntity(body);

                ResponseHandler<String> responseHandler = getDefaultResponseHandler();
                String response = httpclient.execute(httpPost, responseHandler);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode bearerTokenJson = mapper.readTree(response);
                bearerToken = bearerTokenJson.get("access_token").textValue();

            } catch(Exception e) {
                LOG.error("Error getting bearer token.", e);
                throw new RuntimeException("Error getting bearer token.", e);
            } finally {
                try { httpclient.close(); } catch (Exception e) { LOG.warn("Error closing HTTP client", e);}
            }
        }

        return bearerToken;
    }

    private String generateJwt()  {
        String jwtToken;

        try {
            // API key information (substitute actual credential values)
            String orgId = this.config.getOrganizationId();
            String technicalAccountId = this.config.getTechnicalAccountId();
            String apiKey = this.config.getApiKey();
            String secretKeyFilePath = this.config.getPrivateKeyFile();

            // Expiration time in seconds
            Long expirationTime = (new Date().getTime() / 1000) + 7 * 24 * 60 * 60;  // One week from now

            // Secret key as byte array. Secret key file should be in DER encoded format.
            byte[] privateKeyFileContent = Files.readAllBytes(Paths.get(secretKeyFilePath));

            String imsHost = config.getImsHost();

            // Create the private key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec ks = new PKCS8EncodedKeySpec(privateKeyFileContent);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

            Map<String, Object> jwtClaims = ImmutableMap.<String, Object>builder()
                    .put("iss", orgId)
                    .put("sub", technicalAccountId)
                    .put("exp", expirationTime)
                    .put("aud", String.format("https://%s/c/%s", imsHost, apiKey))
                    .put(String.format("https://%s/s/%s", imsHost, "ent_marketing_sdk"), true)
                    .build();

            // Create the final JWT token
            jwtToken = Jwts.builder().setClaims(jwtClaims).signWith(SignatureAlgorithm.RS256, privateKey).compact();
        }
        catch (NoSuchAlgorithmException e) {
            LOG.error("Error generating JWT token.", e);
            throw new RockstarException("There was an error generating the JWT token.  Please contact your administrator.", e);
        } catch (IOException e) {
            LOG.error("Error generating JWT token.", e);
            throw new RockstarException("There was an IO error while generating the JWT token.  Please contact your administrator.", e);
        } catch (InvalidKeySpecException e) {
            LOG.error("Error generating JWT token.", e);
            throw new RockstarException("There was an invalid key error generating the JWT token.  Please contact your administrator.", e);
        }

        return jwtToken;
    }

    @Activate
    public void activate(final RockstarTicketingConfigService config) {
        this.config = config;
    }

    private ResponseHandler<String> getDefaultResponseHandler() {
        return new ResponseHandler<String>() {

            @Override
            public String handleResponse(
                    final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    LOG.error("Error response reason : " + response.getStatusLine().getReasonPhrase());
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
    }

    private JsonNode transformActivityForTarget(final JsonNode activityJson) {

        ((ObjectNode) activityJson).remove("id");

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode experiencesJson = activityJson.get("experiences");
        experiencesJson.forEach(experienceJson -> {
            try {
                final ArrayNode audienceArrayNode = mapper.createArrayNode();

                final int audienceId = experienceJson.get("audience").get("id").asInt();
                if (audienceId != DEFAULT_AUDIENCE_ID) {
                    audienceArrayNode.add(audienceId);
                    ((ObjectNode) experienceJson).set("audienceIds", audienceArrayNode);
                }

                // Ensure offerLocations has at least one element
                JsonNode offerDetailsNode = experienceJson.get("offerDetails");
                if (experienceJson.get("offerLocations").size() == 0) {
                    ObjectNode newOfferDetailsNode = mapper.createObjectNode();
                    ((ArrayNode) experienceJson.get("offerLocations")).add(newOfferDetailsNode);
                }

                //TODO: Update to support multiple offers per experience
                ((ObjectNode) experienceJson.get("offerLocations").get(0)).put("locationLocalId", 0);
                ((ObjectNode) experienceJson.get("offerLocations").get(0)).put("offerId", offerDetailsNode.get("id").asInt());

                ((ObjectNode) experienceJson).remove("audience");
                ((ObjectNode) experienceJson).remove("offerDetails");
            } catch (Exception e) {
                LOG.error("Error transforming Activity for Target.", e);
            }
        });

        return activityJson;
    }

    private JsonNode transformActivityForClient(JsonNode activityJson) {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode experiencesJson = activityJson.get("experiences");
        //TODO: Batch calls for audience and offer details into one request
        experiencesJson.forEach(experienceJson -> {
            try {
                if (experienceJson.has("audienceIds")) {
                    final String audienceId = experienceJson.get("audienceIds").get(0).asText();

                    final String audience = this.getAudience(audienceId);
                    final JsonNode audienceJson = mapper.readTree(audience);
                    ((ObjectNode) experienceJson).set("audience", audienceJson);
                }
                else {
                    final ObjectNode audienceJson = mapper.createObjectNode();
                    audienceJson.put("id", DEFAULT_AUDIENCE_ID);
                    audienceJson.put("name", "All Visitors");
                    audienceJson.put("description", "This audience covers all visitors and should be used as a default catch all.");
                    ((ObjectNode) experienceJson).set("audience", audienceJson);
                }

                final String offerId = experienceJson.get("offerLocations").get(0).get("offerId").asText();
                if (!Objects.equals(offerId, "0")) {
                    final String offer = this.getOfferDetails(offerId);

                    final JsonNode offerJson = mapper.readTree(offer);

                    final String content = offerJson.get("content").asText();
                    if (!Strings.isNullOrEmpty(content)) {
                        final Document doc = htmlParser.parse(null, IOUtils.toInputStream(content, UTF_8), UTF_8);
                        //final Element offerDiv = doc.getElementById("offer-" + offerId);
                        final Element offerDiv = (Element) doc.getElementsByTagName("div").item(0);
                        final String offerVideoId = offerDiv.getAttribute("data-offer-video-id");
                        final String offerHeader = offerDiv.getAttribute("data-offer-header");
                        final String offerHref = offerDiv.getAttribute("data-offer-href");
                        ((ObjectNode) offerJson).put("videoId", offerVideoId);
                        ((ObjectNode) offerJson).put("header", offerHeader);
                        ((ObjectNode) offerJson).put("href", offerHref);

                        ((ObjectNode) experienceJson).set("offerDetails", offerJson);
                    }
                } else {
                    ((ObjectNode) experienceJson).set("offerDetails", mapper.createObjectNode());
                }

            } catch (Exception e) {
                LOG.error("Error transforming Activity for client.", e);
            }
        });

        return activityJson;
    }

    private void transformOfferForTarget(JsonNode offerJson) {

    }

    private void transformOfferForClient(JsonNode offerJson) {

    }

    private String getOfferMarkup(final String offerVideoId, final String offerHeader, final String offerHref) {
        final String offerId = "offer-" + offerVideoId;

        /*
        String offerHtml = String.format(
            "<div id=\"%1$s\" data-offer-video-id=\"%2$s\" data-offer-header=\"%3$s\" data-offer-href=\"%4$s\" class=\"offer-container\">\n" +
            "    <h2 id=\"%1$s-header\">%3$s</h2>\n" +
            "    <a id=\"%1$s-link\" href=\"%4$s\">Details here</a>\n" +
            "    <div id=\"%1$s-player\"></div>\n" +
            "    <script type=\"text/javascript\">\n" +
            "        console.log('Checkpoint 1');\n" +
            "        let offerId = '%1$s';\n" +
            "        let offerData = document.getElementById(offerId).dataset;\n" +
            "        console.log('Checkpoint 2');\n" +
            "\n" +
            "        var playerDiv = document.getElementById(offerId + '-player');\n" +
                    "        console.log('Checkpoint 3');\n" +
            "        var player = document.createElement(\"IFRAME\");\n" +
                    "        console.log('Checkpoint 4');\n" +
            "        var offerVideoUrl = \"https://www.youtube.com/embed/\" + offerData.offerVideoId + \"?autoplay=1&amp;rel=0&amp;controls=0&amp;showinfo=0\";\n" +
                    "        console.log('Checkpoint 5');\n" +
            "        player.setAttribute('width', '640');\n" +
            "        player.setAttribute('height', '390');\n" +
            "        player.setAttribute('src', offerVideoUrl);\n" +
                    "        console.log('Checkpoint 6');\n" +
            "        playerDiv.appendChild(player);\n" +
                    "        console.log('Checkpoint 7');\n" +
            "    </script>\n" +

            "</div>", offerId, offerVideoId, offerHeader, offerHref);
        */

        String offerHtml = String.format(
                "<div id=\"%1$s\" data-offer-video-id=\"%2$s\" data-offer-header=\"%3$s\" data-offer-href=\"%4$s\" class=\"offer-container\">\n" +
                "    <h2 id=\"%1$s-header\">%3$s</h2>\n" +
                "    <iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/%2$s?autoplay=1&showinfo=0\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>\n" +
                "</div>", offerId, offerVideoId, offerHeader, offerHref);

        LOG.warn("offerHtml = \n" + offerHtml);

        return offerHtml;
    }
}
