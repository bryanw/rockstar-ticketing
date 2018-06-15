import {BrowserModule} from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {AdobeIoService} from './shared/services/adobe-io.service';
import {SharedModule} from './shared/shared.module';
// import {OptionsModule} from './options/options.module';
import {RouterModule} from '@angular/router';
import {AuthGuard} from './auth.guard';
import { ExtensionComponent } from './extension/extension.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HttpModule} from "@angular/http";
import { NgDragDropModule } from "ng-drag-drop";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {YoutubeService} from "./shared/services/youtube.service";

const routes = [
    { path: '', redirectTo: 'extension', pathMatch: 'full' },
    { path: 'extension', component: ExtensionComponent },
    { path: '**', component: ExtensionComponent, canActivate: [AuthGuard] }
];

@NgModule({
    declarations: [
        AppComponent,
        ExtensionComponent
    ],
    imports: [
        BrowserModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        NgDragDropModule.forRoot(),
        NgbModule.forRoot(),
        // OptionsModule,
        RouterModule.forRoot(routes)
    ],
    providers: [
        AuthGuard,
        AdobeIoService,
        YoutubeService
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
