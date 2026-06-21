import { Injectable } from '@angular/core';
import { RecentLink } from '../models/short-url.model';

@Injectable({ providedIn: 'root' })
export class RecentLinksService {
  private readonly storageKey = 'binitech.shortener.recent';
  private readonly limit = 50;

  list(): RecentLink[] {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return [];
    }
    try {
      const parsed = JSON.parse(raw) as RecentLink[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  add(link: RecentLink): RecentLink[] {
    const existing = this.list().filter((item) => item.shortCode !== link.shortCode);
    const updated = [link, ...existing].slice(0, this.limit);
    localStorage.setItem(this.storageKey, JSON.stringify(updated));
    return updated;
  }

  remove(shortCode: string): RecentLink[] {
    const updated = this.list().filter((item) => item.shortCode !== shortCode);
    localStorage.setItem(this.storageKey, JSON.stringify(updated));
    return updated;
  }

  clear(): void {
    localStorage.removeItem(this.storageKey);
  }
}
