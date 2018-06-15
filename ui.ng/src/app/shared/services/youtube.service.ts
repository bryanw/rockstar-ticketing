import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../../environments/environment";
import {Observable} from "rxjs/Observable";

@Injectable()
export class YoutubeService {

  apiUrl: string;
  apiKey: string;

  constructor(private http: HttpClient) {
    this.apiUrl = environment.youtubeSearchUrl;
    this.apiKey = environment.youtubeApiKey;
  }

  searchVideos(searchForm): Observable<any> {
    return searchForm.controls.searchInput.valueChanges
        .debounceTime(1000)
        .distinctUntilChanged()
        .switchMap(query => this.http.get(`${this.apiUrl}?q=${query}&key=${this.apiKey}&part=snippet&maxResults=40`))
        .map(res => res.items);
  }

}
