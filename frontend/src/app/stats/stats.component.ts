import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { UrlService } from '../shared/services/url.service';
import { RecentLinksService } from '../shared/services/recent-links.service';
import { UrlStats } from '../shared/models/url-stats.model';

interface StatRow extends UrlStats {
  shortUrl: string;
}

@Component({
  selector: 'app-stats',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <section class="page">
      <header class="head">
        <div>
          <h1>Painel de cliques</h1>
          <p class="muted">Estatísticas ao vivo dos links criados neste navegador.</p>
        </div>
        <button mat-stroked-button (click)="refresh()" [disabled]="loading()">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
      </header>

      @if (loading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      @if (rows().length > 0) {
        <div class="summary surface">
          <div class="metric">
            <span class="value mono">{{ totalClicks() | number }}</span>
            <span class="muted">cliques no total</span>
          </div>
          <div class="metric">
            <span class="value mono">{{ rows().length | number }}</span>
            <span class="muted">links monitorados</span>
          </div>
        </div>

        <div class="table surface">
          <div class="trow thead">
            <span>Short code</span>
            <span>Destino</span>
            <span>Criado</span>
            <span class="num">Cliques</span>
          </div>
          @for (row of rows(); track row.shortCode) {
            <div class="trow">
              <a class="mono code" [href]="row.shortUrl" target="_blank" rel="noopener">{{ row.shortCode }}</a>
              <span class="dest muted">{{ row.longUrl }}</span>
              <span class="muted">{{ row.createdAt | date: 'dd/MM/yyyy HH:mm' }}</span>
              <span class="num mono clicks">{{ row.clicks | number }}</span>
            </div>
          }
        </div>
      } @else if (!loading()) {
        <div class="surface empty">
          <mat-icon>insights</mat-icon>
          <p>Nenhum link ainda.</p>
          <a mat-flat-button color="primary" routerLink="/">Encurtar uma URL</a>
        </div>
      }
    </section>
  `,
  styles: [`
    .head {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 16px;
      margin: 12px 0 22px;
    }
    .head h1 {
      margin: 0 0 4px;
      font-size: 1.7rem;
      font-weight: 800;
    }
    .summary {
      display: flex;
      gap: 40px;
      padding: 20px 24px;
      margin-bottom: 18px;
    }
    .metric {
      display: flex;
      flex-direction: column;
    }
    .metric .value {
      font-size: 2rem;
      font-weight: 700;
      color: var(--brand);
    }
    .table {
      overflow: hidden;
    }
    .trow {
      display: grid;
      grid-template-columns: 130px 1fr 150px 90px;
      gap: 14px;
      align-items: center;
      padding: 14px 18px;
      border-bottom: 1px solid var(--border);
    }
    .trow:last-child {
      border-bottom: none;
    }
    .thead {
      font-size: 0.74rem;
      text-transform: uppercase;
      letter-spacing: 1px;
      color: var(--muted);
      background: #f7fafe;
    }
    .code {
      font-weight: 600;
    }
    .dest {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .num {
      text-align: right;
    }
    .clicks {
      font-weight: 700;
      color: var(--ink);
    }
    .empty {
      text-align: center;
      padding: 48px 24px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
    }
    .empty mat-icon {
      font-size: 42px;
      width: 42px;
      height: 42px;
      color: var(--muted);
    }
    @media (max-width: 640px) {
      .trow {
        grid-template-columns: 1fr 70px;
      }
      .trow .dest,
      .trow span:nth-child(3),
      .thead span:nth-child(2),
      .thead span:nth-child(3) {
        display: none;
      }
    }
  `]
})
export class StatsComponent {
  private readonly urlService = inject(UrlService);
  private readonly recentLinks = inject(RecentLinksService);

  readonly loading = signal(false);
  readonly rows = signal<StatRow[]>([]);
  readonly totalClicks = computed(() => this.rows().reduce((sum, row) => sum + row.clicks, 0));

  constructor() {
    this.refresh();
  }

  refresh(): void {
    const links = this.recentLinks.list();
    if (links.length === 0) {
      this.rows.set([]);
      return;
    }
    this.loading.set(true);
    forkJoin(
      links.map((link) =>
        this.urlService.getStats(link.shortCode).pipe(
          map((stats) => ({ ...stats, shortUrl: link.shortUrl }) as StatRow),
          catchError(() =>
            of({
              shortCode: link.shortCode,
              longUrl: link.longUrl,
              createdAt: link.createdAt,
              clicks: 0,
              shortUrl: link.shortUrl
            } as StatRow)
          )
        )
      )
    ).subscribe((results) => {
      this.rows.set(results.sort((a, b) => b.clicks - a.clicks));
      this.loading.set(false);
    });
  }
}
