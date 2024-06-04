import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ProjectListComponent} from "./project-list/project-list.component";
import {ProjectDetailComponent} from "./project-detail/project-detail.component";
import {DonateDetailComponent} from "./donate-detail/donate-detail.component";
import {HomeComponent} from "./home/home.component";
import {DonateSuccessComponent} from "./donate-success/donate-success.component";

const routes: Routes = [
  { path: 'projects', component: ProjectListComponent },
  { path: 'projects/:id', component: ProjectDetailComponent },
  { path: 'projects/:id/donate', component: DonateDetailComponent },
  { path: '', component: HomeComponent },
  { path: 'thank-you', component: DonateSuccessComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
