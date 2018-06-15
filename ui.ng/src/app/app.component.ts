import {Component, OnInit} from '@angular/core';
import {AdobeIoService} from './shared/services/adobe-io.service';
import {ActivatedRoute, Params} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Rockstar Ticketing Activity Builder';
  constructor (
    private adobeIoService: AdobeIoService,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
      this.activatedRoute.params.subscribe((params: Params) => {

      });
  }
}
