import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, firstValueFrom, of, tap } from 'rxjs';
import { Usuario } from './models';

/** Estado de autenticação baseado na sessão (cookie) mantida pela API. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly _usuario = signal<Usuario | null>(null);

  readonly usuario = this._usuario.asReadonly();
  readonly autenticado = computed(() => this._usuario() !== null);

  /** Consulta a sessão atual; também faz o servidor emitir o cookie CSRF. Nunca rejeita. */
  carregarSessao(): Promise<void> {
    return firstValueFrom(
      this.http.get<Usuario>('/api/auth/me').pipe(
        tap((usuario) => this._usuario.set(usuario)),
        catchError(() => {
          this._usuario.set(null);
          return of(null);
        }),
      ),
    ).then(() => undefined);
  }

  login(username: string, password: string): Observable<Usuario> {
    return this.http
      .post<Usuario>('/api/auth/login', { username, password })
      .pipe(tap((usuario) => this._usuario.set(usuario)));
  }

  logout(): Observable<void> {
    return this.http.post<void>('/api/auth/logout', null).pipe(tap(() => this.limparSessao()));
  }

  /** Descarta o estado local (ex.: quando a API informa que a sessão expirou). */
  limparSessao(): void {
    this._usuario.set(null);
  }
}
