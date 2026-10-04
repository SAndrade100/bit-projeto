import { TestBed } from '@angular/core/testing';
import { StatusChip } from './status-chip';

describe('StatusChip', () => {
  it.each([
    ['ABERTO', 'Aberto'],
    ['EM_ATENDIMENTO', 'Em Atendimento'],
    ['CONCLUIDO', 'Concluído'],
  ] as const)('exibe o rótulo de %s', async (status, rotulo) => {
    const fixture = TestBed.createComponent(StatusChip);
    fixture.componentRef.setInput('status', status);
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(rotulo);
  });
});
