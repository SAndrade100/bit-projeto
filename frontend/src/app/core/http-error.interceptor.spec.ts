import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { httpErrorInterceptor } from './http-error.interceptor';

describe('httpErrorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  const auth = { limparSessao: vi.fn() };
  let navegar: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    auth.limparSessao.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([httpErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  it('401 em chamada comum encerra a sessão local e vai ao login', () => {
    http.get('/api/solicitacoes').subscribe({ error: () => undefined });
    controller.expectOne('/api/solicitacoes').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.limparSessao).toHaveBeenCalled();
    expect(navegar).toHaveBeenCalledWith(['/login'], expect.anything());
  });

  it('401 nas chamadas de autenticação é deixado para a própria tela', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    controller.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.limparSessao).not.toHaveBeenCalled();
    expect(navegar).not.toHaveBeenCalled();
  });

  it('outros erros não afetam a sessão', () => {
    http.get('/api/solicitacoes').subscribe({ error: () => undefined });
    controller.expectOne('/api/solicitacoes').flush({}, { status: 500, statusText: 'Server Error' });

    expect(auth.limparSessao).not.toHaveBeenCalled();
  });
});
