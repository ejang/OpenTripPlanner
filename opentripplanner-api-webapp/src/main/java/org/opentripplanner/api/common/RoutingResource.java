package org.opentripplanner.api.common;

import java.util.List;
import java.util.Date;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.xml.datatype.DatatypeConfigurationException;

import org.opentripplanner.routing.core.OptimizeType;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.core.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines all the JAX-RS query parameters for a path search as fields, allowing them to 
 * be inherited by other REST resource classes (the trip planner and the Analyst WMS or tile 
 * resource). They will be properly included in API docs generated by Enunciate. This implies that
 * the concrete REST resource subclasses will be request-scoped rather than singleton-scoped.
 * 
 * @author abyrd
 */
public abstract class RoutingResource { 

    private static final Logger LOG = LoggerFactory.getLogger(RoutingResource.class);

    /* TODO do not specify @DefaultValues here, so all defaults are handled in one place */
    
    /** The start location -- either latitude, longitude pair in degrees or a Vertex
     *  label. For example, <code>40.714476,-74.005966</code> or
     *  <code>mtanyctsubway_A27_S</code>.  */
    @QueryParam("fromPlace") protected List<String> fromPlace;

    /** The end location (see fromPlace for format). */
    @QueryParam("toPlace") protected List<String> toPlace;

    /** An unordered list of intermediate locations to be visited (see the fromPlace for format). */
    @QueryParam("intermediatePlaces") protected List<String> intermediatePlaces;
    
    /** Whether or not the order of intermediate locations is to be respected (TSP vs series). */
    @DefaultValue("false") @QueryParam("intermediatePlacesOrdered") protected Boolean intermediatePlacesOrdered;
    
    /** The date that the trip should depart (or arrive, for requests where arriveBy is true). */
    @QueryParam("date") protected List<String> date;
    
    /** The time that the trip should depart (or arrive, for requests where arriveBy is true). */
    @QueryParam("time") protected List<String> time;
    
    /** Router ID used when in multiple graph mode. Unused in singleton graph mode. */
    @DefaultValue("") @QueryParam("routerId") protected List<String> routerId;
    
    /** Whether the trip should depart or arrive at the specified date and time. */
    @DefaultValue("false") @QueryParam("arriveBy") protected List<Boolean> arriveBy;
    
    /** Whether the trip must be wheelchair accessible. */
    @DefaultValue("false") @QueryParam("wheelchair") protected List<Boolean> wheelchair;

    /** The maximum distance (in meters) the user is willing to walk. Defaults to approximately 1/2 mile. */
    @DefaultValue("800") @QueryParam("maxWalkDistance") protected List<Double> maxWalkDistance;

    /** The user's walking speed in meters/second. Defaults to approximately 3 MPH. */
    @QueryParam("walkSpeed") protected List<Double> walkSpeed;

    /** For bike triangle routing, how much safety matters (range 0-1). */
    @QueryParam("triangleSafetyFactor") protected List<Double> triangleSafetyFactor;
    
    /** For bike triangle routing, how much slope matters (range 0-1). */
    @QueryParam("triangleSlopeFactor") protected List<Double> triangleSlopeFactor;
    
    /** For bike triangle routing, how much time matters (range 0-1). */            
    @QueryParam("triangleTimeFactor") protected List<Double> triangleTimeFactor;

    /** The set of characteristics that the user wants to optimize for. @See OptimizeType */
    @DefaultValue("QUICK") @QueryParam("optimize") protected List<OptimizeType> optimize;
    
    /** The set of modes that a user is willing to use. */
    @DefaultValue("TRANSIT,WALK") @QueryParam("mode") protected List<TraverseModeSet> modes;

    /** The minimum time, in seconds, between successive trips on different vehicles.
     *  This is designed to allow for imperfect schedule adherence.  This is a minimum;
     *  transfers over longer distances might use a longer time. */
    @DefaultValue("240") @QueryParam("minTransferTime") protected List<Integer> minTransferTime;

    /** The maximum number of possible itineraries to return. */
    @DefaultValue("3") @QueryParam("numItineraries") protected List<Integer> numItineraries;

    /** The list of preferred routes.  The format is agency_route, so TriMet_100. */
    @DefaultValue("") @QueryParam("preferredRoutes") protected List<String> preferredRoutes;
    
    /** The list of unpreferred routes.  The format is agency_route, so TriMet_100. */
    @DefaultValue("") @QueryParam("unpreferredRoutes") protected List<String> unpreferredRoutes;

    /** Whether intermediate stops -- those that the itinerary passes in a vehicle, but 
     *  does not board or alight at -- should be returned in the response.  For example,
     *  on a Q train trip from Prospect Park to DeKalb Avenue, whether 7th Avenue and
     *  Atlantic Avenue should be included. */
    @DefaultValue("false") @QueryParam("showIntermediateStops") protected List<Boolean> showIntermediateStops;

    /** The list of banned routes.  The format is agency_route, so TriMet_100. */
    @DefaultValue("") @QueryParam("bannedRoutes") protected List<String> bannedRoutes;

    /** An additional penalty added to boardings after the first.  The value is in OTP's
     *  internal weight units, which are roughly equivalent to seconds.  Set this to a high
     *  value to discourage transfers.  Of course, transfers that save significant
     *  time or walking will still be taken.*/
    @DefaultValue("0") @QueryParam("transferPenalty") protected List<Integer> transferPenalty;
    
    /** The maximum number of transfers (that is, one plus the maximum number of boardings)
     *  that a trip will be allowed.  Larger values will slow performance, but could give
     *  better routes.  This is limited on the server side by the MAX_TRANSFERS value in
     *  org.opentripplanner.api.ws.Planner. */
    @DefaultValue("2") @QueryParam("maxTransfers") protected List<Integer> maxTransfers;

    /** If true, goal direction is turned off and a full path tree is built (specify only once) */
    @DefaultValue("false") @QueryParam("batch") protected List<Boolean> batch;
    
    /** 
     * Build the 0th Request object from the query parameter lists. 
     * @throws ParameterException when there is a problem interpreting a query parameter
     */
    protected RoutingRequest buildRequest() throws ParameterException {
        return buildRequest(0);
    }
    
    /** 
     * Range/sanity check the query parameter fields and build a Request object from them.
     * @param  n allows building several request objects from the same query parameters, 
     *         re-specifying only those parameters that change from one request to the next. 
     * @throws ParameterException when there is a problem interpreting a query parameter
     */
    protected RoutingRequest buildRequest(int n) throws ParameterException {
        RoutingRequest request = new RoutingRequest();
        request.setRouterId(get(routerId, n, ""));
        request.setFrom(get(fromPlace, n, null));
        request.setTo(get(toPlace, n, null));
        {
            String d = get(date, n, null);
            String t = get(time, n, null);
            if (d == null && t != null) { 
                LOG.debug("parsing ISO datetime {}", t);
                try { // Full ISO date in time param ?
                    request.setDateTime(javax.xml.datatype.DatatypeFactory.newInstance()
                           .newXMLGregorianCalendar(t).toGregorianCalendar().getTime());
                } catch (DatatypeConfigurationException e) {
                    request.setDateTime(d, t);
                }
            } else {
                request.setDateTime(d, t);
            }
        }
        request.setWheelchair(get(wheelchair, n, false));
        request.setNumItineraries(get(numItineraries, n, 3));
        request.setMaxWalkDistance(get(maxWalkDistance, n, 840.0));
        request.setWalkSpeed(get(walkSpeed, n, 1.33));
        OptimizeType opt = get(optimize, n, OptimizeType.QUICK);
        {
            Double tsafe =  get(triangleSafetyFactor, n, null);
            Double tslope = get(triangleSlopeFactor,  n, null);
            Double ttime =  get(triangleTimeFactor,   n, null);
            if (tsafe != null || tslope != null || ttime != null ) {
                if (tsafe == null || tslope == null || ttime == null) {
                    throw new ParameterException(Message.UNDERSPECIFIED_TRIANGLE);
                }
                if (opt == null) {
                    opt = OptimizeType.TRIANGLE;
                } else if (opt != OptimizeType.TRIANGLE) {
                    throw new ParameterException(Message.TRIANGLE_OPTIMIZE_TYPE_NOT_SET);
                }
                if (Math.abs(tsafe + tslope + ttime - 1) > Math.ulp(1) * 3) {
                    throw new ParameterException(Message.TRIANGLE_NOT_AFFINE);
                }
                request.setTriangleSafetyFactor(tsafe);
                request.setTriangleSlopeFactor(tslope);
                request.setTriangleTimeFactor(ttime);
            } else if (opt == OptimizeType.TRIANGLE) {
                throw new ParameterException(Message.TRIANGLE_VALUES_NOT_SET);
            }
        }
        request.setArriveBy(get(arriveBy, n, false));
        request.setShowIntermediateStops(get(showIntermediateStops, n, false));
        /* intermediate places and their ordering are shared because they are themselves a list */
        if (intermediatePlaces != null && intermediatePlaces.size() > 0 
            && ! intermediatePlaces.get(0).equals("")) {
            request.setIntermediatePlaces(intermediatePlaces);
        }
        if (intermediatePlacesOrdered == null)
            intermediatePlacesOrdered = true;
        request.setIntermediatePlacesOrdered(intermediatePlacesOrdered);
        request.setPreferredRoutes(get(preferredRoutes, n, ""));
        request.setUnpreferredRoutes(get(unpreferredRoutes, n, ""));
        request.setBannedRoutes(get(bannedRoutes, n, ""));
        // replace deprecated optimization preference
        // opt has already been assigned above
        if (opt == OptimizeType.TRANSFERS) {
            opt = OptimizeType.QUICK;
            request.setTransferPenalty(get(transferPenalty, n, 0) + 1800);
        } else {
            request.setTransferPenalty(get(transferPenalty, n, 0));
        }
        request.setBatch(get(batch, n, new Boolean(false)));
        request.setOptimize(opt);
        request.setModes(get(modes, n, new TraverseModeSet("WALK,TRANSIT")));
        request.setMinTransferTime(get(minTransferTime, n, 0));
        request.setMaxTransfers(get(maxTransfers, n, 2));
        final long NOW_THRESHOLD_MILLIS = 15 * 60 * 60 * 1000;
        boolean tripPlannedForNow = Math.abs(request.getDateTime().getTime() - new Date().getTime()) 
                < NOW_THRESHOLD_MILLIS;
        request.setUseBikeRentalAvailabilityInformation(tripPlannedForNow);
        if (intermediatePlaces != null && intermediatePlacesOrdered && request.getModes().isTransit())
            throw new UnsupportedOperationException("TSP is not supported for transit trips");
        return request;
    }
    
/**
 * @param l list of query parameter values
 * @param n requested item index 
 * @return nth item if it exists, closest existing item otherwise, or defaultValue if the list l 
 *         is null or empty.
 */
    private <T> T get(List<T> l, int n, T defaultValue) {
        if (l == null || l.size() == 0)
            return defaultValue;
        int maxIndex = l.size() - 1;
        if (n > maxIndex)
            n = maxIndex;
        return l.get(n);
    }

}
