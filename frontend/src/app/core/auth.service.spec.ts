import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { AuthService } from './auth.service';

const ANA = { id: 1, username: 'ana.silva', nome: 'Ana Silva' };

describe('AuthService', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('carrega o usuário quando já existe sessão', async () => {
    const carga = auth.carregarSessao();
    http.expectOne('/api/auth/me').flush(ANA);
    await carga;

    expect(auth.autenticado()).toBe(true);
    expect(auth.usuario()).toEqual(ANA);
  });

  it('permanece deslogado (sem rejeitar) quando a API responde 401', async () => {
    const carga = auth.carregarSessao();
    http.expectOne('/api/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    await carga;

    expect(auth.autenticado()).toBe(false);
  });

  it('login guarda o usuário e logout o remove', () => {
    auth.login('ana.silva', 'senha123').subscribe();
    const req = http.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ username: 'ana.silva', password: 'senha123' });
    req.flush(ANA);
    expect(auth.usuario()).toEqual(ANA);

    auth.logout().subscribe();
    http.expectOne('/api/auth/logout').flush(null, { status: 204, statusText: 'No Content' });
    expect(auth.usuario()).toBeNull();
    // Reemite o cookie CSRF apagado pelo logout (senão o próximo login seria rejeitado).
    http.expectOne('/api/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });
  });

  it('logout só conclui depois de consultar a sessão (reemitindo o token CSRF)', async () => {
    const logout = firstValueFrom(auth.logout(), { defaultValue: undefined });
    http.expectOne('/api/auth/logout').flush(null, { status: 204, statusText: 'No Content' });
    // o GET /api/auth/me é disparado em seguida e só então o logout termina
    await Promise.resolve();
    http.expectOne('/api/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });

    await logout;
    expect(auth.autenticado()).toBe(false);
  });

  it('não guarda usuário se o login falhar', () => {
    auth.login('ana.silva', 'errada').subscribe({ error: () => undefined });
    http.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.autenticado()).toBe(false);
  });
});
