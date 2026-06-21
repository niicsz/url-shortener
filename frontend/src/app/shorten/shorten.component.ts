import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UrlService } from '../shared/services/url.service';
import { RecentLinksService } from '../shared/services/recent-links.service';
import { RecentLink, ShortenResponse } from '../shared/models/short-url.model';

@Component({
  selector: 'app-shorten',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    DatePipe,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule
  ],
  template: `
    <section class="page">
      <header class="hero">
        <h1>Encurte qualquer URL em <span>Base62</span>.</h1>
        <p class="muted lead">
          Códigos de 7 caracteres gerados por um contador atômico no Redis e ofuscados com Hashids,
          persistidos no Cassandra e servidos com cache-aside.
        </p>
      </header>

      <div class="surface composer">
        <mat-form-field appearance="outline" class="url-field">
          <mat-label>URL longa</mat-label>
          <mat-icon matPrefix>link</mat-icon>
          <input
            matInput
            placeholder="https://exemplo.com/uma/url/bem/comprida"
            [ngModel]="longUrl()"
            (ngModelChange)="longUrl.set($event)"
            (keyup.enter)="submit()"
            [disabled]="loading()"
          />
        </mat-form-field>
        <button mat-flat-button color="primary" class="submit" (click)="submit()" [disabled]="loading()">
          <mat-icon>bolt</mat-icon>
          Encurtar
        </button>
      </div>

      @if (loading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      @if (result(); as current) {
        <div class="surface result">
          <div class="result-head">
            <mat-icon>check_circle</mat-icon>
            <span>Link pronto</span>
          </div>
          <a class="short mono" [href]="current.shortUrl" target="_blank" rel="noopener">{{ current.shortUrl }}</a>
          <div class="result-actions">
            <button mat-stroked-button (click)="copy(current.shortUrl)">
              <mat-icon>content_copy</mat-icon>
              Copiar
            </button>
            <a mat-stroked-button [href]="current.shortUrl" target="_blank" rel="noopener">
              <mat-icon>open_in_new</mat-icon>
              Abrir
            </a>
          </div>
        </div>
      }

      @if (recent().length > 0) {
        <div class="recent">
          <div class="recent-head">
            <h2>Recentes</h2>
            <a routerLink="/painel">Ver painel de cliques</a>
          </div>
          @for (link of recent(); track link.shortCode) {
            <div class="surface row">
              <div class="row-main">
                <a class="short mono" [href]="link.shortUrl" target="_blank" rel="noopener">{{ link.shortUrl }}</a>
                <span class="muted origin">{{ link.longUrl }}</span>
                <span class="muted when">{{ link.createdAt | date: 'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <div class="row-actions">
                <button mat-icon-button matTooltip="Copiar" (click)="copy(link.shortUrl)">
                  <mat-icon>content_copy</mat-icon>
                </button>
                <button mat-icon-button (click)="remove(link.shortCode)">
                  <mat-icon>close</mat-icon>
                </button>
              </div>
            </div>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .hero {
      text-align: center;
      margin: 18px 0 28px;
    }
    .hero h1 {
      font-size: clamp(1.9rem, 4vw, 2.9rem);
      line-height: 1.05;
      margin: 0 0 12px;
      font-weight: 800;
    }
    .hero h1 span {
      color: var(--brand);
    }
    .lead {
      max-width: 620px;
      margin: 0 auto;
    }
    .composer {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      padding: 16px;
    }
    .url-field {
      flex: 1 1 auto;
    }
    .submit {
      height: 56px;
      padding: 0 22px;
    }
    .result {
      margin-top: 16px;
      padding: 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .result-head {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #1b8a4b;
    }
    .short {
      font-size: 1.15rem;
      font-weight: 600;
      word-break: break-all;
    }
    .result-actions {
      display: flex;
      gap: 10px;
    }
    .recent {
      margin-top: 32px;
    }
    .recent-head {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      margin-bottom: 12px;
    }
    .recent-head h2 {
      font-size: 1.1rem;
      margin: 0;
    }
    .row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 12px 14px;
      margin-bottom: 10px;
    }
    .row-main {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;
    }
    .origin,
    .when {
      font-size: 0.82rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 540px;
    }
    .row-actions {
      display: flex;
      flex-shrink: 0;
    }
    @media (max-width: 640px) {
      .composer {
        flex-direction: column;
      }
      .submit {
        width: 100%;
      }
    }
  `]
})
export class ShortenComponent {
  private readonly urlService = inject(UrlService);
  private readonly recentLinks = inject(RecentLinksService);
  private readonly snackBar = inject(MatSnackBar);

  readonly longUrl = signal('');
  readonly loading = signal(false);
  readonly result = signal<ShortenResponse | null>(null);
  readonly recent = signal<RecentLink[]>(this.recentLinks.list());

  submit(): void {
    const url = this.longUrl().trim();
    if (!url) {
      this.snackBar.open('Informe uma URL para encurtar.', 'OK', { duration: 3000 });
      return;
    }
    this.loading.set(true);
    this.urlService.shorten(url).subscribe({
      next: (response) => {
        this.result.set(response);
        const link: RecentLink = {
          shortCode: response.shortCode,
          shortUrl: response.shortUrl,
          longUrl: url,
          createdAt: new Date().toISOString()
        };
        this.recent.set(this.recentLinks.add(link));
        this.longUrl.set('');
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        const message = err?.error?.message ?? 'Não foi possível encurtar a URL.';
        this.snackBar.open(message, 'OK', { duration: 4000 });
      }
    });
  }

  copy(value: string): void {
    navigator.clipboard.writeText(value).then(() => {
      this.snackBar.open('Link copiado!', 'OK', { duration: 2000 });
    });
  }

  remove(shortCode: string): void {
    this.recent.set(this.recentLinks.remove(shortCode));
  }
}
