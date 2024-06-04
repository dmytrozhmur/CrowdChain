// src/app/donate.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DonateService {
  private apiUrl = 'http://localhost:9000/donate';

  constructor(private http: HttpClient) { }

  donate(projectId: string, donateData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${projectId}`, donateData);
  }
}
