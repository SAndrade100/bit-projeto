import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { errosDeCampos, mensagemDeErro } from '../../../core/api-error';
import { Categoria } from '../../../core/models';
import { SolicitacaoService } from '../../../core/solicitacao.service';

/** Formulário único para criar (/nova) e editar (/:id/editar) uma solicitação. */
@Component({
  selector: 'app-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './form.html',
  styleUrl: './form.scss',
})
export class Form implements OnInit {
  /** Parâmetro de rota :id (ligado por withComponentInputBinding); ausente ao criar. */
  readonly id = input(undefined, { transform: (v: unknown) => (v == null ? undefined : numberAttribute(v)) });

  private readonly api = inject(SolicitacaoService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly edicao = computed(() => this.id() !== undefined);
  /** Em /solicitacoes/abc/editar o id vira NaN: não há o que editar. */
  protected readonly idInvalido = computed(() => {
    const id = this.id();
    return id !== undefined && !(Number.isInteger(id) && id > 0);
  });
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly carregando = signal(false);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    descricao: ['', [Validators.required, Validators.maxLength(5000)]],
    categoriaId: [null as number | null, Validators.required],
  });

  constructor() {
    this.api.categorias().subscribe((lista) => this.categorias.set(lista));
  }

  ngOnInit(): void {
    const id = this.id();
    if (id === undefined) return;
    if (this.idInvalido()) {
      this.erro.set('Solicitação não encontrada.');
      return;
    }

    this.carregando.set(true);
    this.api.buscar(id).subscribe({
      next: (s) => {
        if (!s.editavel) {
          this.snackBar.open('Esta solicitação não pode mais ser editada.', 'Fechar', { duration: 5000 });
          this.router.navigate(['/solicitacoes', id]);
          return;
        }
        this.form.setValue({ titulo: s.titulo, descricao: s.descricao, categoriaId: s.categoria.id });
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }

  protected salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { titulo, descricao, categoriaId } = this.form.getRawValue();
    const dados = { titulo, descricao, categoriaId: categoriaId! };
    const id = this.id();

    this.enviando.set(true);
    this.erro.set(null);
    const requisicao = id === undefined ? this.api.criar(dados) : this.api.atualizar(id, dados);
    requisicao.subscribe({
      next: (s) => {
        this.snackBar.open(id === undefined ? 'Solicitação criada.' : 'Solicitação atualizada.', 'Fechar', {
          duration: 4000,
        });
        this.router.navigate(['/solicitacoes', s.id]);
      },
      error: (e) => {
        this.aplicarErrosDoServidor(e);
        this.erro.set(mensagemDeErro(e));
        this.enviando.set(false);
      },
    });
  }

  /** Reflete nos campos as mensagens de validação devolvidas pela API. */
  private aplicarErrosDoServidor(erro: unknown): void {
    for (const [campo, mensagem] of Object.entries(errosDeCampos(erro))) {
      this.form.get(campo)?.setErrors({ servidor: mensagem });
    }
  }

  protected mensagemDoServidor(campo: 'titulo' | 'descricao' | 'categoriaId'): string | null {
    return this.form.controls[campo].getError('servidor') ?? null;
  }
}
