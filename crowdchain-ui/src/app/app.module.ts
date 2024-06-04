import { NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ProjectListComponent } from './project-list/project-list.component';
import { ProjectDetailComponent } from './project-detail/project-detail.component';
import { NavbarComponent } from './navbar/navbar.component';
import { MatProgressBarModule } from "@angular/material/progress-bar";
import {provideHttpClient, withInterceptorsFromDi} from "@angular/common/http";
import { DonateDetailComponent } from './donate-detail/donate-detail.component';
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatDatepickerModule} from "@angular/material/datepicker";

import {MatMomentDateModule, provideMomentDateAdapter} from '@angular/material-moment-adapter';
import {MatInputModule} from "@angular/material/input";
import {MatSliderModule} from "@angular/material/slider";
import {FlexLayoutServerModule} from "@angular/flex-layout/server";
import { HomeComponent } from './home/home.component';
import {MatToolbarModule} from "@angular/material/toolbar";
import { DonateSuccessComponent } from './donate-success/donate-success.component';
import {MatCardModule} from "@angular/material/card";
import {MatSidenav, MatSidenavContainer} from "@angular/material/sidenav";
import {MatNavList} from "@angular/material/list";

@NgModule({
  declarations: [
    AppComponent,
    ProjectListComponent,
    ProjectDetailComponent,
    NavbarComponent,
    DonateDetailComponent,
    HomeComponent,
    DonateSuccessComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    FlexLayoutModule,
    FormsModule,
    MatDatepickerModule,
    MatInputModule,
    MatMomentDateModule,
    MatSliderModule,
    FlexLayoutServerModule,
    MatToolbarModule,
    MatCardModule,
    MatSidenavContainer,
    MatNavList,
    MatSidenav
  ],
  providers: [
    provideClientHydration(),
    provideAnimationsAsync(),
    provideHttpClient(withInterceptorsFromDi())
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
