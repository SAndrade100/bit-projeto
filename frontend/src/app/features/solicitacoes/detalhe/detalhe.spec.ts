import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Solicitacao } from '../../../core/models';
import { SolicitacaoService } from '../../../core/solicitacao.service';
import { Detalhe } from './detalhe';

function solicitacao(parcial: Partial<Solicitacao>): Solicitacao {
  return {
    id: 1,
    codigo: 'SOL-000001',
    titulo: 'Notebook',
    descricao: 'Preciso de um notebook',
    categoria: { id: 1, nome: 'TI' },
    solicitante: { id: 1, username: 'ana.silva', nome: 'Ana Silva' },
    status: 'ABERTO',
    statusDescricao: 'Aberto',
    proximoStatus: 'EM_ATENDIMENTO',
    editavel: true,
    criadoEm: '2026-10-04T10:00:00Z',
    atualizadoEm: '2026-10-04T10:00:00Z',
    ...parcial,
  };
}

describe('Detalhe', () => {
  async function renderizar(dados: Solicitacao) {
    TestBed.configureTestingModule({
      imports: [Detalhe],
      providers: [provideRouter([]), { provide: SolicitacaoService, useValue: { buscar: () => of(dados) } }],
    });
    const fixture = TestBed.createComponent(Detalhe);
    fixture.componentRef.setInput('id', '1');
    await fixture.whenStable();
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('id inválido na URL mostra "não encontrada" sem consultar a API', async () => {
    const buscar = vi.fn();
    TestBed.configureTestingModule({
      imports: [Detalhe],
      providers: [provideRouter([]), { provide: SolicitacaoService, useValue: { buscar } }],
    });
    const fixture = TestBed.createComponent(Detalhe);
    fixture.componentRef.setInput('id', 'abc');
    await fixture.whenStable();

    expect(buscar).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Solicitação não encontrada.');
  });

  it('mostra editar, excluir e iniciar atendimento para o dono de uma solicitação aberta', async () => {
    const texto = await renderizar(solicitacao({}));
    expect(texto).toContain('Editar');
    expect(texto).toContain('Excluir');
    expect(texto).toContain('Iniciar atendimento');
  });

  it('esconde editar e excluir quando a solicitação não é editável', async () => {
    const texto = await renderizar(solicitacao({ editavel: false, status: 'EM_ATENDIMENTO', proximoStatus: 'CONCLUIDO' }));
    expect(texto).not.toContain('Editar');
    expect(texto).not.toContain('Excluir');
    expect(texto).toContain('Concluir solicitação');
  });

  it('não oferece avanço de status para solicitação concluída', async () => {
    const texto = await renderizar(solicitacao({ editavel: false, status: 'CONCLUIDO', proximoStatus: null }));
    expect(texto).not.toContain('Iniciar atendimento');
    expect(texto).not.toContain('Concluir solicitação');
  });
});
