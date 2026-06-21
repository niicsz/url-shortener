import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule, MatIconModule],
  template: `
    <mat-toolbar class="topbar">
      <a class="brand" routerLink="/">
        <mat-icon>link</mat-icon>
        <span>BiniTech <strong>Encurtador</strong></span>
      </a>
      <span class="spacer"></span>
      <nav class="nav">
        <a mat-button routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Encurtar</a>
        <a mat-button routerLink="/painel" routerLinkActive="active">Painel</a>
      </nav>
    </mat-toolbar>
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .topbar {
      position: sticky;
      top: 0;
      z-index: 10;
      background: rgba(255, 255, 255, 0.85);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid var(--border);
      color: var(--ink);
      height: 64px;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.05rem;
      letter-spacing: 0.2px;
    }
    .brand mat-icon {
      color: var(--brand);
    }
    .brand strong {
      color: var(--brand);
    }
    .spacer {
      flex: 1 1 auto;
    }
    .nav a.active {
      color: var(--brand);
      font-weight: 600;
    }
  `]
})
export class AppComponent {}
