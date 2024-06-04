// src/app/project.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = 'http://localhost:9000/projects';

  constructor(private http: HttpClient) { }

  getProject(id: string): Observable<any> {
    console.log(id);
    const url = `${this.apiUrl}/${id}`;
    console.log(url);
    return this.http.get<any>(url);
  }

  getProjects(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
}
