import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { throwError } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { Login } from './login';

describe('Login', () => {
  const auth = { login: vi.fn() };

  beforeEach(() => {
    auth.login.mockReset();
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  async function renderizar() {
    const fixture = TestBed.createComponent(Login);
    await fixture.whenStable();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  }

  it('não chama a API com o formulário vazio e mostra os erros', async () => {
    const { fixture, el } = await renderizar();
    (el.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    expect(auth.login).not.toHaveBeenCalled();
    expect(el.textContent).toContain('Informe o usuário');
    expect(el.textContent).toContain('Informe a senha');
  });

  it('exibe a mensagem da API quando as credenciais são inválidas', async () => {
    auth.login.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { detail: 'Usuário ou senha inválidos.' } })),
    );
    const { fixture, el } = await renderizar();
    const campos = el.querySelectorAll('input');
    (campos[0] as HTMLInputElement).value = 'ana.silva';
    campos[0].dispatchEvent(new Event('input'));
    (campos[1] as HTMLInputElement).value = 'errada';
    campos[1].dispatchEvent(new Event('input'));
    (el.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    expect(auth.login).toHaveBeenCalledWith('ana.silva', 'errada');
    expect(el.querySelector('[role=alert]')?.textContent).toContain('Usuário ou senha inválidos.');
  });
});
