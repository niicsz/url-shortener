import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ShortenResponse } from '../models/short-url.model';
import { UrlStats } from '../models/url-stats.model';

@Injectable({ providedIn: 'root' })
export class UrlService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1';

  shorten(url: string): Observable<ShortenResponse> {
    return this.http.post<ShortenResponse>(`${this.baseUrl}/shorten`, { url });
  }

  getStats(shortCode: string): Observable<UrlStats> {
    return this.http.get<UrlStats>(`${this.baseUrl}/stats/${shortCode}`);
  }
}
