import { Routes } from '@angular/router';
import { authGuard, visitanteGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [visitanteGuard],
    title: 'Entrar',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        title: 'Dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'solicitacoes',
        title: 'Solicitações',
        loadComponent: () => import('./features/solicitacoes/lista/lista').then((m) => m.Lista),
      },
      {
        path: 'solicitacoes/nova',
        title: 'Nova solicitação',
        loadComponent: () => import('./features/solicitacoes/form/form').then((m) => m.Form),
      },
      {
        path: 'solicitacoes/:id',
        title: 'Detalhes da solicitação',
        loadComponent: () => import('./features/solicitacoes/detalhe/detalhe').then((m) => m.Detalhe),
      },
      {
        path: 'solicitacoes/:id/editar',
        title: 'Editar solicitação',
        loadComponent: () => import('./features/solicitacoes/form/form').then((m) => m.Form),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
