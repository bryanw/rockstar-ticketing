import {Component, OnInit} from '@angular/core';
import {Activity, AutoAllocateTraffic, Locations} from '../shared/models/activity.model';
import {AdobeIoService} from '../shared/services/adobe-io.service';
import {Experience} from "../shared/models/experience.model";
import {ActivityList} from "../shared/models/activity-list.model";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Observable} from "rxjs/Observable";
import {Http} from "@angular/http";
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import {Audience} from "../shared/models/audience.model";
import {AudienceList} from "../shared/models/audience-list.model";
import {Offer} from "../shared/models/offer.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {DomSanitizer} from "@angular/platform-browser";
import {YoutubeService} from "../shared/services/youtube.service";

@Component({
    selector: 'app-extension',
    templateUrl: './extension.component.html',
    styleUrls: ['./extension.component.css'],
    providers: [FormBuilder]
})
export class ExtensionComponent implements OnInit {

    // selectedActivity: Activity = new Activity(1, '1', 'd', '', 1);
    selectedActivity: Activity = new Activity();
    activityList = new ActivityList();
    droppedItems = [];
    audienceList = new AudienceList();
    waitingForActivity = false;
    activityTableLoaded = false;
    videoPreviewId: string;
    videoPreviewUrl;

    searchForm: FormGroup;
    //createActivityForm: FormGroup;

    youtubeResults: Observable<any>;

    //TODO: Replace Http with HttpClient and move call to service
    constructor(private adobeIoService: AdobeIoService, private youtubeService: YoutubeService, private _formBuilder: FormBuilder, private _http: Http, private modalService: NgbModal, private domSanitizer : DomSanitizer) {
        //TODO: Move these to environment.ts
        const API_URL = 'https://www.googleapis.com/youtube/v3/search';
        const API_KEY = 'AIzaSyC53fDmDtBXhQc_W-7Qi8aBZDKLoc_v_Dg';
        this.searchForm = this._formBuilder.group({
            'searchInput': ''
        });
        this.youtubeResults = this.youtubeService.searchVideos(this.searchForm);
    }

    ngOnInit() {
        this.adobeIoService.getActivities().subscribe(
            data => {
                this.activityList = { ...data };
                this.selectedActivity = this.activityList.activities[0];
                this.onActivitySelect(this.activityList.activities[0].id)
            }
        );

        this.adobeIoService.getAudiences().subscribe(
            data => this.audienceList = { ...data }
        )
    }

    onActivitySelect(activityId) {

        if (activityId > 0) {
            this.waitingForActivity = true;
            this.activityTableLoaded = false;
            this.adobeIoService.getActivity(activityId).subscribe(
                data => {
                    this.selectedActivity = {...data};
                    this.waitingForActivity = false;
                    this.activityTableLoaded = true;
                }
            )
        } else {
            this.activityTableLoaded = true;
        }
    }

    createActivity(activityName) {
        let newActivity = new Activity();
        newActivity.name = activityName;

        this.activityList.activities.push(newActivity);
        this.selectedActivity = newActivity;
        this.activityTableLoaded = true;
    }

    addExperience() {
        //TODO: Find highest number not already used.  This won't work if an experience was deleted.
        const newId = String(this.adobeIoService.getNextAvailableExperienceLocalId(this.selectedActivity));
        this.selectedActivity.experiences.push(new Experience(newId));
    }

    removeExperience(experienceId) {
        this.selectedActivity.experiences = this.selectedActivity.experiences.filter(experience => experience.experienceLocalId !== experienceId);
    }

    saveChanges() {
        //TODO: Need to write POST and PUT servlets.  Remember to strip All Visitors audiences from experience.
        console.log("Saving changes...");
        if (this.selectedActivity.id > 0) {
            this.adobeIoService.updateActivity(this.selectedActivity).subscribe(
                data => {}
            );
        } else {
            this.adobeIoService.createActivity(this.selectedActivity).subscribe(
                data => {}
            );
        }
        console.log("Done saving changes");
    }

    onThumbnailDrop(e: any, experienceId) {
        console.log("Drop event = " + JSON.stringify(e));
        console.log("experienceId = " + experienceId);

        let that = this;
        this.selectedActivity.experiences.forEach(function(experience) {
            if (experience.experienceLocalId === experienceId) {
                experience.offerDetails.videoId = e.dragData.id.videoId;

                //TODO: Check if offer already exists
                that.adobeIoService.createOffer(experience.offerDetails).subscribe(
                    data => {
                        experience.offerDetails.id = data.id;
                    }
                );

            }
        }, this);
        //this.droppedItems.push(e.dragData);
    }

    compareAudiences(a1: Audience, a2: Audience): boolean {
        return a1 && a2 ? a1.id === a2.id : a1 === a2;
    }

    previewVideo(content, videoId) {
        this.videoPreviewId = videoId;
        this.videoPreviewUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(`https://www.youtube.com/embed/${videoId}?autoplay=1&showinfo=0`);
        const modalRef = this.modalService.open(content, {centered: true, size: "lg"}).result.then((result) => {
            console.log(`Closed with: ${result}`);
        }, (reason) => {
            console.log(`Dismissed ${reason}`);
        });

    }

}
