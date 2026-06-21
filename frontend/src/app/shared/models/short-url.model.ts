export interface ShortenResponse {
  shortCode: string;
  shortUrl: string;
}

export interface RecentLink {
  shortCode: string;
  shortUrl: string;
  longUrl: string;
  createdAt: string;
}
