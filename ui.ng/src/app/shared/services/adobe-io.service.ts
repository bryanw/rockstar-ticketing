import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Activity} from '../models/activity.model';
import {mergeMap, tap} from 'rxjs/operators';
import {Observable} from 'rxjs/Observable';
import {ActivityList} from "../models/activity-list.model";
import {Offer} from "../models/offer.model";
import {AudienceList} from "../models/audience-list.model";
import {Audience} from "../models/audience.model";
import {OfferList} from "../models/offer-list.model";


export const DEFAULT_HEADERS_CONFIG = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
};

@Injectable()
export class AdobeIoService {

    constructor(private http: HttpClient) {
    }

    getActivities(): Observable<ActivityList> {
        const activityList = this.adobeIoGet<ActivityList>('/bin/rockstar/activities',
            new HttpParams());
        activityList.pipe(
            tap(a => console.log('activityList = ', a))
        );

        return activityList;
    }

    getActivity(activityId): Observable<Activity> {
        const activity = this.adobeIoGet<Activity>('/bin/rockstar/activity.json?activityId=' + activityId,
            new HttpParams());
        activity.pipe(
            tap(a => console.log('activity = ', a))
        );

        return activity;
    }

    getAudiences(): Observable<AudienceList> {
        const audiences = this.adobeIoGet<AudienceList>('/bin/rockstar/audiences',
            new HttpParams());
        audiences.pipe(
            tap(a => console.log('audiences = ', a))
        );

        return audiences;
    }

    getAudience(audienceId): Observable<Audience> {
        const audience = this.adobeIoGet<Audience>('/bin/rockstar/audiences?audienceId=' + audienceId,
            new HttpParams());
        audience.pipe(
            tap(a => console.log('audience = ', a))
        );

        return audience;
    }

    getOffers(): Observable<OfferList> {
        const offers = this.adobeIoGet<OfferList>('/bin/rockstar/offers',
            new HttpParams());
        offers.pipe(
            tap(a => console.log('offers = ', a))
        );

        return offers;
    }

    getOfferDetails(offerId): Observable<Offer> {
        const offerDetails = this.adobeIoGet<Offer>('/bin/rockstar/offerdetails?offerId=' + offerId,
            new HttpParams());
        offerDetails.pipe(
            tap(a => console.log('activity = ', a))
        );

        return offerDetails;
    }

    createOrUpdateActivity(activity) {
        if (activity.id > 0) {
            return this.updateActivity(activity);
        } else {
            return this.createActivity(activity);
        }
    }

    createActivity(activity): Observable<Activity> {
        return this.adobeIoPost<Activity>('/bin/rockstar/activity.json', activity);
    }

    updateActivity(activity): Observable<Activity> {
        return this.adobeIoPut<Activity>('/bin/rockstar/activity.json', activity);
    }

    createOffer(offer): Observable<Offer> {
        return this.adobeIoPost<Offer>('/bin/rockstar/offerdetails.json', offer);
    }


    adobeIoGet<T>(path: string, params: HttpParams = new HttpParams()): Observable<T> {

        return this.http.get<T>(path,
                        {headers: new HttpHeaders(DEFAULT_HEADERS_CONFIG), params: params});
    }

    adobeIoPut<T>(path: string, body: string): Observable<T> {
        return this.http.put<T>(path, body,
            {headers: new HttpHeaders(DEFAULT_HEADERS_CONFIG)});
    }

    adobeIoPost<T>(path: string, body: string): Observable<T> {
        return this.http.post<T>(path, body,
            {headers: new HttpHeaders(DEFAULT_HEADERS_CONFIG)});
    }

    // TODO: Find out a better way to generate this
    getNextAvailableExperienceLocalId(activity: Activity): number {
        return Math.floor(Math.random() * Math.floor(10000));

        // The code below won't work because you can get this error
        // 'experienceLocalId previously used in removed experiences not allowed'.
        /*
        let nextAvailableExperienceLocalId = 1;

        let takenExperienceLocalIds = activity.experiences.map(function(experience) {
            return +experience.experienceLocalId;  // The + is to convert to number
        }).sort();

        while(takenExperienceLocalIds.some(function(id) { return nextAvailableExperienceLocalId === id })) {
            nextAvailableExperienceLocalId++;
        }

        return nextAvailableExperienceLocalId
        */
    }
}
