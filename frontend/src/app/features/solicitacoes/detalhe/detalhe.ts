import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, numberAttribute, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { mensagemDeErro } from '../../../core/api-error';
import { Solicitacao, StatusSolicitacao } from '../../../core/models';
import { SolicitacaoService } from '../../../core/solicitacao.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { StatusChip } from '../../../shared/status-chip/status-chip';

/** Texto do botão que avança o fluxo, conforme o status de destino. */
const ROTULO_AVANCO: Record<StatusSolicitacao, string> = {
  ABERTO: '',
  EM_ATENDIMENTO: 'Iniciar atendimento',
  CONCLUIDO: 'Concluir solicitação',
};

@Component({
  selector: 'app-detalhe',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule, StatusChip],
  templateUrl: './detalhe.html',
  styleUrl: './detalhe.scss',
})
export class Detalhe implements OnInit {
  /** Parâmetro de rota :id. */
  readonly id = input.required({ transform: numberAttribute });

  private readonly api = inject(SolicitacaoService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly solicitacao = signal<Solicitacao | null>(null);
  protected readonly erro = signal<string | null>(null);
  protected readonly processando = signal(false);

  ngOnInit(): void {
    // /solicitacoes/abc chega aqui como NaN: não vale a pena (nem faz sentido) consultar a API.
    if (!Number.isInteger(this.id()) || this.id() <= 0) {
      this.erro.set('Solicitação não encontrada.');
      return;
    }
    this.api.buscar(this.id()).subscribe({
      next: (s) => this.solicitacao.set(s),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }

  protected rotuloAvanco(s: Solicitacao): string {
    return s.proximoStatus ? ROTULO_AVANCO[s.proximoStatus] : '';
  }

  protected avancar(s: Solicitacao): void {
    if (!s.proximoStatus) return;
    this.processando.set(true);
    this.api.alterarStatus(s.id, s.proximoStatus).subscribe({
      next: (atualizada) => {
        this.solicitacao.set(atualizada);
        this.processando.set(false);
        this.snackBar.open(`Status alterado para "${atualizada.statusDescricao}".`, 'Fechar', { duration: 4000 });
      },
      error: (e) => this.falhar(e),
    });
  }

  protected excluir(s: Solicitacao): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          titulo: 'Excluir solicitação',
          mensagem: `Deseja excluir a solicitação ${s.codigo}? Esta ação não pode ser desfeita.`,
          confirmar: 'Excluir',
        },
      })
      .afterClosed()
      .subscribe((confirmado) => {
        if (!confirmado) return;
        this.processando.set(true);
        this.api.excluir(s.id).subscribe({
          next: () => {
            this.snackBar.open('Solicitação excluída.', 'Fechar', { duration: 4000 });
            this.router.navigate(['/solicitacoes']);
          },
          error: (e) => this.falhar(e),
        });
      });
  }

  private falhar(erro: unknown): void {
    this.processando.set(false);
    this.snackBar.open(mensagemDeErro(erro), 'Fechar', { duration: 6000 });
  }
}
