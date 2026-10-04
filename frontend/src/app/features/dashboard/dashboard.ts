import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { mensagemDeErro } from '../../core/api-error';
import { Dashboard as Indicadores } from '../../core/models';
import { SolicitacaoService } from '../../core/solicitacao.service';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, MatCardModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly api = inject(SolicitacaoService);

  protected readonly indicadores = signal<Indicadores | null>(null);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.api.dashboard().subscribe({
      next: (dados) => this.indicadores.set(dados),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
  }
}
