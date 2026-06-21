import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./shorten/shorten.component').then((m) => m.ShortenComponent)
  },
  {
    path: 'painel',
    loadComponent: () => import('./stats/stats.component').then((m) => m.StatsComponent)
  },
  { path: '**', redirectTo: '' }
];
