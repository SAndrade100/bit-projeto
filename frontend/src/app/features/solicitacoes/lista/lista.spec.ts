import { TestBed } from '@angular/core/testing';
import { provideNativeDateAdapter } from '@angular/material/core';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, Subject, of } from 'rxjs';
import { SolicitacaoService } from '../../../core/solicitacao.service';
import { Lista } from './lista';

describe('Lista', () => {
  const pagina = { conteudo: [], pagina: 0, tamanho: 10, totalElementos: 0, totalPaginas: 0 };
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  const api = { listar: vi.fn(), categorias: vi.fn() };

  beforeEach(() => {
    api.listar.mockReset().mockReturnValue(of(pagina));
    api.categorias.mockReset().mockReturnValue(of([]));
    params = new BehaviorSubject(convertToParamMap({ status: 'ABERTO' }));
    TestBed.configureTestingModule({
      imports: [Lista],
      providers: [
        provideRouter([]),
        provideNativeDateAdapter(),
        { provide: SolicitacaoService, useValue: api },
        { provide: ActivatedRoute, useValue: { queryParamMap: params.asObservable() } },
      ],
    });
  });

  it('aplica o status vindo da query string ao abrir a tela', async () => {
    const fixture = TestBed.createComponent(Lista);
    await fixture.whenStable();

    expect(api.listar).toHaveBeenCalledTimes(1);
    expect(api.listar.mock.calls[0][0].status).toBe('ABERTO');
  });

  it('limpa o filtro quando a query string perde o status (ex.: clique no menu)', async () => {
    const fixture = TestBed.createComponent(Lista);
    await fixture.whenStable();

    params.next(convertToParamMap({}));
    await fixture.whenStable();

    expect(api.listar).toHaveBeenCalledTimes(2);
    expect(api.listar.mock.calls[1][0].status).toBeNull();
  });

  it('ignora status inválido na query string', async () => {
    params.next(convertToParamMap({ status: 'XYZ' }));
    const fixture = TestBed.createComponent(Lista);
    await fixture.whenStable();

    expect(api.listar.mock.calls[0][0].status).toBeNull();
  });

  it('uma resposta atrasada não sobrescreve a da consulta mais recente', async () => {
    const lenta = new Subject<typeof pagina>();
    const rapida = new Subject<typeof pagina>();
    api.listar.mockReset().mockReturnValueOnce(lenta).mockReturnValueOnce(rapida);

    const fixture = TestBed.createComponent(Lista); // 1ª consulta (lenta) ao abrir
    await fixture.whenStable();
    params.next(convertToParamMap({ status: 'CONCLUIDO' })); // 2ª consulta (rápida)
    await fixture.whenStable();

    rapida.next({ ...pagina, totalElementos: 2 });
    lenta.next({ ...pagina, totalElementos: 99 }); // chega depois, mas é de uma consulta antiga
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).not.toContain('99');
    expect(lenta.observed).toBe(false); // a consulta antiga foi cancelada
  });
});
