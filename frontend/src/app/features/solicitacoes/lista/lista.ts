import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { mensagemDeErro } from '../../../core/api-error';
import { paraIsoData } from '../../../core/datas';
import {
  Categoria,
  FiltroSolicitacao,
  Pagina,
  STATUS_OPCOES,
  Solicitacao,
  StatusSolicitacao,
} from '../../../core/models';
import { SolicitacaoService } from '../../../core/solicitacao.service';
import { StatusChip } from '../../../shared/status-chip/status-chip';

@Component({
  selector: 'app-lista',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    StatusChip,
  ],
  templateUrl: './lista.html',
  styleUrl: './lista.scss',
})
export class Lista {
  private readonly api = inject(SolicitacaoService);
  private readonly rota = inject(ActivatedRoute);

  protected readonly colunas = ['codigo', 'titulo', 'categoria', 'solicitante', 'data', 'status', 'acoes'];
  protected readonly statusOpcoes = STATUS_OPCOES;

  protected readonly form = inject(FormBuilder).group({
    titulo: [''],
    categoriaId: [null as number | null],
    status: [null as StatusSolicitacao | null],
    inicio: [null as Date | null],
    fim: [null as Date | null],
  });

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly pagina = signal<Pagina<Solicitacao> | null>(null);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly tamanho = signal(10);

  /** Filtro efetivamente aplicado (o formulário só vale depois de "Filtrar"). */
  private filtroAplicado: FiltroSolicitacao = {};

  constructor() {
    this.api.categorias().subscribe((lista) => this.categorias.set(lista));

    // O dashboard leva aqui com ?status=... para listar um grupo específico.
    const status = this.rota.snapshot.queryParamMap.get('status');
    if (STATUS_OPCOES.some((o) => o.valor === status)) {
      this.form.patchValue({ status: status as StatusSolicitacao });
    }
    this.filtrar();
  }

  protected filtrar(): void {
    const { titulo, categoriaId, status, inicio, fim } = this.form.getRawValue();
    this.filtroAplicado = {
      titulo: titulo ?? '',
      categoriaId,
      status,
      dataInicio: paraIsoData(inicio),
      dataFim: paraIsoData(fim),
    };
    this.carregar(0);
  }

  protected limpar(): void {
    this.form.reset();
    this.filtrar();
  }

  protected mudarPagina(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.carregar(evento.pageIndex);
  }

  private carregar(pagina: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api.listar(this.filtroAplicado, pagina, this.tamanho()).subscribe({
      next: (resultado) => {
        this.pagina.set(resultado);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }
}
