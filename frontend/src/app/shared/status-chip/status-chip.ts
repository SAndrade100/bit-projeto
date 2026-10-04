import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { STATUS_OPCOES, StatusSolicitacao } from '../../core/models';

@Component({
  selector: 'app-status-chip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="chip" [class]="'chip ' + status()">{{ rotulo() }}</span>`,
  styles: `
    .chip {
      display: inline-block;
      padding: 2px 12px;
      border-radius: 12px;
      font-size: 0.8125rem;
      font-weight: 500;
      white-space: nowrap;
    }
    .ABERTO { background: #dbeafe; color: #1e3a8a; }
    .EM_ATENDIMENTO { background: #fef3c7; color: #78350f; }
    .CONCLUIDO { background: #d1fae5; color: #064e3b; }
  `,
})
export class StatusChip {
  readonly status = input.required<StatusSolicitacao>();
  protected readonly rotulo = computed(
    () => STATUS_OPCOES.find((o) => o.valor === this.status())?.rotulo ?? this.status(),
  );
}
