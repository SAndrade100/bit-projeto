import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SolicitacaoService } from './solicitacao.service';

describe('SolicitacaoService', () => {
  let service: SolicitacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SolicitacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listar envia apenas os filtros preenchidos', () => {
    service
      .listar({ titulo: '  notebook ', categoriaId: null, status: 'ABERTO', dataInicio: '2026-10-01', dataFim: '' }, 2, 25)
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/solicitacoes');
    expect(req.request.params.get('titulo')).toBe('notebook');
    expect(req.request.params.get('status')).toBe('ABERTO');
    expect(req.request.params.get('dataInicio')).toBe('2026-10-01');
    expect(req.request.params.get('pagina')).toBe('2');
    expect(req.request.params.get('tamanho')).toBe('25');
    expect(req.request.params.has('categoriaId')).toBe(false);
    expect(req.request.params.has('dataFim')).toBe(false);
    req.flush({ conteudo: [], pagina: 2, tamanho: 25, totalElementos: 0, totalPaginas: 0 });
  });

  it('alterarStatus usa PATCH no recurso de status', () => {
    service.alterarStatus(7, 'EM_ATENDIMENTO').subscribe();
    const req = http.expectOne('/api/solicitacoes/7/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'EM_ATENDIMENTO' });
    req.flush({});
  });
});
