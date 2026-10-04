import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, visitanteGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('guards de autenticação', () => {
  const autenticado = signal(false);

  beforeEach(() => {
    autenticado.set(false);
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: { autenticado } }],
    });
  });

  const executar = (guard: typeof authGuard, url = '/solicitacoes') =>
    TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );

  it('authGuard envia o visitante ao login lembrando o destino', () => {
    const resultado = executar(authGuard, '/solicitacoes/3') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login?redirect=%2Fsolicitacoes%2F3');
  });

  it('authGuard libera usuário autenticado', () => {
    autenticado.set(true);
    expect(executar(authGuard)).toBe(true);
  });

  it('visitanteGuard libera o visitante e redireciona quem já está logado', () => {
    expect(executar(visitanteGuard)).toBe(true);

    autenticado.set(true);
    const resultado = executar(visitanteGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/dashboard');
  });
});
